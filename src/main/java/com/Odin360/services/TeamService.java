package com.Odin360.services;

import com.Odin360.Domains.Dtos.CreateTeamDto;
import com.Odin360.Domains.entities.Team;

public interface TeamService {
    Team createTeam (CreateTeamDto createTeamDto);
}
