package com.pharmacy.shared.dto.request;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class BatchDistributionRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long medicineId;
    private String medicineName;
    private Long batchId;
    private String batchNumber;
    private int sellingQuantity;
    private int remainingQuantity;
    private double sellingPrice;
    private LocalDate expirationDate;
    private double originalPrice;
}
