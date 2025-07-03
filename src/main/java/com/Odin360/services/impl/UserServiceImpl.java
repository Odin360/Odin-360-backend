package com.Odin360.services.impl;

import com.Odin360.Domains.Dtos.CreateUserDto;
import com.Odin360.Domains.Dtos.EmailDto;
import com.Odin360.Domains.Dtos.UserPasswordDto;
import com.Odin360.Domains.entities.Team;
import com.Odin360.Domains.entities.User;
import com.Odin360.mappers.UserMapper;
import com.Odin360.repositories.TeamRepository;
import com.Odin360.repositories.UserRepository;
import com.Odin360.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TeamRepository teamRepository;
    @Override
    public Set<User> listUsers() {
        List<User>users= userRepository.findAll();
        return new HashSet<>(users);
    }

    @Override
    @Transactional
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

    @Override
    @Transactional
    public User joinTeam(UUID userId, UUID teamId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("User not found"));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(()->new RuntimeException("Team not found"));
        user.getTeams().add(team);
        team.getUsers().add(user);
        teamRepository.save(team);
       return userRepository.save(user);

    }

    @Override
    public void deleteById(UUID userId) {
        if(userRepository.existsById(userId)){
            userRepository.deleteById(userId);
        }
        else {
            throw new RuntimeException("User does not exist");
        }
    }

    @Override
    public Set<Team> getTeams(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("user not found"));
        return user.getTeams();

    }

    @Override
    public UUID generateRandomUUID() {
        return UUID.randomUUID();
    }
}
