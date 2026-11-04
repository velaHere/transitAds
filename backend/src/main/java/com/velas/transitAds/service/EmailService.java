package com.velas.transitAds.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender sender;

    @Autowired
    public EmailService(JavaMailSender sender) {
        this.sender = sender;
    }

    @Async("emailExecutor") // --> does not work when this method is called from this class (not works when called from the same class the method exists in)
    public void sendVerificationEmail(String to, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject("Email Verification Code");
        message.setText(
                """
                Thanks for creating account on our site.
                Verification Code: %s
                """.formatted(otp)
        );

        sender.send(message);
    }
}
