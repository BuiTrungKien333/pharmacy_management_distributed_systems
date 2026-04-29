package com.pharmacy.shared.dto.request;

import com.pharmacy.shared.util.enums.BatchStatus;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BatchUpdateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private LocalDate manufacturingDate;
    private LocalDate expirationDate;
    private int importQuantity;
    private int remainingQuantity;
    private double importPrice;
    private double totalAmount;
    private double sellingPrice;
    private Long medicineId;
    private Long supplierId;
    private Long employeeId;
    private BatchStatus batchStatus;
}
