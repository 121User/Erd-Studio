package com.example.project.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendEmailConfirmationCode(String toAddress, int code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("er.diagram.studio@gmail.com");
        message.setTo(toAddress);
        message.setSubject("Код подтверждения электронной почты");
        message.setText("Добро пожаловать в Erd-Studio!\nВаш код для подтверждения Email: " + code);
        javaMailSender.send(message);
    }

    public void sendPasswordRecoveryCode(String toAddress, int code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("er.diagram.studio@gmail.com");
        message.setTo(toAddress);
        message.setSubject("Код для восстановления пароля");
        message.setText("Ваш код для восстановления пароля: " + code);
        javaMailSender.send(message);
    }
}