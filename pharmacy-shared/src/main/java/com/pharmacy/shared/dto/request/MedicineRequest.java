package com.pharmacy.shared.dto.request;

import com.pharmacy.shared.util.enums.MedicineType;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String barcode;

    private String medicineName;

    private String nameWithoutAccents;

    private String activeIngredients;

    private String dosageForm;

    private String administrationRoute;

    private String indications;

    private String contraindications;

    private String dosage;

    private String registrationNumber;

    private String manufacturingCountry;

    private String manufacturer;

    private String qualityStandard;

    private String packagingSpecification;

    private String measuringUnit;

    private String description;

    private String avatarUrl;

    private Integer totalQuantity;

    private LocalDateTime deletedAt;

    private MedicineType medicineType;
}
