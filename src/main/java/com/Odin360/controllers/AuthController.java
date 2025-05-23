package com.Odin360.controllers;

import com.Odin360.Domains.Dtos.AuthResponse;
import com.Odin360.Domains.Dtos.LoginRequest;
import com.Odin360.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;
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
}
