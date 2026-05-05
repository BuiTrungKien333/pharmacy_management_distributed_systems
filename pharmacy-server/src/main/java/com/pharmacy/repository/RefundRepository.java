package com.pharmacy.repository;

import com.pharmacy.entity.InvoiceReturn;
import com.pharmacy.shared.dto.response.*;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RefundRepository {

    public void save(EntityManager em, InvoiceReturn invoiceReturn) {
        try {
            em.persist(invoiceReturn);
        } catch (Exception e) {
            log.error("event=invoice_return_save_failed errorMessage={}", e.getMessage());
            throw e;
        }
    }

    public InvoiceResponse findInvoiceByCode(EntityManager em, String invoiceCode) {
        try {
            List<Object[]> rows = em.createNativeQuery("""
                            select i.invoice_code,
                                   i.created_date,
                                   i.is_returned,
                                   c.id,
                                   c.full_name,
                                   c.phone_number
                            from tbl_invoice i
                            left join tbl_customer c on i.customer_id = c.id
                            where i.invoice_code = :invoiceCode
                            """)
                    .setParameter("invoiceCode", invoiceCode)
                    .setMaxResults(1)
                    .getResultList();

            if (rows.isEmpty()) {
                return null;
            }

            Object[] row = rows.get(0);

            LocalDateTime createdDate = null;
            Object createdObj = row[1];
            if (createdObj instanceof Timestamp) {
                createdDate = ((Timestamp) createdObj).toLocalDateTime();
            } else if (createdObj instanceof LocalDateTime) {
                createdDate = (LocalDateTime) createdObj;
            }

            CustomerResponse customer = null;
            if (row[3] != null) {
                customer = CustomerResponse.builder()
                        .id(((Number) row[3]).longValue())
                        .fullName((String) row[4])
                        .phoneNumber((String) row[5])
                        .build();
            }

            return InvoiceResponse.builder()
                    .invoiceCode((String) row[0])
                    .createdDate(createdDate)
                    .returned(Boolean.TRUE.equals(row[2]))
                    .customer(customer)
                    .build();
        } catch (Exception e) {
            log.error("event=refund_find_invoice_by_code_failed invoiceCode={} errorMessage={}", invoiceCode, e.getMessage());
            throw e;
        }
    }

    public List<InvoiceDetailResponse> findInvoiceDetailsByCode(EntityManager em, String invoiceCode) {
        try {
            List<Object[]> rows = em.createNativeQuery("""
                            select d.id,
                                   d.quantity,
                                   d.unit_price,
                                   d.total_amount,
                                   d.batch_number,
                                   d.invoice_code,
                                   m.id as medicine_id,
                                   m.barcode,
                                   m.medicine_name,
                                   m.measuring_unit,
                                   b.batch_number
                            from tbl_invoice_detail d
                            join tbl_batch b on d.batch_number = b.id
                            join tbl_medicine m on d.medicine_id = m.id
                            where d.invoice_code = :invoiceCode
                            """)
                    .setParameter("invoiceCode", invoiceCode)
                    .getResultList();

            List<InvoiceDetailResponse> result = new ArrayList<>();
            for (Object[] row : rows) {
                MedicineMiniResponse medicine = new MedicineMiniResponse(
                        ((Number) row[6]).longValue(),
                        (String) row[7],
                        (String) row[8],
                        (String) row[9]
                );

                InvoiceDetailResponse detail = new InvoiceDetailResponse();
                detail.setId(((Number) row[0]).longValue());
                detail.setQuantity(((Number) row[1]).intValue());
                detail.setUnitPrice(((Number) row[2]).doubleValue());
                detail.setTotalAmount(((Number) row[3]).doubleValue());
                detail.setBatch(BatchResponse.builder()
                        .id(((Number) row[4]).longValue())
                        .batchNumber((String) row[10])
                        .build());
                detail.setMedicine(medicine);
                detail.setInvoiceId((String) row[5]);

                result.add(detail);
            }

            return result;
        } catch (Exception e) {
            log.error("event=refund_find_invoice_detail_failed invoiceCode={} errorMessage={}", invoiceCode, e.getMessage());
            throw e;
        }
    }

    public void updateStatusReturnOfInvoice(EntityManager em, String invoiceCode) {
        String jpql = "update Invoice i set i.returned = true where i.invoiceCode =: invoiceCode";
        try {
            em.createQuery(jpql).setParameter("invoiceCode", invoiceCode).executeUpdate();
        } catch (Exception e) {
            log.error("event=refund_update_invoice_return_status_failed invoiceCode={} errorMessage={}", invoiceCode, e.getMessage());
            throw e;
        }
    }

    public void updateStatusApporvOfInvoiceRefund(EntityManager em, String invoiceRefundCode) {
        String jpql = "update InvoiceReturn i set i.approved = true where i.returnInvoiceCode =: invoiceRefundCode";
        try {
            em.createQuery(jpql).setParameter("invoiceRefundCode", invoiceRefundCode).executeUpdate();
        } catch (Exception e) {
            log.error("event=refund_update_approve_status_failed invoiceRefundCode={} errorMessage={}", invoiceRefundCode, e.getMessage());
            throw e;
        }
    }
}
