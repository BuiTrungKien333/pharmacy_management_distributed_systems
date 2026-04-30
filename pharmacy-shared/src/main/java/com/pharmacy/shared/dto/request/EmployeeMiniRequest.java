package com.pharmacy.shared.dto.request;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class EmployeeMiniRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String fullName;
}
