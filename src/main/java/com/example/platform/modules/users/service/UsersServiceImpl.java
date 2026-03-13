package com.example.platform.modules.users.service;

import com.example.platform.modules.users.dto.CreateUserRequest;
import com.example.platform.modules.users.dto.UpdateUserRequest;
import com.example.platform.modules.users.dto.UserDto;
import com.example.platform.modules.users.entity.Role;
import com.example.platform.modules.users.entity.UserEntity;
import com.example.platform.modules.users.mapper.UserMapper;
import com.example.platform.modules.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {
    private final UsersRepository usersRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> listUsers() {
        return usersRepository.findAll().stream().map(userMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getById(long id) {
        UserEntity entity = usersRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return userMapper.toDto(entity);
    }

    @Override
    @Transactional
    public UserDto createUser(CreateUserRequest request, Role role) {
        if (usersRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new IllegalArgumentException("Email already in use");
        }
        Role effectiveRole = request.getRole() != null ? request.getRole() : role;
        UserEntity entity = UserEntity.builder()
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(effectiveRole)
                .build();
        return userMapper.toDto(usersRepository.save(entity));
    }

    @Override
    @Transactional
    public UserDto updateUser(long id, UpdateUserRequest request) {
        UserEntity entity = usersRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().toLowerCase();
            if (!newEmail.equals(entity.getEmail()) && usersRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Email already in use");
            }
            entity.setEmail(newEmail);
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            entity.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRole() != null) {
            entity.setRole(request.getRole());
        }
        return userMapper.toDto(usersRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteUser(long id) {
        if (!usersRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found");
        }
        usersRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserEntity> findEntityByEmail(String email) {
        return usersRepository.findByEmail(email.toLowerCase());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserEntity> findEntityById(long id) {
        return usersRepository.findById(id);
    }
}

