package com.Odin360.services.impl;

import com.Odin360.Domains.Dtos.CreateTeamDto;
import com.Odin360.Domains.entities.Team;
import com.Odin360.mappers.TeamMapper;
import com.Odin360.repositories.TeamRepository;
import com.Odin360.services.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {
   private final TeamMapper teamMapper;
   private final TeamRepository teamRepository;
    @Override
    public Team createTeam(CreateTeamDto createTeamDto) {
        Team team = teamMapper.fromCreateTeamDto(createTeamDto);
        return  teamRepository.save(team);
    }
}
