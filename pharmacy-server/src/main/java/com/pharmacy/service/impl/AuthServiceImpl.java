package com.pharmacy.service.impl;

import com.pharmacy.config.PasswordHasher;
import com.pharmacy.config.SecurityContextHolder;
import com.pharmacy.entity.Account;
import com.pharmacy.persistence.JpaTransactionTemplate;
import com.pharmacy.repository.AccountRepository;
import com.pharmacy.repository.AuthorizationRepository;
import com.pharmacy.shared.service.AuthService;
import com.pharmacy.util.EmailUtil;
import com.pharmacy.util.OTPUtil;
import com.pharmacy.util.TemplateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int OTP_EXPIRE_SECONDS = 300;

    private final AccountRepository accountRepository;
    private final AuthorizationRepository authorizationRepository;

    @Override
    public boolean forgotPassword(String username, String email) {
        if (username == null || username.isBlank() || email == null || email.isBlank()) {
            throw new IllegalArgumentException("Username và email không được để trống");
        }

        // 1. Process the Database Transaction very quickly
        String htmlContent = JpaTransactionTemplate.execute(em -> {
            Account account = accountRepository.findByUsernameAndEmail(em, username, email);
            if (account == null) {
                log.info("event=forgot_password_failed reason=user_or_email_not_found username={}", username);
                throw new SecurityException("Mã nhân viên hoặc email không chính xác, hoặc tài khoản không tồn tại.");
            }

            String otp = OTPUtil.generateOTP();
            LocalDateTime expiryTime = LocalDateTime.now().plusSeconds(OTP_EXPIRE_SECONDS);

            // Save OTP to DB
            accountRepository.saveOtp(em, account, otp, expiryTime);

            // Load the HTML template but DO NOT SEND YET
            return TemplateUtil.loadTemplate("templates/send-email-forgot-password.html")
                    .replace("{{user}}", username)
                    .replace("{{OTP}}", otp);
        });

        // 2. Send Email outside the Transaction
        Thread.startVirtualThread(() -> {
            try {
                EmailUtil.sendHtmlEmail(email, "Alami Pharmacy - Forgot password", htmlContent);
                log.info("event=forgot_password_email_sent_virtual_thread username={}", username);
            } catch (Exception e) {
                log.error("event=forgot_password_email_failed_virtual_thread username={} error={}", username, e.getMessage());
            }
        });

        return true;
    }

    @Override
    public boolean verifyOTP(String username, String otp) {
        if (otp == null || otp.isBlank()) {
            throw new IllegalArgumentException("OTP không được để trống");
        }

        return JpaTransactionTemplate.execute(em -> {
            Account account = accountRepository.findByUsernameAndOtp(em, username, otp, LocalDateTime.now());

            if (account == null) {
                log.info("event=otp_verify_failed username={}", username);
                throw new SecurityException("OTP không hợp lệ hoặc đã hết hạn.");
            }

            return true;
        });
    }

    @Override
    public boolean changePassword(String username, String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }

        return JpaTransactionTemplate.execute(em -> {
            Account account = accountRepository.findByUsername(em, username);
            if (account == null) {
                log.info("event=change_password_failed reason=user_not_found username={}", username);
                throw new SecurityException("Tài khoản không tồn tại.");
            }

            String hash = PasswordHasher.hash(password);
            accountRepository.updatePasswordAndClearOtp(em, account, hash);
            log.info("event=password_changed username={}", username);

            return true;
        });
    }

    @Override
    public boolean login(String username, String password) {
        if (username == null || password == null || username.isBlank())
            throw new IllegalArgumentException("Username, password not null or empty");

        return JpaTransactionTemplate.execute(em -> {
            Account account = accountRepository.findByUsername(em, username.trim());

            if (account == null) {
                log.info("event=auth_failed reason=user_not_found userName={}", username);
                throw new SecurityException("Tài khoản hoặc mật khẩu không chính xác!");
            }

            boolean ok = PasswordHasher.matches(password, account.getPassword());
            if (!ok) {
                log.info("event=auth_failed reason=invalid_password userName={}", username);
                throw new SecurityException("Tài khoản hoặc mật khẩu không chính xác!");
            }

            if (account.isAccountLocked()) {
                log.info("event=auth_failed reason=account_locked userName={}", username);
                throw new SecurityException("Tài khoản đã bị khóa, vui lòng liên hệ Admin");
            }

            Set<String> userPermissions = authorizationRepository.findPermissionKeysForUser(em, username);
            SecurityContextHolder.setLoginSession(userPermissions);

            return true;
        });
    }
}
