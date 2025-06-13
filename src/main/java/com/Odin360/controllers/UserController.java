package com.Odin360.controllers;

import com.Odin360.Domains.Dtos.CreateUserDto;
import com.Odin360.Domains.Dtos.EmailDto;
import com.Odin360.Domains.Dtos.UserDto;
import com.Odin360.Domains.Dtos.UserPasswordDto;
import com.Odin360.Domains.entities.User;
import com.Odin360.mappers.UserMapper;
import com.Odin360.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    public final UserMapper userMapper;
//create user

 //get user by id
    @GetMapping("/{id}")
    public ResponseEntity<CreateUserDto> getUserById(@PathVariable UUID id){
        User retrievedUser = userService.getUserById(id);
        CreateUserDto retrievedUserDto = userMapper.toDto(retrievedUser);
        return ResponseEntity.ok(retrievedUserDto);
    }
    @PutMapping("/{userId}/{teamId}")
public ResponseEntity<String> joinTeam(@PathVariable UUID userId,@PathVariable UUID teamId){
        userService.joinTeam(userId,teamId);
        return ResponseEntity.ok("Team has been joined successfully");
}
  @PostMapping("/user")
 public ResponseEntity<UserDto> getByEmail(@RequestBody EmailDto emailDto){
        User user = userService.findByEmail(emailDto);
        UserDto userDto = userMapper.fromUser(user);
        return ResponseEntity.ok(userDto);
    }
}
