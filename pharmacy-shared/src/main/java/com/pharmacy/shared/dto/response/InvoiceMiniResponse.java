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
public class InvoiceMiniResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String invoiceCode;
    private LocalDateTime createdDate;

}
