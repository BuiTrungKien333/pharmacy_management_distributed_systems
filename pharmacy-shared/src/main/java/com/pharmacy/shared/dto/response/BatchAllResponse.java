package com.pharmacy.shared.dto.response;

import com.pharmacy.shared.util.enums.BatchStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatchAllResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String batchNumber;
    private LocalDate manufacturingDate;
    private LocalDate expirationDate;
    private LocalDateTime importDate;
    private int importQuantity;
    private int remainingQuantity;
    private double importPrice;
    private double totalAmount;
    private double sellingPrice;
    private MedicineResponse medicine;
    private SupplierMiniResponse supplier;
    private EmployeeMiniResponse employee;
    private BatchStatus batchStatus;
}
