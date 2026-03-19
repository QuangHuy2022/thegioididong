package com.example.demo_3001.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final Map<String, String> otpStorage = new HashMap<>();

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public String generateOTP(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(email, otp);
        return otp;
    }

    public void sendOTP(String to, String otp) {
        System.out.println("OTP for " + to + " is: " + otp); // Luôn log ra console để dễ test
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Mã xác nhận OTP đổi Voucher");
            message.setText("Mã OTP của bạn là: " + otp + ". Mã này có hiệu lực trong 5 phút.");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send real email to " + to + ": " + e.getMessage());
            // Không throw exception ở đây để user vẫn có thể dùng OTP từ console
        }
    }

    public boolean validateOTP(String email, String otp) {
        return otp.equals(otpStorage.get(email));
    }

    public void clearOTP(String email) {
        otpStorage.remove(email);
    }
}
