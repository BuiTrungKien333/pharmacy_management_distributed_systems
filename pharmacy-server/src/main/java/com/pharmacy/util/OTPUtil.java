package com.pharmacy.util;

import java.security.SecureRandom;

public class OTPUtil {
    private static final SecureRandom secureRandom = new SecureRandom();

    public static String generateOTP() {
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }
}
