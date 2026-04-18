package com.pharmacy.repository;

import com.pharmacy.entity.Account;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;

public class AccountRepository {

    public Account findByUsername(EntityManager em, String username) {
        List<Account> accounts = em.createQuery(
                        "select a from Account a where a.username = :username",
                        Account.class)
                .setParameter("username", username)
                .setMaxResults(1)
                .getResultList();

        return accounts.isEmpty() ? null : accounts.get(0);
    }

    public Account findByUsernameAndEmail(EntityManager em, String username, String email) {
        List<Account> accounts = em.createQuery(
                        "select a from Account a " +
                                "join a.employee e " +
                                "where a.username = :username and e.email = :email",
                        Account.class)
                .setParameter("username", username)
                .setParameter("email", email)
                .setMaxResults(1)
                .getResultList();

        return accounts.isEmpty() ? null : accounts.get(0);
    }

    public Account findByUsernameAndOtp(EntityManager em, String username, String otp, LocalDateTime now) {
        List<Account> accounts = em.createQuery(
                        "select a from Account a " +
                                "where a.username = :username " +
                                "and a.otpKey = :otp " +
                                "and a.expiryTime is not null " +
                                "and a.expiryTime >= :now",
                        Account.class)
                .setParameter("username", username)
                .setParameter("otp", otp)
                .setParameter("now", now)
                .setMaxResults(1)
                .getResultList();

        return accounts.isEmpty() ? null : accounts.get(0);
    }

    public void saveOtp(EntityManager em, Account account, String otp, LocalDateTime expiryTime) {
        account.setOtpKey(otp);
        account.setExpiryTime(expiryTime);
        em.merge(account);
    }

    public void updatePasswordAndClearOtp(EntityManager em, Account account, String hashedPassword) {
        account.setPassword(hashedPassword);
        account.setOtpKey(null);
        account.setExpiryTime(null);
        em.merge(account);
    }
}
