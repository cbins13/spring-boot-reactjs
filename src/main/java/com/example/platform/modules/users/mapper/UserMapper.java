package com.example.platform.modules.users.mapper;

import com.example.platform.modules.users.dto.UserDto;
import com.example.platform.modules.users.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(UserEntity entity);
}

