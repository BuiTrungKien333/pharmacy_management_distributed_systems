package com.pharmacy.shared.service;

import java.util.Set;

public interface AuthService {
    boolean login(String username, String password);

    boolean forgotPassword(String username, String email);

    boolean verifyOTP(String username, String otp);

    boolean changePassword(String username, String password);

    Set<String> getCurrentPermissions();
}