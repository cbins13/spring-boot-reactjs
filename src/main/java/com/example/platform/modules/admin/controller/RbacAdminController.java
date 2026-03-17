package com.example.platform.modules.admin.controller;

import com.example.platform.modules.users.dto.PermissionDto;
import com.example.platform.modules.users.dto.RoleDto;
import com.example.platform.modules.users.entity.PermissionEntity;
import com.example.platform.modules.users.entity.RoleEntity;
import com.example.platform.modules.users.repository.PermissionRepository;
import com.example.platform.modules.users.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RbacAdminController {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    @GetMapping("/roles")
    public List<RoleDto> listRoles() {
        return roleRepository.findAll().stream()
                .map(this::toRoleDto)
                .toList();
    }

    @GetMapping("/permissions")
    public List<PermissionDto> listPermissions() {
        return permissionRepository.findAll().stream()
                .map(this::toPermissionDto)
                .toList();
    }

    private RoleDto toRoleDto(RoleEntity role) {
        List<String> permissions = role.getPermissions() == null
                ? List.of()
                : role.getPermissions().stream()
                .map(PermissionEntity::getName)
                .sorted()
                .toList();

        return RoleDto.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(permissions)
                .build();
    }

    private PermissionDto toPermissionDto(PermissionEntity permission) {
        return PermissionDto.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .build();
    }
}

