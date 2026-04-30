package com.pharmacy.shared.dto.response;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class InvoiceResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String invoiceCode;

    private CustomerResponse customer;

    private EmployeeMiniResponse employee;

    private LocalDateTime createdDate;

    private double totalGoodsAmount;

    private double totalPayableAmount;

    private boolean returned;

}
