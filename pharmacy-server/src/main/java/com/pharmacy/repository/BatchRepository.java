package com.pharmacy.repository;

import com.pharmacy.entity.Batch;
import com.pharmacy.shared.dto.response.BatchAllResponse;
import com.pharmacy.shared.dto.response.BatchResponse;
import com.pharmacy.shared.util.enums.BatchStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class BatchRepository {

    private static final String BATCH_RESPONSE_SELECT =
            "select new com.pharmacy.shared.dto.response.BatchResponse(" +
                    "b.id, b.batchNumber, b.expirationDate, b.importDate, b.importQuantity, " +
                    "b.remainingQuantity, b.sellingPrice, b.batchStatus, " +
                    "new com.pharmacy.shared.dto.response.MedicineMiniResponse(" +
                    "m.id, m.barcode, m.medicineName, m.measuringUnit)) " +
                    "from Batch b join b.medicine m";

    public void save(EntityManager em, Batch batch) {
        try {
            em.persist(batch);
        } catch (Exception e) {
            log.error("event=batch_save_failed errorMessage={}", e.getMessage());
            throw e;
        }
    }

    public Batch update(EntityManager em, Batch batch) {
        try {
            return em.merge(batch);
        } catch (Exception e) {
            log.error("event=batch_update_failed batchNumber={} errorMessage={}", batch.getBatchNumber(), e.getMessage());
            throw e;
        }
    }

    public Batch findById(EntityManager em, Long id) {
        try {
            List<Batch> batches = em.createQuery("select b from Batch b where b.id = :id", Batch.class)
                    .setParameter("id", id)
                    .setMaxResults(1)
                    .getResultList();
            return batches.isEmpty() ? null : batches.get(0);
        } catch (Exception e) {
            log.error("event=batch_find_by_id_failed id={} errorMessage={}", id, e.getMessage());
            throw e;
        }
    }

    public BatchResponse findResponseById(EntityManager em, Long id) {
        try {
            List<BatchResponse> batches = em.createQuery(
                            BATCH_RESPONSE_SELECT + " where b.id = :id",
                            BatchResponse.class)
                    .setParameter("id", id)
                    .setMaxResults(1)
                    .getResultList();
            return batches.isEmpty() ? null : batches.get(0);
        } catch (Exception e) {
            log.error("event=batch_find_response_by_id_failed id={} errorMessage={}", id, e.getMessage());
            throw e;
        }
    }

    public BatchAllResponse findResponseAllFieldById(EntityManager em, Long id) {
        try {
            List<BatchAllResponse> batches = em.createQuery(
                            "select new com.pharmacy.shared.dto.response.BatchAllResponse(" +
                                    "b.id, b.batchNumber, b.manufacturingDate, b.expirationDate, b.importDate, " +
                                    "b.importQuantity, b.remainingQuantity, b.importPrice, b.totalAmount, b.sellingPrice, " +
                                    "new com.pharmacy.shared.dto.response.MedicineResponse(" +
                                    "m.id, m.barcode, m.medicineName, m.measuringUnit, m.avatarUrl, " +
                                    "coalesce(m.totalQuantity, 0), m.medicineType, m.deletedAt), " +
                                    "new com.pharmacy.shared.dto.response.SupplierMiniResponse(" +
                                    "s.id, s.factoryCode, s.supplierName), " +
                                    "new com.pharmacy.shared.dto.response.EmployeeMiniResponse(" +
                                    "e.id, e.employeeCode, e.fullName), " +
                                    "b.batchStatus) " +
                                    "from Batch b " +
                                    "join b.medicine m " +
                                    "join b.supplier s " +
                                    "join b.employee e " +
                                    "where b.id = :id",
                            BatchAllResponse.class)
                    .setParameter("id", id)
                    .setMaxResults(1)
                    .getResultList();

            return batches.isEmpty() ? null : batches.get(0);
        } catch (Exception e) {
            log.error("event=batch_find_response_all_by_id_failed id={} errorMessage={}", id, e.getMessage());
            throw e;
        }
    }


    public BatchResponse findResponseByBatchNumber(EntityManager em, String batchNumber) {
        try {
            List<BatchResponse> batches = em.createQuery(
                            BATCH_RESPONSE_SELECT + " where b.batchNumber = :batchNumber",
                            BatchResponse.class)
                    .setParameter("batchNumber", batchNumber)
                    .setMaxResults(1)
                    .getResultList();
            return batches.isEmpty() ? null : batches.get(0);
        } catch (Exception e) {
            log.error("event=batch_find_response_by_batch_number_failed batchNumber={} errorMessage={}", batchNumber, e.getMessage());
            throw e;
        }
    }

    public int countAll(EntityManager em) {
        try {
            Long total = em.createQuery("select count(b) from Batch b", Long.class).getSingleResult();
            return total == null ? 0 : total.intValue();
        } catch (Exception e) {
            log.error("event=batch_count_all_failed errorMessage={}", e.getMessage());
            throw e;
        }
    }

    public List<BatchResponse> findAllByPage(EntityManager em, int skip, int pageSize, int sortOption) {
        try {
            String orderBy = sortOption == 1 ? " order by b.expirationDate desc" : " order by b.importDate desc";
            TypedQuery<BatchResponse> query = em.createQuery(BATCH_RESPONSE_SELECT + orderBy, BatchResponse.class)
                    .setFirstResult(skip)
                    .setMaxResults(pageSize);
            return query.getResultList();
        } catch (Exception e) {
            log.error("event=batch_find_all_by_page_failed skip={} pageSize={} sortOption={} errorMessage={}",
                    skip, pageSize, sortOption, e.getMessage());
            throw e;
        }
    }

    public int countFilteredByStatusAndDate(EntityManager em, BatchStatus batchStatus, int filter, LocalDate dateFrom, LocalDate toDate) {
        try {
            StringBuilder jpql = new StringBuilder("select count(b) from Batch b where 1=1");
            Map<String, Object> params = new HashMap<>();

            appendBatchStatusCondition(jpql, params, batchStatus);
            appendDateFilterCondition(jpql, params, filter, dateFrom, toDate);

            TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
            params.forEach(query::setParameter);
            Long total = query.getSingleResult();
            return total == null ? 0 : total.intValue();
        } catch (Exception e) {
            log.error("event=batch_count_filtered_by_status_and_date_failed status={} filter={} dateFrom={} toDate={} errorMessage={}",
                    batchStatus, filter, dateFrom, toDate, e.getMessage());
            throw e;
        }
    }

    public List<BatchResponse> findFilteredByStatusAndDate(EntityManager em,
                                                           int skip,
                                                           int pageSize,
                                                           BatchStatus batchStatus,
                                                           int filter,
                                                           LocalDate dateFrom,
                                                           LocalDate toDate,
                                                           int sortOption) {
        try {
            StringBuilder jpql = new StringBuilder(BATCH_RESPONSE_SELECT).append(" where 1=1");
            Map<String, Object> params = new HashMap<>();

            appendBatchStatusCondition(jpql, params, batchStatus);
            appendDateFilterCondition(jpql, params, filter, dateFrom, toDate);
            appendSort(jpql, sortOption);

            TypedQuery<BatchResponse> query = em.createQuery(jpql.toString(), BatchResponse.class)
                    .setFirstResult(skip)
                    .setMaxResults(pageSize);
            params.forEach(query::setParameter);
            return query.getResultList();
        } catch (Exception e) {
            log.error("event=batch_find_filtered_by_status_and_date_failed skip={} pageSize={} status={} filter={} dateFrom={} toDate={} sortOption={} errorMessage={}",
                    skip, pageSize, batchStatus, filter, dateFrom, toDate, sortOption, e.getMessage());
            throw e;
        }
    }

    public int countFilteredByStatusAndDateAndSearchByBatchNumber(EntityManager em,
                                                                  BatchStatus batchStatus,
                                                                  int filter,
                                                                  LocalDate dateFrom,
                                                                  LocalDate toDate,
                                                                  String keyword) {
        try {
            StringBuilder jpql = new StringBuilder("select count(b) from Batch b where 1=1");
            Map<String, Object> params = new HashMap<>();

            appendBatchStatusCondition(jpql, params, batchStatus);
            appendDateFilterCondition(jpql, params, filter, dateFrom, toDate);
            appendBatchNumberLikeCondition(jpql, params, keyword);

            TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
            params.forEach(query::setParameter);
            Long total = query.getSingleResult();
            return total == null ? 0 : total.intValue();
        } catch (Exception e) {
            log.error("event=batch_count_filtered_status_date_search_batch_number_failed status={} filter={} dateFrom={} toDate={} keyword={} errorMessage={}",
                    batchStatus, filter, dateFrom, toDate, keyword, e.getMessage());
            throw e;
        }
    }

    public List<BatchResponse> findFilteredByStatusAndDateAndSearchByBatchNumber(EntityManager em,
                                                                                 int skip,
                                                                                 int pageSize,
                                                                                 BatchStatus batchStatus,
                                                                                 int filter,
                                                                                 LocalDate dateFrom,
                                                                                 LocalDate toDate,
                                                                                 int sortOption,
                                                                                 String keyword) {
        try {
            StringBuilder jpql = new StringBuilder(BATCH_RESPONSE_SELECT).append(" where 1=1");
            Map<String, Object> params = new HashMap<>();

            appendBatchStatusCondition(jpql, params, batchStatus);
            appendDateFilterCondition(jpql, params, filter, dateFrom, toDate);
            appendBatchNumberLikeCondition(jpql, params, keyword);
            appendSort(jpql, sortOption);

            TypedQuery<BatchResponse> query = em.createQuery(jpql.toString(), BatchResponse.class)
                    .setFirstResult(skip)
                    .setMaxResults(pageSize);
            params.forEach(query::setParameter);
            return query.getResultList();
        } catch (Exception e) {
            log.error("event=batch_find_filtered_status_date_search_batch_number_failed skip={} pageSize={} status={} filter={} dateFrom={} toDate={} sortOption={} keyword={} errorMessage={}",
                    skip, pageSize, batchStatus, filter, dateFrom, toDate, sortOption, keyword, e.getMessage());
            throw e;
        }
    }

    public int countFilteredByStatusAndDateAndSearchByBarcode(EntityManager em,
                                                              BatchStatus batchStatus,
                                                              int filter,
                                                              LocalDate dateFrom,
                                                              LocalDate toDate,
                                                              String barcodeKeyword) {
        try {
            StringBuilder jpql = new StringBuilder("select count(b) from Batch b join b.medicine m where 1=1");
            Map<String, Object> params = new HashMap<>();

            appendBatchStatusCondition(jpql, params, batchStatus);
            appendDateFilterCondition(jpql, params, filter, dateFrom, toDate);
            appendBarcodeLikeCondition(jpql, params, barcodeKeyword);

            TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
            params.forEach(query::setParameter);
            Long total = query.getSingleResult();
            return total == null ? 0 : total.intValue();
        } catch (Exception e) {
            log.error("event=batch_count_filtered_status_date_search_barcode_failed status={} filter={} dateFrom={} toDate={} barcodeKeyword={} errorMessage={}",
                    batchStatus, filter, dateFrom, toDate, barcodeKeyword, e.getMessage());
            throw e;
        }
    }

    public List<BatchResponse> findFilteredByStatusAndDateAndSearchByBarcode(EntityManager em,
                                                                             int skip,
                                                                             int pageSize,
                                                                             BatchStatus batchStatus,
                                                                             int filter,
                                                                             LocalDate dateFrom,
                                                                             LocalDate toDate,
                                                                             int sortOption,
                                                                             String barcodeKeyword) {
        try {
            StringBuilder jpql = new StringBuilder(BATCH_RESPONSE_SELECT).append(" where 1=1");
            Map<String, Object> params = new HashMap<>();

            appendBatchStatusCondition(jpql, params, batchStatus);
            appendDateFilterCondition(jpql, params, filter, dateFrom, toDate);
            appendBarcodeLikeCondition(jpql, params, barcodeKeyword);
            appendSort(jpql, sortOption);

            TypedQuery<BatchResponse> query = em.createQuery(jpql.toString(), BatchResponse.class)
                    .setFirstResult(skip)
                    .setMaxResults(pageSize);
            params.forEach(query::setParameter);
            return query.getResultList();
        } catch (Exception e) {
            log.error("event=batch_find_filtered_status_date_search_barcode_failed skip={} pageSize={} status={} filter={} dateFrom={} toDate={} sortOption={} barcodeKeyword={} errorMessage={}",
                    skip, pageSize, batchStatus, filter, dateFrom, toDate, sortOption, barcodeKeyword, e.getMessage());
            throw e;
        }
    }

    public int updateExpiredBatchStatus(EntityManager em, LocalDate today) {
        try {
            return em.createQuery(
                            "update Batch b set b.batchStatus = :expiredStatus " +
                                    "where b.expirationDate <= :today " +
                                    "and (b.batchStatus is null or (b.batchStatus <> :expiredStatus and b.batchStatus <> :cancelledStatus))")
                    .setParameter("expiredStatus", BatchStatus.EXPIRED)
                    .setParameter("cancelledStatus", BatchStatus.CANCELLED)
                    .setParameter("today", today)
                    .executeUpdate();
        } catch (Exception e) {
            log.error("event=batch_bulk_update_expired_failed today={} errorMessage={}", today, e.getMessage());
            throw e;
        }
    }

    private void appendBatchStatusCondition(StringBuilder jpql, Map<String, Object> params, BatchStatus batchStatus) {
        if (batchStatus != null) {
            jpql.append(" and b.batchStatus = :batchStatus");
            params.put("batchStatus", batchStatus);
        }
    }

    private void appendDateFilterCondition(StringBuilder jpql,
                                           Map<String, Object> params,
                                           int filter,
                                           LocalDate dateFrom,
                                           LocalDate toDate) {
        if (filter == 0) return;

        LocalDate today = LocalDate.now();
        switch (filter) {
            case 1 -> addImportDateRange(jpql, params, today, today.plusDays(1));
            case 2 -> addImportDateRange(jpql, params, today.minusDays(6), today.plusDays(1));
            case 3 -> {
                LocalDate firstDayOfMonth = today.withDayOfMonth(1);
                addImportDateRange(jpql, params, firstDayOfMonth, firstDayOfMonth.plusMonths(1));
            }
            case 4 -> addImportDateRange(jpql, params, dateFrom, toDate.plusDays(1));
            case 5 -> addExpirationDateRange(jpql, params, today, today.plusDays(7));
            case 6 -> addExpirationDateRange(jpql, params, today, today.plusDays(30));
            case 7 -> addExpirationDateRange(jpql, params, today, today.plusMonths(3));
            default -> throw new IllegalArgumentException("Giá trị filter không hợp lệ: " + filter);
        }
    }

    private void addImportDateRange(StringBuilder jpql, Map<String, Object> params, LocalDate startDate, LocalDate endDateExclusive) {
        jpql.append(" and b.importDate >= :importStart and b.importDate < :importEnd");
        params.put("importStart", LocalDateTime.of(startDate, java.time.LocalTime.MIN));
        params.put("importEnd", LocalDateTime.of(endDateExclusive, java.time.LocalTime.MIN));
    }

    private void addExpirationDateRange(StringBuilder jpql, Map<String, Object> params, LocalDate startDate, LocalDate endDateInclusive) {
        jpql.append(" and b.expirationDate >= :expirationStart and b.expirationDate <= :expirationEnd");
        params.put("expirationStart", startDate);
        params.put("expirationEnd", endDateInclusive);
    }

    private void appendSort(StringBuilder jpql, int sortOption) {
        if (sortOption == 1) {
            jpql.append(" order by b.expirationDate desc");
        } else {
            jpql.append(" order by b.importDate desc");
        }
    }

    private void appendBatchNumberLikeCondition(StringBuilder jpql, Map<String, Object> params, String keyword) {
        if (keyword != null) {
            jpql.append(" and b.batchNumber like :keyword");
            params.put("keyword", "%" + keyword + "%");
        }
    }

    private void appendBarcodeLikeCondition(StringBuilder jpql, Map<String, Object> params, String barcodeKeyword) {
        if (barcodeKeyword != null) {
            jpql.append(" and m.barcode like :barcodeKeyword");
            params.put("barcodeKeyword", "%" + barcodeKeyword + "%");
        }
    }

    public void deductBatchQuantity(EntityManager em, String batchNumber, int sellingQuantity) {
        String jpql = "update Batch b " +
                "set b.remainingQuantity = coalesce(b.remainingQuantity - :sellingPrice, 0) " +
                "where b.batchNumber = :batchNumber " +
                "and b.batchStatus = :batchStatus " +
                "and b.remainingQuantity >= 0";

        em.createQuery(jpql)
                .setParameter("sellingPrice", sellingQuantity)
                .setParameter("batchNumber", batchNumber)
                .setParameter("batchStatus", BatchStatus.SELLING)
                .executeUpdate();
    }
}
