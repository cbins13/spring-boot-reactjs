package com.example.platform.modules.admin.controller;

import com.example.platform.modules.admin.service.AdminUserService;
import com.example.platform.modules.users.dto.PermissionDto;
import com.example.platform.modules.users.dto.UserDto;
import com.example.platform.modules.users.repository.PermissionRepository;
import com.example.platform.security.userdetails.UserPrincipal;
import jakarta.validation.constraints.NotEmpty;
import lombok.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final PermissionRepository permissionRepository;

    @GetMapping
    public Page<UserDto> list(Pageable pageable) {
        return adminUserService.listUsers(pageable);
    }

    @Value
    public static class UpdateUserRolesRequest {
        @NotEmpty
        List<String> roles;
    }

    @PutMapping("/{id}/role")
    public UserDto updateRole(
            @PathVariable long id,
            @RequestBody UpdateUserRolesRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return adminUserService.updateUserRoles(id, request.getRoles(), principal.getUserId());
    }

    @GetMapping("/roles")
    public List<String> roles() {
        return adminUserService.listAvailableRoles();
    }

    @GetMapping("/permissions")
    public List<PermissionDto> listPermissions() {
        return permissionRepository.findAll().stream()
                .map(p -> PermissionDto.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .description(p.getDescription())
                        .build())
                .toList();
    }
}