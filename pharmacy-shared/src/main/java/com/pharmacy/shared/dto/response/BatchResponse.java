package com.pharmacy.shared.dto.response;

import com.pharmacy.shared.util.enums.BatchStatus;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BatchResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String batchNumber;
    private LocalDate expirationDate;
    private LocalDateTime importDate;
    private int importQuantity;
    private int remainingQuantity;
    private double sellingPrice;
    private BatchStatus batchStatus;
    private MedicineMiniResponse medicine;
}
