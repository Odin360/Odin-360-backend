package com.Odin360.controllers;


import com.Odin360.Domains.Dtos.CreateTeamDto;
import com.Odin360.Domains.Dtos.TeamResponse;
import com.Odin360.Domains.entities.Team;
import com.Odin360.mappers.TeamMapper;
import com.Odin360.services.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TeamController {
   private final TeamService teamService;
   private final TeamMapper teamMapper;
   @PostMapping
   public ResponseEntity<TeamResponse> createTeam(@RequestBody CreateTeamDto teamDto){
      Team team = teamService.createTeam(teamDto);
      TeamResponse teamResponse = teamMapper.toTeamResponse(team);
      return new ResponseEntity<>(teamResponse, HttpStatus.CREATED);

   }
}
