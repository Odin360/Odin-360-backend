package com.Odin360.mappers;

import com.Odin360.Domains.Dtos.CreateUserDto;
import com.Odin360.Domains.Dtos.EmailDto;
import com.Odin360.Domains.Dtos.OtpDto;
import com.Odin360.Domains.Dtos.UserPasswordDto;
import com.Odin360.Domains.entities.User;
import org.mapstruct.Mapper;

import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    CreateUserDto toDto (User user);
    User fromDto (CreateUserDto createUserDto);
    User fromPasswordDto (UserPasswordDto userPasswordDto);
    User fromEmailDto (EmailDto emailDto);
}
