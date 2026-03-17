package com.example.platform.security.userdetails;

import com.example.platform.modules.users.entity.PermissionEntity;
import com.example.platform.modules.users.entity.RoleEntity;
import com.example.platform.modules.users.entity.UserEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

    private final long userId;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal fromEntity(UserEntity user) {
        // Role-based authorities (e.g. ROLE_USER, ROLE_ADMIN)
        Stream<SimpleGrantedAuthority> roleAuthorities = user.getRoles().stream()
                .map(RoleEntity::getName)
                .map(SimpleGrantedAuthority::new);

        // Permission-based authorities (e.g. MANAGE_USERS, VIEW_AUDIT_LOGS)
        Stream<SimpleGrantedAuthority> permissionAuthorities = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(PermissionEntity::getName)
                .distinct()
                .map(SimpleGrantedAuthority::new);

        List<SimpleGrantedAuthority> authorities = Stream.concat(roleAuthorities, permissionAuthorities)
                .toList();

        return new UserPrincipal(user.getId(), user.getEmail(), user.getPassword(), authorities);
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}