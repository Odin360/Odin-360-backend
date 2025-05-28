package com.Odin360.services;

import com.Odin360.Domains.Dtos.EmailDto;
import com.Odin360.Domains.Dtos.OtpDto;
import com.Odin360.Domains.entities.User;
import jakarta.mail.MessagingException;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthenticationService {
    UserDetails authenticate(String email, String password);
    String generateToken(UserDetails userDetails);
    UserDetails validateToken(String token);
    void sendVerificationEmail(User user) throws MessagingException;

    void verifyUser(OtpDto otpDto);

    void resendVerificationCode(EmailDto emailDto);
}
