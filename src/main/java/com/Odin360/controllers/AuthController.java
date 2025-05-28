package com.Odin360.controllers;

import com.Odin360.Domains.Dtos.*;
import com.Odin360.Domains.entities.User;
import com.Odin360.Security.OdinUserDetails;
import com.Odin360.mappers.UserMapper;
import com.Odin360.services.AuthenticationService;
import com.Odin360.services.EmailService;
import com.Odin360.services.UserService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

  @PostMapping("/login")
    public ResponseEntity<AuthResponse> login (@RequestBody LoginRequest loginRequest){
        UserDetails user = authenticationService.authenticate(
                loginRequest.getEmail(),
                loginRequest.getPassword());
        AuthResponse authResponse = AuthResponse.builder()
                .token(authenticationService.generateToken(user))
                .expiresIn(86400) //24hours in seconds
                .build();
        return ResponseEntity.ok(authResponse);
    }
    @PostMapping("/signUp")
    public ResponseEntity<CreateUserDto> createUser(@RequestBody  CreateUserDto createUserDto) throws MessagingException {
        User savedUser = userService.createUser(createUserDto);
        savedUser.setEnabled(false);
        OdinUserDetails userDetails = new OdinUserDetails(savedUser);
        if(!userDetails.isEnabled()){
        authenticationService.sendVerificationEmail(savedUser);
        }
        CreateUserDto savedCreateUserDto = userMapper.toDto(savedUser);
        return new ResponseEntity<>(savedCreateUserDto, HttpStatus.CREATED);
    }
    @PostMapping("/verifyEmail")
    public ResponseEntity<String> verifyEmail(@RequestBody EmailDto emailDto) throws MessagingException {
   User user = userService.findByEmail(emailDto);
   authenticationService.sendVerificationEmail(user);
   return ResponseEntity.ok("Email sent successfully");
    }
    @PostMapping("/resetPassword")
    public ResponseEntity<String> updatePasswordByEmail(@RequestBody UserPasswordDto passwordDto){
        userService.updatePasswordByEmail(passwordDto);
        return ResponseEntity.ok("Password deleted successfully");
    }
    @PostMapping("/verifyUser")
    public ResponseEntity<String> verifyUser(@RequestBody OtpDto otpDto){
      authenticationService.verifyUser(otpDto);
      return  ResponseEntity.ok("Verification successful");
    }
    @PostMapping("/resendOtp")
    public ResponseEntity<String> resendVerificationCode(@RequestBody EmailDto emailDto){
        authenticationService.resendVerificationCode(emailDto);
        return  ResponseEntity.ok("Verification Code resent successfully");
    }
}
