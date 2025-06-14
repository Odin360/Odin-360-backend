package com.Odin360.controllers;


import com.Odin360.Domains.Dtos.CreateTeamDto;
import com.Odin360.Domains.Dtos.TeamResponse;
import com.Odin360.Domains.Dtos.UserDto;
import com.Odin360.Domains.entities.Team;
import com.Odin360.Domains.entities.User;
import com.Odin360.mappers.TeamMapper;
import com.Odin360.mappers.UserMapper;
import com.Odin360.services.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping(path="/api/v1/teams")
public class TeamController {
   private final TeamService teamService;
   private final TeamMapper teamMapper;
   private final UserMapper userMapper;
   @PostMapping("/create")
   public ResponseEntity<TeamResponse> createTeam(@RequestBody CreateTeamDto teamDto){
      Team team = teamService.createTeam(teamDto);
      TeamResponse teamResponse = teamMapper.toTeamResponse(team);
      return new ResponseEntity<>(teamResponse, HttpStatus.CREATED);}
@GetMapping
      public ResponseEntity<Set<TeamResponse>> getTeam(){
      Set<Team> teamList = teamService.getTeam();
      return ResponseEntity.ok(teamList.stream().map(teamMapper::toTeamResponse).collect(Collectors.toSet()));
      }
 @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteTeam(@PathVariable("id") UUID teamId){
       teamService.deleteTeam(teamId);
       return ResponseEntity.ok("Team deleted successfully");
  }
  @GetMapping("/users/{teamId}")
  public ResponseEntity<List<UserDto>> listUsers(@PathVariable UUID teamId){
       Set<User>users=teamService.getUsers(teamId);
       return ResponseEntity.ok(users.stream().map(userMapper::fromUser).toList());
  }
}
