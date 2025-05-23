package com.Odin360.services.impl;

import com.Odin360.Domains.Dtos.CreateUserDto;
import com.Odin360.Domains.Dtos.EmailDto;
import com.Odin360.Domains.Dtos.UserPasswordDto;
import com.Odin360.Domains.entities.User;
import com.Odin360.mappers.UserMapper;
import com.Odin360.repositories.UserRepository;
import com.Odin360.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    @Override
    public User createUser(CreateUserDto createUserDto) {
         User user = userMapper.fromDto(createUserDto);
         user.setPassword(passwordEncoder.encode(user.getPassword()));
         return userRepository.save(user);
    }


    @Override
    public User getUserById(UUID id) {
        return userRepository.findById(id).
                orElseThrow(()->new RuntimeException("User does not exist"));
    }

    @Override
    public void updatePasswordByEmail(UserPasswordDto passwordDto) {
    User user = userMapper.fromPasswordDto(passwordDto);
    User retrievedUser = userRepository.findByEmail(user.getEmail())
            .orElseThrow(()->new RuntimeException(user.getEmail()+" was not found"));
    retrievedUser.setPassword(passwordEncoder.encode(user.getPassword()));
    userRepository.save(retrievedUser);
    }

    @Override
    public User findByEmail(EmailDto emailDto) {
        User user = userMapper.fromEmailDto(emailDto);
        return userRepository.findByEmail(user.getEmail())
                .orElseThrow(()->new RuntimeException(user.getEmail()+" was not found"));
    }

}
