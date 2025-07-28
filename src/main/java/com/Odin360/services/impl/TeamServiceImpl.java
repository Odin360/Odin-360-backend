package com.Odin360.services.impl;

import com.Odin360.Domains.Dtos.CreateTeamDto;
import com.Odin360.Domains.Dtos.TranscriptResponse;
import com.Odin360.Domains.entities.Team;
import com.Odin360.Domains.entities.User;
import com.Odin360.mappers.TeamMapper;
import com.Odin360.repositories.TeamRepository;
import com.Odin360.services.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.Set;

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
    public Set<Team> getTeam() {
        List<Team>teams= teamRepository.findAll();
        return new HashSet<>(teams);
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

    @Override
    public Set<User> getUsers(UUID teamId) {
            Team team = teamRepository.findById(teamId)
                    .orElseThrow(()->new RuntimeException("team not found"));
            return team.getUsers();
    }

    @Override
    public void addTranscript(TranscriptResponse transcriptResponse) {
        Team team = teamRepository.findById(transcriptResponse.getTeamId())
                .orElseThrow(()->new RuntimeException("team not found"));
        team.setMeetingTranscript(team.getMeetingTranscript()+transcriptResponse.getTranscript());
    }

    @Override
    public String getTranscript(UUID teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(()->new RuntimeException("team not found"));
        return team.getMeetingTranscript();
    }
}
