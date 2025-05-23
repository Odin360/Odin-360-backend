package com.Odin360.services;

import jakarta.mail.MessagingException;
import org.springframework.security.core.userdetails.UserDetails;

public interface EmailService {
    void sendVerificationEmail(String to, String subject, String text) throws MessagingException;
    String generateVerificationCode();
}
