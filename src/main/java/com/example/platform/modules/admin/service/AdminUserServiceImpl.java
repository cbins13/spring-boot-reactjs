package com.example.platform.modules.admin.service;

import com.example.platform.modules.users.dto.UserDto;
import com.example.platform.modules.users.entity.RoleEntity;
import com.example.platform.modules.users.entity.UserEntity;
import com.example.platform.modules.users.mapper.UserMapper;
import com.example.platform.modules.users.repository.RoleRepository;
import com.example.platform.modules.users.repository.UsersRepository;
import com.example.platform.modules.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UsersRepository usersRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> listUsers(Pageable pageable) {
        return usersRepository.findAll(pageable).map(userMapper::toDto);
    }

    @Override
    @Transactional
    public UserDto updateUserRoles(long targetUserId, List<String> roleNames, long actorUserId) {
        UserEntity actor = usersRepository.findById(actorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Actor not found"));
        UserEntity target = usersRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        var oldRoles = target.getRoles().stream().map(RoleEntity::getName).toList();

        // self-demotion prevention
        boolean actorIsAdmin = actor.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getName()));
        boolean newHasAdmin = roleNames.contains("ROLE_ADMIN");
        if (actor.getId().equals(target.getId()) && actorIsAdmin && !newHasAdmin) {
            throw new IllegalArgumentException("Admins cannot remove their own admin role");
        }

        if (roleNames.isEmpty()) {
            throw new IllegalArgumentException("User must have at least one role");
        }

        var newRoles = new HashSet<RoleEntity>();
        for (String name : roleNames) {
            RoleEntity role = roleRepository.findByName(name)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + name));
            newRoles.add(role);
        }
        target.setRoles(newRoles);
        usersRepository.save(target);

        auditService.logRoleChanged(actorUserId, targetUserId, oldRoles, roleNames, null);

        return userMapper.toDto(target);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> listAvailableRoles() {
        return roleRepository.findAll().stream()
                .map(RoleEntity::getName)
                .toList();
    }
}