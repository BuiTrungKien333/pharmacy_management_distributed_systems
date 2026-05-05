package com.pharmacy.shared.dto.response;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class InvoiceDetailResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private int quantity;
    private double unitPrice;
    private double totalAmount;
    private BatchResponse batch;
    private MedicineMiniResponse medicine;
    private String invoiceId;

    public void setTotalAmount() {
        this.totalAmount = this.quantity * this.unitPrice;
    }

}
