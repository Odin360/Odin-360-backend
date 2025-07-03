package com.Odin360.services;

import com.Odin360.Domains.Dtos.CreateUserDto;
import com.Odin360.Domains.Dtos.EmailDto;
import com.Odin360.Domains.Dtos.UserPasswordDto;
import com.Odin360.Domains.entities.Team;
import com.Odin360.Domains.entities.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.Set;

@Service
public interface UserService {
    Set<User> listUsers();

    User createUser(CreateUserDto createUserDto);

    User getUserById(UUID id);

    void updatePasswordByEmail(UserPasswordDto passwordDto);

    User findByEmail(EmailDto emailDto);

    User joinTeam(UUID userId, UUID teamId);

    void deleteById(UUID userId);

    Set<Team> getTeams(UUID userId);

    UUID generateRandomUUID();
}
