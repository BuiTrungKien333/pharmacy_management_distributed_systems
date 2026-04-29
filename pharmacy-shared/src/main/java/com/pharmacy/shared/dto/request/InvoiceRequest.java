package com.pharmacy.shared.dto.request;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Builder
public class InvoiceRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private CustomerRequest customer;

    private EmployeeMiniRequest employee;

    private String voucherCode;

    private double totalGoodsAmount;

    private double totalPayableAmount;

    private String note;

}
