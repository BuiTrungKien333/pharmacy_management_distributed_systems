package com.pharmacy.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "tbl_supplier")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "factory_code", length = 100)
    private String factoryCode;

    @Column(name = "supplier_name", columnDefinition = "VARCHAR(255)")
    private String supplierName;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "tax_code", length = 50)
    private String taxCode;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

}
