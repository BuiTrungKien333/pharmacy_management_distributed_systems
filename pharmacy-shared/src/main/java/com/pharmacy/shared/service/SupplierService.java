package com.pharmacy.shared.service;

import com.pharmacy.shared.dto.response.SupplierMiniResponse;

import java.util.List;

public interface SupplierService {
    List<SupplierMiniResponse> getAllSupplier();
}
