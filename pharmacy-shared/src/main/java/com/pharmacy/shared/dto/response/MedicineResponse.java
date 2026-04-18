package com.pharmacy.shared.dto.response;

import com.pharmacy.shared.util.enums.MedicineType;
import lombok.*;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
public class MedicineResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String barcode;
    private String medicineName;
    private String measuringUnit;
    private String avatarUrl;
    private int totalQuantity;
    private MedicineType medicineType;
}
