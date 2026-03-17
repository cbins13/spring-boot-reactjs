package com.example.platform;

import com.example.platform.modules.auth.dto.AuthResponse;
import com.example.platform.modules.auth.dto.LoginRequest;
import com.example.platform.modules.auth.dto.RefreshRequest;
import com.example.platform.modules.auth.dto.RegisterRequest;
import com.example.platform.modules.users.dto.UpdateUserRequest;
import com.example.platform.modules.users.entity.UserEntity;
import com.example.platform.modules.users.service.UsersService;
import com.example.platform.testsupport.OracleTestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthIntegrationTest extends OracleTestContainerConfig {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    UsersService usersService;

    @Test
    void register_login_refresh_and_use_access_token() {
        // register
        ResponseEntity<AuthResponse> registerResp = restTemplate.postForEntity(
                "/api/auth/register",
                new RegisterRequest("user1@example.com", "Password123!"),
                AuthResponse.class
        );
        assertThat(registerResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(registerResp.getBody()).isNotNull();
        String refresh1 = registerResp.getBody().getRefreshToken();
        String access1 = registerResp.getBody().getAccessToken();
        assertThat(access1).isNotBlank();
        assertThat(refresh1).isNotBlank();

        // access protected endpoint (update own profile)
        UserEntity user = usersService.findEntityByEmail("user1@example.com").orElseThrow();
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(access1);
        HttpEntity<UpdateUserRequest> updateReq = new HttpEntity<>(new UpdateUserRequest("user1+new@example.com", null, null), authHeaders);
        ResponseEntity<String> updateResp = restTemplate.exchange(
                "/api/users/" + user.getId(),
                HttpMethod.PUT,
                updateReq,
                String.class
        );
        assertThat(updateResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // login
        ResponseEntity<AuthResponse> loginResp = restTemplate.postForEntity(
                "/api/auth/login",
                new LoginRequest("user1+new@example.com", "Password123!"),
                AuthResponse.class
        );
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResp.getBody()).isNotNull();
        String refresh2 = loginResp.getBody().getRefreshToken();
        assertThat(refresh2).isNotBlank();

        // refresh rotates token
        ResponseEntity<AuthResponse> refreshResp = restTemplate.postForEntity(
                "/api/auth/refresh",
                new RefreshRequest(refresh2),
                AuthResponse.class
        );
        assertThat(refreshResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(refreshResp.getBody()).isNotNull();
        String refresh3 = refreshResp.getBody().getRefreshToken();
        assertThat(refresh3).isNotBlank();
        assertThat(refresh3).isNotEqualTo(refresh2);

        // old refresh token should now fail
        ResponseEntity<String> refreshOldResp = restTemplate.postForEntity(
                "/api/auth/refresh",
                new RefreshRequest(refresh2),
                String.class
        );
        assertThat(refreshOldResp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void protected_endpoints_require_authentication() {
        ResponseEntity<String> resp = restTemplate.getForEntity("/api/users", String.class);
        assertThat(resp.getStatusCode().value()).isIn(401, 403);
    }
}

