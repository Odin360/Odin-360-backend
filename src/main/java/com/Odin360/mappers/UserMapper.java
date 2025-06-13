package com.Odin360.mappers;

import com.Odin360.Domains.Dtos.*;
import com.Odin360.Domains.entities.User;
import org.mapstruct.Mapper;

import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    CreateUserDto toDto (User user);
    User fromDto (CreateUserDto createUserDto);
    User fromPasswordDto (UserPasswordDto userPasswordDto);
    User fromEmailDto (EmailDto emailDto);
    UserDto fromUser (User user);
}
