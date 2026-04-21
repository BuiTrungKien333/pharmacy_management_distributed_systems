package com.pharmacy.shared.dto.response;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MedicineMiniResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String barcode;
    private String medicineName;
    private String measuringUnit;
}
