package com.pharmacy.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DataMapper {
    DataMapper INSTANCE = Mappers.getMapper(DataMapper.class);

//    MedicineDTO toDTO(Medicine entity);
//    Medicine toEntity(MedicineDTO dto);
//    List<MedicineDTO> toMedicineDTOs(List<Medicine> entities);
//
//    InvoiceDTO toDTO(Invoice entity);
//    Invoice toEntity(InvoiceDTO dto);
//
//    BatchResponse toDTO(Batch entity);
//    Batch toEntity(BatchResponse dto);
}
