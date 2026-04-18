package com.pharmacy.entity;

import com.pharmacy.entity.enums.MedicineType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tbl_medicine")
public class Medicine extends BaseEntity {

    @Column(name = "barcode", unique = true, length = 20)
    private String barcode;

    @Column(name = "medicine_name", columnDefinition = "VARCHAR(100)")
    private String medicineName;

    @Column(name = "name_without_accents", columnDefinition = "VARCHAR(100)")
    private String nameWithoutAccents;

    @Column(name = "active_ingredients", columnDefinition = "VARCHAR(200)")
    private String activeIngredients;

    @Column(name = "dosage_form", columnDefinition = "VARCHAR(100)")
    private String dosageForm;

    @Column(name = "administration_route", columnDefinition = "VARCHAR(55)")
    private String administrationRoute;

    @Column(name = "indications", columnDefinition = "VARCHAR(255)")
    private String indications;

    @Column(name = "contraindications", columnDefinition = "VARCHAR(255)")
    private String contraindications;

    @Column(name = "dosage", columnDefinition = "VARCHAR(255)")
    private String dosage;

    @Column(name = "registration_number", length = 50)
    private String registrationNumber;

    @Column(name = "manufacturing_country", columnDefinition = "VARCHAR(100)")
    private String manufacturingCountry;

    @Column(name = "manufacturer", columnDefinition = "VARCHAR(200)")
    private String manufacturer;

    @Column(name = "quality_standard", columnDefinition = "VARCHAR(55)")
    private String qualityStandard;

    @Column(name = "packaging_specification", columnDefinition = "VARCHAR(100)")
    private String packagingSpecification;

    @Column(name = "measuring_unit", columnDefinition = "VARCHAR(50)")
    private String measuringUnit;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "avatar_url", length = 100)
    private String avatarUrl;

    @Column(name = "total_quantity")
    private Integer totalQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "medicine_type", length = 50)
    private MedicineType medicineType;
}
