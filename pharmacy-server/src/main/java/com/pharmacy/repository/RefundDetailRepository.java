package com.pharmacy.repository;

import com.pharmacy.entity.InvoiceDetailReturn;
import com.pharmacy.shared.dto.request.InvoiceDetailRefundRequest;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RefundDetailRepository {

    public void save(EntityManager em, InvoiceDetailReturn invoiceDetailReturn) {
        try {
            em.persist(invoiceDetailReturn);
        } catch (Exception e) {
            log.error("event=invoice_detail_refund_save_failed errorMessage={}", e.getMessage());
            throw e;
        }
    }

    public void update(EntityManager em, InvoiceDetailRefundRequest request) {
        String jpql = "update InvoiceDetailReturn i set i.status = true, reason = :reason, i.resolution =:resolution where i.id =:id";
        em.createQuery(jpql).setParameter("id", request.getId()).executeUpdate();
    }
}
