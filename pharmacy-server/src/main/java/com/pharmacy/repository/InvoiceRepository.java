package com.pharmacy.repository;

import com.pharmacy.entity.Invoice;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InvoiceRepository {

    public void save(EntityManager em, Invoice invoice) {
        try {
            em.persist(invoice);
        } catch (Exception e) {
            log.error("event=invoice_save_failed errorMessage={}", e.getMessage());
            throw e;
        }
    }

}
