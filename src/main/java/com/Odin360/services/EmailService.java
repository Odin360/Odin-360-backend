package com.Odin360.services;

import jakarta.mail.MessagingException;
import org.springframework.security.core.userdetails.UserDetails;

public interface EmailService {
    void sendEmail(String to, String subject, String text,String from) throws MessagingException;
    String generateVerificationCode();
}
