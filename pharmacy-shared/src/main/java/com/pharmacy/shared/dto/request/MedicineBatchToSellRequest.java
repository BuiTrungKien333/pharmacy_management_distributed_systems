package com.pharmacy.shared.dto.request;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class MedicineBatchToSellRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String medicineName;
    private String barcode;
    private int totalQuantity;
    private String avatarUrl;
    private String measuringUnit;
    private LocalDateTime deletedAt;
    private int sellingQuantity;
    private double sellingPrice;
    private double totalAmount;

    public void setTotalAmount() {
        this.totalAmount = this.sellingQuantity * this.sellingPrice;
    }

    private List<BatchSellRequest> batchSellRequestList;

    private List<BatchDistributionRequest> batchDistributionRequestList;
}
