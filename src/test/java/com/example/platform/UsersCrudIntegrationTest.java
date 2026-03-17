package com.example.platform;

import com.example.platform.modules.auth.dto.AuthResponse;
import com.example.platform.modules.auth.dto.LoginRequest;
import com.example.platform.modules.users.dto.CreateUserRequest;
import com.example.platform.modules.users.dto.UserDto;
import com.example.platform.modules.users.entity.Role;
import com.example.platform.modules.users.service.UsersService;
import com.example.platform.testsupport.OracleTestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UsersCrudIntegrationTest extends OracleTestContainerConfig {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    UsersService usersService;

    @Test
    void admin_can_list_and_delete_users() {
        // Create admin directly via users service boundary (no controller needed).
        usersService.createUser(new CreateUserRequest("admin@example.com", "AdminPass123!", null), Role.ROLE_ADMIN);
        usersService.createUser(new CreateUserRequest("u2@example.com", "Password123!", null), Role.ROLE_USER);

        AuthResponse login = restTemplate.postForEntity(
                "/api/auth/login",
                new LoginRequest("admin@example.com", "AdminPass123!"),
                AuthResponse.class
        ).getBody();
        assertThat(login).isNotNull();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(login.getAccessToken());

        ResponseEntity<UserDto[]> listResp = restTemplate.exchange(
                "/api/users",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                UserDto[].class
        );
        assertThat(listResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResp.getBody()).isNotNull();
        assertThat(listResp.getBody().length).isGreaterThanOrEqualTo(2);

        long userId = usersService.findEntityByEmail("u2@example.com").orElseThrow().getId();
        ResponseEntity<Void> delResp = restTemplate.exchange(
                "/api/users/" + userId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );
        assertThat(delResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void user_cannot_list_users() {
        usersService.createUser(new CreateUserRequest("u3@example.com", "Password123!", null), Role.ROLE_USER);
        AuthResponse login = restTemplate.postForEntity(
                "/api/auth/login",
                new LoginRequest("u3@example.com", "Password123!"),
                AuthResponse.class
        ).getBody();
        assertThat(login).isNotNull();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(login.getAccessToken());
        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/users",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}

