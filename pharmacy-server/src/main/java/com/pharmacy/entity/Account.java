package com.pharmacy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tbl_account")
public class Account extends BaseEntity {

    @Column(name = "user_name", length = 20, unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "otp_key", length = 7)
    private String otpKey;

    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;

    @Column(name = "account_locked", nullable = false)
    private boolean accountLocked = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", referencedColumnName = "id", unique = true)
    private Employee employee;
}
