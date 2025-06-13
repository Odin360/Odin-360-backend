package com.Odin360.services;

import com.Odin360.Domains.Dtos.CreateTeamDto;
import com.Odin360.Domains.entities.Team;

import java.util.List;
import java.util.UUID;

public interface TeamService {
    Team createTeam (CreateTeamDto createTeamDto);

    List<Team> getTeam();

    void deleteTeam(UUID teamId);
}
