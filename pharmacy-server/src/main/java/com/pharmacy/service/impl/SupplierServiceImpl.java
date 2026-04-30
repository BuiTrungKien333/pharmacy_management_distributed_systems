package com.pharmacy.service.impl;

import com.pharmacy.persistence.JpaTransactionTemplate;
import com.pharmacy.repository.SupplierRepository;
import com.pharmacy.shared.dto.response.SupplierMiniResponse;
import com.pharmacy.shared.service.SupplierService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    @Override
    public List<SupplierMiniResponse> getAllSupplier() {
        return JpaTransactionTemplate.execute(em -> supplierRepository.findAllMini(em));
    }
}
