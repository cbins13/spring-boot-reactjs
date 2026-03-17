package com.example.platform.modules.users.service;

import com.example.platform.modules.users.dto.CreateUserRequest;
import com.example.platform.modules.users.dto.UpdateUserRequest;
import com.example.platform.modules.users.dto.UserDto;
import com.example.platform.modules.users.entity.Role;
import com.example.platform.modules.users.entity.UserEntity;

import java.util.List;
import java.util.Optional;

/**
 * Service interface is the module boundary: other modules may depend on this interface,
 * but must not access repositories/entities directly.
 */
public interface UsersService {
    List<UserDto> listUsers();

    UserDto getById(long id);

    UserDto createUser(CreateUserRequest request, Role role);

    /**
     * Create a user while providing the actor user id for auditing purposes.
     * Implementations may emit audit logs using this context.
     */
    UserDto createUser(CreateUserRequest request, Role role, Long actorUserId);

    UserDto updateUser(long id, UpdateUserRequest request);

    void deleteUser(long id);

    Optional<UserEntity> findEntityByEmail(String email);

    Optional<UserEntity> findEntityById(long id);
}

