package com.example.platform.modules.admin.service;

import com.example.platform.modules.users.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminUserService {
    Page<UserDto> listUsers(Pageable pageable);
    UserDto updateUserRoles(long targetUserId, List<String> roleNames, long actorUserId);
    List<String> listAvailableRoles();
}