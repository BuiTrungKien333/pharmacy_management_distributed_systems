package com.pharmacy.repository;

import com.pharmacy.shared.dto.response.*;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class InvoiceRefundRepository {

    public InvoiceRefundResponse findInvoiceRefundByCode(EntityManager em, String invoiceRefundCode) {
        try {
            List<Object[]> rows = em.createNativeQuery("""
                            select r.return_invoice_code,
                                   r.created_date,
                                   r.refund_amount,
                                   r.reason,
                                   r.is_approved,
                                   c.id,
                                   c.full_name,
                                   c.phone_number,
                                   e.id,
                                   e.employee_code,
                                   e.full_name,
                                   i.invoice_code,
                                   i.created_date
                            from tbl_invoice_return r
                            join tbl_customer c on r.customer_id = c.id
                            join tbl_employee e on r.employee_id = e.id
                            join tbl_invoice i on r.invoice_code = i.invoice_code
                            where r.return_invoice_code = :code
                            """)
                    .setParameter("code", invoiceRefundCode)
                    .setMaxResults(1)
                    .getResultList();

            if (rows.isEmpty()) {
                return null;
            }

            return mapRefundRow(rows.get(0));
        } catch (Exception e) {
            log.error("event=invoice_refund_find_by_code_failed code={} errorMessage={}", invoiceRefundCode, e.getMessage());
            throw e;
        }
    }

    public List<InvoiceDetailResponse> findInvoiceRefundDetailsByCode(EntityManager em, String invoiceRefundCode) {
        try {
            List<Object[]> rows = em.createNativeQuery("""
                            select d.id,
                                   d.quantity,
                                   d.unit_price,
                                   d.total_amount,
                                   d.batch_number,
                                   d.return_invoice_code,
                                   m.id as medicine_id,
                                   m.barcode,
                                   m.medicine_name,
                                   m.measuring_unit,
                                   b.batch_number
                            from tbl_invoice_detail_return d
                            join tbl_batch b on d.batch_number = b.id
                            join tbl_medicine m on d.medicine_id = m.id
                            where d.return_invoice_code = :code
                            """)
                    .setParameter("code", invoiceRefundCode)
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
            log.error("event=invoice_refund_find_detail_failed code={} errorMessage={}", invoiceRefundCode, e.getMessage());
            throw e;
        }
    }

    public int countFiltered(EntityManager em, int filterApprove, int filterDate, LocalDate startDate, LocalDate endDate) {
        try {
            StringBuilder sql = new StringBuilder("select count(*) from tbl_invoice_return r where 1=1");
            Map<String, Object> params = new HashMap<>();

            appendApproveFilter(sql, params, filterApprove);
            appendDateFilter(sql, params, filterDate, startDate, endDate);

            return ((Number) buildQuery(em, sql, params).getSingleResult()).intValue();
        } catch (Exception e) {
            log.error("event=invoice_refund_count_filtered_failed filterApprove={} filterDate={} startDate={} endDate={} errorMessage={}",
                    filterApprove, filterDate, startDate, endDate, e.getMessage());
            throw e;
        }
    }

    public int countFilteredAndSearchByCode(EntityManager em, int filterApprove, int filterDate, LocalDate startDate, LocalDate endDate, String keyword) {
        try {
            StringBuilder sql = new StringBuilder("select count(*) from tbl_invoice_return r where 1=1");
            Map<String, Object> params = new HashMap<>();

            appendApproveFilter(sql, params, filterApprove);
            appendDateFilter(sql, params, filterDate, startDate, endDate);
            appendCodeLike(sql, params, keyword);

            return ((Number) buildQuery(em, sql, params).getSingleResult()).intValue();
        } catch (Exception e) {
            log.error("event=invoice_refund_count_filtered_search_code_failed filterApprove={} filterDate={} startDate={} endDate={} keyword={} errorMessage={}",
                    filterApprove, filterDate, startDate, endDate, keyword, e.getMessage());
            throw e;
        }
    }

    public int countFilteredAndSearchByBatchNumber(EntityManager em, int filterApprove, int filterDate, LocalDate startDate, LocalDate endDate, String keyword) {
        try {
            StringBuilder sql = new StringBuilder("select count(*) from tbl_invoice_return r where 1=1");
            Map<String, Object> params = new HashMap<>();

            appendApproveFilter(sql, params, filterApprove);
            appendDateFilter(sql, params, filterDate, startDate, endDate);
            appendBatchNumberExists(sql, params, keyword);

            return ((Number) buildQuery(em, sql, params).getSingleResult()).intValue();
        } catch (Exception e) {
            log.error("event=invoice_refund_count_filtered_search_batch_failed filterApprove={} filterDate={} startDate={} endDate={} keyword={} errorMessage={}",
                    filterApprove, filterDate, startDate, endDate, keyword, e.getMessage());
            throw e;
        }
    }

    public List<InvoiceRefundResponse> findFiltered(EntityManager em, int skip, int pageSize, int filterApprove, int filterDate, LocalDate startDate, LocalDate endDate) {
        try {
            StringBuilder sql = new StringBuilder("""
                    select r.return_invoice_code,
                           r.created_date,
                           r.refund_amount,
                           r.reason,
                           r.is_approved,
                           c.id,
                           c.full_name,
                           c.phone_number,
                           e.id,
                           e.employee_code,
                           e.full_name
                    from tbl_invoice_return r
                    join tbl_customer c on r.customer_id = c.id
                    join tbl_employee e on r.employee_id = e.id
                    where 1=1
                    """);
            Map<String, Object> params = new HashMap<>();

            appendApproveFilter(sql, params, filterApprove);
            appendDateFilter(sql, params, filterDate, startDate, endDate);
            sql.append(" order by r.created_date desc offset :skip rows fetch first :pageSize rows only");

            params.put("skip", skip);
            params.put("pageSize", pageSize);

            List<Object[]> rows = buildQuery(em, sql, params).getResultList();
            return mapRefundRowsBasic(rows);
        } catch (Exception e) {
            log.error("event=invoice_refund_find_filtered_failed skip={} pageSize={} filterApprove={} filterDate={} startDate={} endDate={} errorMessage={}",
                    skip, pageSize, filterApprove, filterDate, startDate, endDate, e.getMessage());
            throw e;
        }
    }

    public List<InvoiceRefundResponse> findFilteredAndSearchByCode(EntityManager em, int skip, int pageSize, int filterApprove, int filterDate, LocalDate startDate, LocalDate endDate, String keyword) {
        try {
            StringBuilder sql = new StringBuilder("""
                    select r.return_invoice_code,
                           r.created_date,
                           r.refund_amount,
                           r.reason,
                           r.is_approved,
                           c.id,
                           c.full_name,
                           c.phone_number,
                           e.id,
                           e.employee_code,
                           e.full_name
                    from tbl_invoice_return r
                    join tbl_customer c on r.customer_id = c.id
                    join tbl_employee e on r.employee_id = e.id
                    where 1=1
                    """);
            Map<String, Object> params = new HashMap<>();

            appendApproveFilter(sql, params, filterApprove);
            appendDateFilter(sql, params, filterDate, startDate, endDate);
            appendCodeLike(sql, params, keyword);
            sql.append(" order by r.created_date desc offset :skip rows fetch first :pageSize rows only");

            params.put("skip", skip);
            params.put("pageSize", pageSize);

            List<Object[]> rows = buildQuery(em, sql, params).getResultList();
            return mapRefundRowsBasic(rows);
        } catch (Exception e) {
            log.error("event=invoice_refund_find_filtered_search_code_failed skip={} pageSize={} filterApprove={} filterDate={} startDate={} endDate={} keyword={} errorMessage={}",
                    skip, pageSize, filterApprove, filterDate, startDate, endDate, keyword, e.getMessage());
            throw e;
        }
    }

    public List<InvoiceRefundResponse> findFilteredAndSearchByBatchNumber(EntityManager em, int skip, int pageSize, int filterApprove, int filterDate, LocalDate startDate, LocalDate endDate, String keyword) {
        try {
            StringBuilder sql = new StringBuilder("""
                    select r.return_invoice_code,
                           r.created_date,
                           r.refund_amount,
                           r.reason,
                           r.is_approved,
                           c.id,
                           c.full_name,
                           c.phone_number,
                           e.id,
                           e.employee_code,
                           e.full_name
                    from tbl_invoice_return r
                    join tbl_customer c on r.customer_id = c.id
                    join tbl_employee e on r.employee_id = e.id
                    where 1=1
                    """);
            Map<String, Object> params = new HashMap<>();

            appendApproveFilter(sql, params, filterApprove);
            appendDateFilter(sql, params, filterDate, startDate, endDate);
            appendBatchNumberExists(sql, params, keyword);
            sql.append(" order by r.created_date desc offset :skip rows fetch first :pageSize rows only");

            params.put("skip", skip);
            params.put("pageSize", pageSize);

            List<Object[]> rows = buildQuery(em, sql, params).getResultList();
            return mapRefundRowsBasic(rows);
        } catch (Exception e) {
            log.error("event=invoice_refund_find_filtered_search_batch_failed skip={} pageSize={} filterApprove={} filterDate={} startDate={} endDate={} keyword={} errorMessage={}",
                    skip, pageSize, filterApprove, filterDate, startDate, endDate, keyword, e.getMessage());
            throw e;
        }
    }

    private jakarta.persistence.Query buildQuery(EntityManager em, StringBuilder sql, Map<String, Object> params) {
        jakarta.persistence.Query query = em.createNativeQuery(sql.toString());
        params.forEach(query::setParameter);
        return query;
    }

    private void appendApproveFilter(StringBuilder sql, Map<String, Object> params, int filterApprove) {
        if (filterApprove != 0) {
            sql.append(" and r.is_approved = true");
        }
    }

    private void appendDateFilter(StringBuilder sql, Map<String, Object> params, int filterDate, LocalDate startDate, LocalDate endDate) {
        if (filterDate == 0) {
            return;
        }

        LocalDate today = LocalDate.now();
        switch (filterDate) {
            case 1 -> addDateRange(sql, params, today, today.plusDays(1));
            case 2 -> addDateRange(sql, params, today.minusDays(6), today.plusDays(1));
            case 3 -> addDateRange(sql, params, today.minusDays(29), today.plusDays(1));
            case 4 -> addDateRange(sql, params, startDate, endDate == null ? null : endDate.plusDays(1));
            default -> throw new IllegalArgumentException("Giá trị filterDate không hợp lệ: " + filterDate);
        }
    }

    private void addDateRange(StringBuilder sql, Map<String, Object> params, LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null) {
            sql.append(" and r.created_date >= :fromDate");
            params.put("fromDate", fromDate);
        }
        if (toDate != null) {
            sql.append(" and r.created_date < :toDate");
            params.put("toDate", toDate);
        }
    }

    private void appendCodeLike(StringBuilder sql, Map<String, Object> params, String keyword) {
        if (keyword != null) {
            sql.append(" and r.return_invoice_code ilike :keyword or c.phone_number ilike :keyword");
            params.put("keyword", "%" + keyword + "%");
        }
    }

    private void appendBatchNumberExists(StringBuilder sql, Map<String, Object> params, String keyword) {
        if (keyword != null) {
            sql.append(" and exists (select 1 from tbl_invoice_detail_return d join tbl_batch b on d.batch_number = b.id " +
                    "where d.return_invoice_code = r.return_invoice_code and b.batch_number ilike :batchKeyword)");
            params.put("batchKeyword", "%" + keyword + "%");
        }
    }

    private List<InvoiceRefundResponse> mapRefundRowsBasic(List<Object[]> rows) {
        List<InvoiceRefundResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(mapRefundRowBasic(row));
        }
        return result;
    }

    private InvoiceRefundResponse mapRefundRowBasic(Object[] row) {
        LocalDateTime createdDate = null;
        Object createdObj = row[1];
        if (createdObj instanceof Timestamp) {
            createdDate = ((Timestamp) createdObj).toLocalDateTime();
        } else if (createdObj instanceof LocalDateTime) {
            createdDate = (LocalDateTime) createdObj;
        }

        CustomerResponse customer = null;
        if (row[5] != null) {
            customer = CustomerResponse.builder()
                    .id(((Number) row[5]).longValue())
                    .fullName((String) row[6])
                    .phoneNumber((String) row[7])
                    .build();
        }

        EmployeeMiniResponse employee = null;
        if (row[8] != null) {
            employee = EmployeeMiniResponse.builder()
                    .id(((Number) row[8]).longValue())
                    .employeeCode((String) row[9])
                    .fullName((String) row[10])
                    .build();
        }

        return InvoiceRefundResponse.builder()
                .returnInvoiceCode((String) row[0])
                .createdDate(createdDate)
                .refundAmount(((Number) row[2]).doubleValue())
                .reason((String) row[3])
                .approved((Boolean) row[4])
                .customer(customer)
                .employee(employee)
                .build();
    }

    private InvoiceRefundResponse mapRefundRow(Object[] row) {
        LocalDateTime createdDate = null;
        Object createdObj = row[1];
        if (createdObj instanceof Timestamp) {
            createdDate = ((Timestamp) createdObj).toLocalDateTime();
        } else if (createdObj instanceof LocalDateTime) {
            createdDate = (LocalDateTime) createdObj;
        }

        CustomerResponse customer = null;
        if (row[5] != null) {
            customer = CustomerResponse.builder()
                    .id(((Number) row[5]).longValue())
                    .fullName((String) row[6])
                    .phoneNumber((String) row[7])
                    .build();
        }

        EmployeeMiniResponse employee = null;
        if (row[8] != null) {
            employee = EmployeeMiniResponse.builder()
                    .id(((Number) row[8]).longValue())
                    .employeeCode((String) row[9])
                    .fullName((String) row[10])
                    .build();
        }

        InvoiceMiniResponse invoice = null;
        if (row.length > 12 && row[11] != null) {
            invoice = InvoiceMiniResponse.builder()
                    .invoiceCode((String) row[11])
                    .createdDate((LocalDateTime) row[12])
                    .build();
        }

        return InvoiceRefundResponse.builder()
                .returnInvoiceCode((String) row[0])
                .createdDate(createdDate)
                .refundAmount(((Number) row[2]).doubleValue())
                .reason((String) row[3])
                .approved((Boolean) row[4])
                .customer(customer)
                .employee(employee)
                .invoice(invoice)
                .build();
    }
}
