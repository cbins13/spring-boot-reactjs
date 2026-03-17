package com.example.platform.modules.users.mapper;

import com.example.platform.modules.users.dto.UserDto;
import com.example.platform.modules.users.entity.RoleEntity;
import com.example.platform.modules.users.entity.UserEntity;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(UserEntity entity);

    /**
     * Maps a set of role entities on the user to a list of role names for the DTO.
     */
    default List<String> map(Set<RoleEntity> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        return roles.stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toList());
    }
}

