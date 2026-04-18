package com.pharmacy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "tbl_store")
public class Store {

    @Id
    @Column(name = "store_code", length = 50)
    private String storeCode;

    @Column(name = "store_name", columnDefinition = "VARCHAR(255)")
    private String storeName;

    @Column(name = "registration_number", length = 100)
    private String registrationNumber;

    @Column(name = "certification_number", length = 100)
    private String certificationNumber;

    @Column(name = "address", columnDefinition = "VARCHAR(255)")
    private String address;

}
