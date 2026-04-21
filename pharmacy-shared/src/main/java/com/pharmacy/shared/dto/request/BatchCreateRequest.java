package com.pharmacy.shared.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatchCreateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalDate manufacturingDate;

    private LocalDate expirationDate;

    private int importQuantity;

    private double importPrice;

    private double totalAmount;

    private double sellingPrice;

    private Long medicineId;

    private Long supplierId;

    private Long employeeId;
}
