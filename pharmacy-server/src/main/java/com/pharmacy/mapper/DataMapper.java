package com.pharmacy.mapper;

import com.pharmacy.entity.Medicine;
import com.pharmacy.shared.dto.request.MedicineRequest;
import com.pharmacy.shared.dto.response.MedicineResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface DataMapper {

    DataMapper INSTANCE = Mappers.getMapper(DataMapper.class);

    @Mapping(target = "totalQuantity", expression = "java(request.getTotalQuantity() == null ? 0 : request.getTotalQuantity())")
    Medicine toEntity(MedicineRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "barcode", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "totalQuantity", expression = "java(request.getTotalQuantity() == null ? 0 : request.getTotalQuantity())")
    void updateEntityFromRequest(MedicineRequest request, @MappingTarget Medicine medicine);

    @Mapping(target = "totalQuantity", expression = "java(medicine.getTotalQuantity() == null ? 0 : medicine.getTotalQuantity())")
    MedicineResponse toMedicineResponse(Medicine medicine);

    List<MedicineResponse> toMedicineResponses(List<Medicine> medicines);
}
