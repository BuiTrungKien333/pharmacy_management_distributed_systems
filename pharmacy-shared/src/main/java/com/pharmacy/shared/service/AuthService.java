package com.pharmacy.shared.service;

public interface AuthService {
    boolean login(String username, String password);

    boolean forgotPassword(String username, String email);

    boolean verifyOTP(String username, String otp);

    boolean changePassword(String username, String password);
}