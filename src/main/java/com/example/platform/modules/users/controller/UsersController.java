package com.example.platform.modules.users.controller;

import com.example.platform.modules.users.dto.CreateUserRequest;
import com.example.platform.modules.users.dto.UpdateUserRequest;
import com.example.platform.modules.users.dto.UserDto;
import com.example.platform.modules.users.entity.Role;
import com.example.platform.modules.users.service.UsersService;
import com.example.platform.security.userdetails.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersController {
    private final UsersService usersService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto> list() {
        return usersService.listUsers();
    }

    @GetMapping("/{id}")
    @PreAuthorize("#id == principal.userId or hasRole('ADMIN')")
    public UserDto get(@PathVariable long id, @AuthenticationPrincipal UserPrincipal principal) {
        return usersService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto create(@Valid @RequestBody CreateUserRequest request,
                          @AuthenticationPrincipal UserPrincipal principal) {
        return usersService.createUser(request, Role.ROLE_USER, principal.getUserId());
    }

    @PutMapping("/{id}")
    @PreAuthorize("#id == principal.userId or hasRole('ADMIN')")
    public UserDto update(@PathVariable long id, @Valid @RequestBody UpdateUserRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        return usersService.updateUser(id, request);
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public void deleteCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        usersService.deleteUser(principal.getUserId());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable long id) {
        usersService.deleteUser(id);
    }
}

