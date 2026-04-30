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
public class BatchSellRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long batchId;
    private String batchNumber;
    private LocalDate expirationDate;
    private int remainingQuantity;
    private double sellingPrice;

}
