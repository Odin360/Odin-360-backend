package com.Odin360.controllers;

import com.Odin360.Domains.Dtos.CreateUserDto;
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
    @PostMapping
    public ResponseEntity<CreateUserDto> createUser(@RequestBody  CreateUserDto createUserDto){
        User savedUser = userService.createUser(createUserDto);
        CreateUserDto savedCreateUserDto = userMapper.toDto(savedUser);
                return new ResponseEntity<>(savedCreateUserDto, HttpStatus.CREATED);
    }
 //get user by id
    @GetMapping("{id}")
    public ResponseEntity<CreateUserDto> getUserById(@PathVariable("id") UUID id){
        User retrievedUser = userService.getUserById(id);
        CreateUserDto retrievedUserDto = userMapper.toDto(retrievedUser);
        return ResponseEntity.ok(retrievedUserDto);
    }
 //update password
    @PostMapping("/resetPassword")
    public ResponseEntity<String> updatePasswordByEmail(@RequestBody  UserPasswordDto passwordDto){
        userService.updatePasswordByEmail(passwordDto);
        return ResponseEntity.ok("Password deleted successfully");
    }
}
