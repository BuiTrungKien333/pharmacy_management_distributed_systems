package com.pharmacy.shared.dto.response;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

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

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        MedicineMiniResponse that = (MedicineMiniResponse) object;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
