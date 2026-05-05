package com.pharmacy.shared.dto.response;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class InvoiceRefundResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String returnInvoiceCode;
    private LocalDateTime createdDate;
    private double refundAmount;
    private String reason;
    private boolean approved;
    private CustomerResponse customer;
    private InvoiceMiniResponse invoice;
    private EmployeeMiniResponse employee;
}
