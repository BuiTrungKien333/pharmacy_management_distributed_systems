package com.pharmacy.shared.dto.response;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class CustomerResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String fullName;
    private String phoneNumber;
}
