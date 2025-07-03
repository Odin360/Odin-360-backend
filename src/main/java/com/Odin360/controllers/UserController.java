package com.Odin360.controllers;

import com.Odin360.Domains.Dtos.*;
import com.Odin360.Domains.entities.Team;
import com.Odin360.Domains.entities.User;
import com.Odin360.mappers.TeamMapper;
import com.Odin360.mappers.UserMapper;
import com.Odin360.services.StreamService;
import com.Odin360.services.UserService;
import lombok.RequiredArgsConstructor;
import org.eclipse.angus.mail.iap.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final TeamMapper teamMapper;
    private final StreamService streamService;

//create user

 //get user by id
    @GetMapping("/{id}")
    public ResponseEntity<CreateUserDto> getUserById(@PathVariable UUID id){
        User retrievedUser = userService.getUserById(id);
        CreateUserDto retrievedUserDto = userMapper.toDto(retrievedUser);
        return ResponseEntity.ok(retrievedUserDto);
    }
    @PutMapping("/{userId}/{teamId}")
public ResponseEntity<UserDto> joinTeam(@PathVariable UUID userId,@PathVariable UUID teamId){
        User user = userService.joinTeam(userId,teamId);
        return ResponseEntity.ok(userMapper.fromUser(user));
}
  @PostMapping("/user")
 public ResponseEntity<UserDto> getByEmail(@RequestBody EmailDto emailDto){
        User user = userService.findByEmail(emailDto);
        UserDto userDto = userMapper.fromUser(user);
        return ResponseEntity.ok(userDto);
    }
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteById(@PathVariable UUID userId){
        userService.deleteById(userId);
        return ResponseEntity.ok("User deleted successfully");
    }
    @GetMapping("/teams/{userId}")
    public ResponseEntity<Set<TeamResponse>> getTeams(@PathVariable UUID userId){
        Set<Team> teams = userService.getTeams(userId);
        return ResponseEntity.ok(teams.stream().map(teamMapper::toTeamResponse).collect(Collectors.toSet()));
    }
    @GetMapping("/generateToken/{userId}")
    public ResponseEntity<String> generateClientToken(@PathVariable String userId){
        return ResponseEntity.ok(streamService.clientToken(userId));
    }
    @GetMapping("/generateRandomUUID")
    public ResponseEntity<UUID>  generateRandomUUID(){
        return ResponseEntity.ok(userService.generateRandomUUID());
    }
}
