package com.pharmacy.repository;

import com.pharmacy.shared.dto.response.SupplierMiniResponse;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class SupplierRepository {

    public List<SupplierMiniResponse> findAllMini(EntityManager em) {
        try {
            return em.createQuery(
                            "select new com.pharmacy.shared.dto.response.SupplierMiniResponse(" +
                                    "s.id, s.factoryCode, s.supplierName) " +
                                    "from Supplier s order by s.supplierName asc",
                            SupplierMiniResponse.class)
                    .getResultList();
        } catch (Exception e) {
            log.error("event=supplier_find_all_mini_failed errorMessage={}", e.getMessage());
            throw e;
        }
    }
}
