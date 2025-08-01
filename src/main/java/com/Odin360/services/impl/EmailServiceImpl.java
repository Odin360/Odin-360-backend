package com.Odin360.services.impl;

import com.Odin360.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender emailSender;
    @Override
    public void sendEmail(String to, String subject, String text,String from) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,true);
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text,true);

        emailSender.send(message);

        
    }

    @Override
    public String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(60000) + 20000; // Range: 20000 to 79999
        return String.valueOf(code);
    }

}
