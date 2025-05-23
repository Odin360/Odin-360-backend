package com.Odin360.services;

import com.Odin360.Domains.Dtos.CreateUserDto;
import com.Odin360.Domains.Dtos.EmailDto;
import com.Odin360.Domains.Dtos.UserPasswordDto;
import com.Odin360.Domains.entities.User;

import java.util.List;
import java.util.UUID;


public interface UserService {
    List<User> listUsers();

    User createUser(CreateUserDto createUserDto);

    User getUserById(UUID id);

    void updatePasswordByEmail(UserPasswordDto passwordDto);

    User findByEmail(EmailDto emailDto);
}
