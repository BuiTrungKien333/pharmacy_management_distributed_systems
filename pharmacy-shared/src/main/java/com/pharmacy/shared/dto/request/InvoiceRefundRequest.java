package com.pharmacy.shared.dto.request;

import com.pharmacy.shared.dto.response.CustomerResponse;
import com.pharmacy.shared.dto.response.EmployeeMiniResponse;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class InvoiceRefundRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String returnInvoiceCode;

    private double refundAmount;

    private String reason;

    private String invoiceCode;

    private CustomerResponse customer;

    private EmployeeMiniResponse employee;

}
