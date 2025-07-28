package com.Odin360.services.impl;

import com.Odin360.Domains.Dtos.EmailDto;
import com.Odin360.Domains.Dtos.OtpDto;
import com.Odin360.Domains.entities.User;
import com.Odin360.repositories.UserRepository;
import com.Odin360.services.AuthenticationService;
import com.Odin360.services.EmailService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final Long jwtExpiryMs = 86400000L;
    private final EmailService emailService;
    @Value("${jwt.secret}")
    private String securityKey;
    @Override
    public UserDetails authenticate(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email,password)
        );
        return userDetailsService.loadUserByUsername(email);
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        Map<String,Object> claims = new HashMap<>();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiryMs))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();

    }

    @Override
    public UserDetails validateToken(String token) {
        String username = extractUsername(token);
        return userDetailsService.loadUserByUsername(username);
    }

    @Override
    public void sendVerificationEmail(User user) throws MessagingException {

        String code = emailService.generateVerificationCode();
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));
        user.setVerificationCode(code);
        userRepository.save(user);
        String subject = "Account Verification";
        String from = "scrivenapp@gmail.com";
        String verificationCode = "VERIFICATION CODE " + code;
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to Scriven</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue💪:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
        try{
            emailService.sendEmail(user.getEmail(), subject, htmlMessage,from);
            log.info("email sent successfully");
        }
        catch (MessagingException e){
            log.error(String.valueOf(e));
        }

    }

    @Override
    public void verifyUser(OtpDto otpDto) {
        User retrievedUser = userRepository.findByEmail(otpDto.getEmail())
                .orElseThrow(()-> new RuntimeException("Account does not exist"));
        if(LocalDateTime.now().isBefore(retrievedUser.getVerificationCodeExpiresAt())){
        if(otpDto.getVerificationCode().equals(retrievedUser.getVerificationCode())){
            retrievedUser.setEnabled(true);
            userRepository.save(retrievedUser);
        }else{
            throw new RuntimeException("You entered the wrong verification code");
        }
        }
        else {
            throw new RuntimeException("Verification code has expired");
        }
    }

    @Override
    public void resendVerificationCode(EmailDto emailDto) {
        User retrievedUser = userRepository.findByEmail(emailDto.getEmail())
                .orElseThrow(()->new RuntimeException("Account does not exist"));
        if (LocalDateTime.now().isBefore(retrievedUser.getVerificationCodeExpiresAt())){
            String code = retrievedUser.getVerificationCode();
            String subject = "Account Verification";
            String from = "scrivenapp@gmail.com";
            String verificationCode = "VERIFICATION CODE " + code;
            String htmlMessage = "<html>"
                    + "<body style=\"font-family: Arial, sans-serif;\">"
                    + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                    + "<h2 style=\"color: #333;\">Welcome to Scriven</h2>"
                    + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                    + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                    + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                    + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                    + "</div>"
                    + "</div>"
                    + "</body>"
                    + "</html>";
            try{
                emailService.sendEmail(retrievedUser.getEmail(), subject, htmlMessage,from);
                log.info("verification code  resent successfully");
            }
            catch (MessagingException e){
                log.error(String.valueOf(e));
            }
        }
    }

    private String extractUsername(String token){
        return extractAllClaims(token).getSubject();
    }
    private Claims extractAllClaims(String token){
        return Jwts.parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey(){
        byte[] keyBytes = securityKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
