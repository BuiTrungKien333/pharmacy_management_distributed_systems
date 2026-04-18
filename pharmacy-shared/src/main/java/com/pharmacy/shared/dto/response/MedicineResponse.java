package com.pharmacy.shared.dto.response;

import com.pharmacy.shared.util.enums.MedicineType;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
public class MedicineResponse {
    private Long id;
    private String barcode;
    private String medicineName;
    private String measuringUnit;
    private String avatarUrl;
    private int totalQuantity;
    private MedicineType medicineType;
}
