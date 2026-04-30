package com.pharmacy.repository;

import com.pharmacy.entity.InvoiceDetail;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InvoiceDetailRepository {

    public void save(EntityManager em, InvoiceDetail invoiceDetail) {
        try {
            em.persist(invoiceDetail);
        } catch (Exception e) {
            log.error("event=invoice_detail_save_failed errorMessage={}", e.getMessage());
            throw e;
        }
    }

}
