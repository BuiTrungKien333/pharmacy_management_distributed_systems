package com.pharmacy.shared.dto.response;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeMiniResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String employeeCode;
    private String fullName;

    public String getDisplayName() {
        return employeeCode + " - " + fullName;
    }
}
