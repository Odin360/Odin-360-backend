package com.Odin360.services;

import com.Odin360.Domains.Dtos.CreateTeamDto;
import com.Odin360.Domains.entities.Team;
import com.Odin360.Domains.entities.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.Set;

@Service
public interface TeamService {
    Team createTeam (CreateTeamDto createTeamDto);

    Set<Team> getTeam();

    void deleteTeam(UUID teamId);

    Set<User> getUsers(UUID teamId);
}
