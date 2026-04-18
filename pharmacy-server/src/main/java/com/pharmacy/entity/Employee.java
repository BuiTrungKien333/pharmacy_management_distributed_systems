package com.pharmacy.entity;


import com.pharmacy.shared.util.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "store")
@Entity
@Table(name = "tbl_employee")
public class Employee extends BaseEntity {

    @Column(name = "employee_code", length = 20, unique = true)
    private String employeeCode;

    @Column(name = "full_name", columnDefinition = "VARCHAR(255)")
    private String fullName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 50)
    private Gender gender;

    @Column(name = "address", columnDefinition = "VARCHAR(200)")
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_code")
    private Store store;

    @Column(name = "avatar_url", length = 100)
    private String avatarUrl;

    @Column(name = "join_date")
    private LocalDate joinDate;

    @ManyToMany
    @JoinTable(
            name = "tbl_user_role",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "employee_code"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToOne(mappedBy = "employee", fetch = FetchType.LAZY)
    private Account account;
}
