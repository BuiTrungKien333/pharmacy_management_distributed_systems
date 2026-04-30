package com.pharmacy.shared.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplierMiniResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String factoryCode;
    private String supplierName;

    public String toString() {
        String str = String.format("%s - %s", factoryCode, supplierName);
        return str.length() > 90 ? str.substring(0, 90) + "..." : str;
    }
}
