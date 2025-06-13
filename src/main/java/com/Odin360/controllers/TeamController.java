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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path="/api/v1/teams")
public class TeamController {
   private final TeamService teamService;
   private final TeamMapper teamMapper;
   @PostMapping("/create")
   public ResponseEntity<TeamResponse> createTeam(@RequestBody CreateTeamDto teamDto){
      Team team = teamService.createTeam(teamDto);
      TeamResponse teamResponse = teamMapper.toTeamResponse(team);
      return new ResponseEntity<>(teamResponse, HttpStatus.CREATED);}
@GetMapping
      public ResponseEntity<List<TeamResponse>> getTeam(){
      List<Team> teamList = teamService.getTeam();
      return ResponseEntity.ok(teamList.stream().map(teamMapper::toTeamResponse).toList());
      }
 @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteTeam(@PathVariable("id") UUID teamId){
       teamService.deleteTeam(teamId);
       return ResponseEntity.ok("Team deleted successfully");
  }
}
