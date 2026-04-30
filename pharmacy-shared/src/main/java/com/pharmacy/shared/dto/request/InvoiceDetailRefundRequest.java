package com.pharmacy.shared.dto.request;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class InvoiceDetailRefundRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String invoiceRefundCode;

    private int quantity;

    private double unitPrice;

    private double totalAmount;

    private Long batchId;

    private Long medicineId;

    private String resolution;

    public void setTotalAmount() {
        this.totalAmount = this.quantity * this.unitPrice;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        InvoiceDetailRefundRequest request = (InvoiceDetailRefundRequest) object;
        return Objects.equals(medicineId, request.medicineId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(medicineId);
    }
}
