package com.Odin360.services.impl;

import com.Odin360.Domains.Dtos.CreateTeamDto;
import com.Odin360.Domains.entities.Team;
import com.Odin360.mappers.TeamMapper;
import com.Odin360.repositories.TeamRepository;
import com.Odin360.services.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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

    @Override
    public List<Team> getTeam() {
        return teamRepository.findAll();
    }

    @Override
    public void deleteTeam(UUID teamId) {
       if(teamRepository.existsById(teamId)){
           teamRepository.deleteById(teamId);
       }
       else{
           throw new RuntimeException("User with id "+teamId+",does not exist");
       }
    }
}
