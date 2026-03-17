package com.example.platform.security.userdetails;

import com.example.platform.modules.users.entity.UserEntity;
import com.example.platform.modules.users.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UsersService usersService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = usersService.findEntityByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return UserPrincipal.fromEntity(user);
    }
}