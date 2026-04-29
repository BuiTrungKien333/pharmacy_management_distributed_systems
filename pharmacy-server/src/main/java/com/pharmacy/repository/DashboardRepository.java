package com.pharmacy.repository;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class DashboardRepository {

    public int countDailyInvoices(EntityManager em, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        try {
            Long count = em.createQuery(
                            "select count(i) from Invoice i " +
                                    "where i.returned = false and i.createdDate >= :startOfDay and i.createdDate < :endOfDay",
                            Long.class)
                    .setParameter("startOfDay", startOfDay)
                    .setParameter("endOfDay", endOfDay)
                    .getSingleResult();
            return count == null ? 0 : count.intValue();
        } catch (Exception e) {
            log.error("event=dashboard_count_daily_invoices_failed errorMessage={}", e.getMessage());
            throw e;
        }
    }

    public double calculateDailyRevenue(EntityManager em, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        try {
            Double total = em.createQuery(
                            "select coalesce(sum(i.totalPayableAmount), 0) from Invoice i " +
                                    "where i.returned = false and i.createdDate >= :startOfDay and i.createdDate < :endOfDay",
                            Double.class)
                    .setParameter("startOfDay", startOfDay)
                    .setParameter("endOfDay", endOfDay)
                    .getSingleResult();
            return total == null ? 0.0 : total;
        } catch (Exception e) {
            log.error("event=dashboard_calculate_daily_revenue_failed errorMessage={}", e.getMessage());
            throw e;
        }
    }

    public double calculateDailyProfit(EntityManager em, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        try {
            Double totalProfit = em.createQuery(
                            "select coalesce(sum((d.unitPrice - b.importPrice) * d.quantity), 0) " +
                                    "from InvoiceDetail d " +
                                    "join d.invoice i " +
                                    "join d.batch b " +
                                    "where i.returned = false and i.createdDate >= :startOfDay and i.createdDate < :endOfDay",
                            Double.class)
                    .setParameter("startOfDay", startOfDay)
                    .setParameter("endOfDay", endOfDay)
                    .getSingleResult();
            return totalProfit == null ? 0.0 : totalProfit;
        } catch (Exception e) {
            log.error("event=dashboard_calculate_daily_profit_failed errorMessage={}", e.getMessage());
            throw e;
        }
    }

    public int countDailyNewCustomers(EntityManager em, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        try {
            Long count = em.createQuery(
                            "select count(c) from Customer c " +
                                    "where c.createdAt >= :startOfDay and c.createdAt < :endOfDay",
                            Long.class)
                    .setParameter("startOfDay", startOfDay)
                    .setParameter("endOfDay", endOfDay)
                    .getSingleResult();
            return count == null ? 0 : count.intValue();
        } catch (Exception e) {
            log.error("event=dashboard_count_daily_new_customers_failed errorMessage={}", e.getMessage());
            throw e;
        }
    }

    public Map<LocalDate, Double> getRevenueByDateRange(EntityManager em, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        try {
            List<Object[]> rows = em.createNativeQuery(
                            "select cast(i.created_date as date) as d, coalesce(sum(i.total_payable_amount), 0) as revenue " +
                                    "from tbl_invoice i " +
                                    "where i.is_returned = false and i.created_date >= :startDateTime and i.created_date < :endDateTime " +
                                    "group by cast(i.created_date as date) " +
                                    "order by d")
                    .setParameter("startDateTime", startDateTime)
                    .setParameter("endDateTime", endDateTime)
                    .getResultList();

            Map<LocalDate, Double> result = new HashMap<>();
            for (Object[] row : rows) {
                LocalDate day = toLocalDate(row[0]);
                double value = ((Number) row[1]).doubleValue();
                result.put(day, value);
            }
            return result;
        } catch (Exception e) {
            log.error("event=dashboard_get_revenue_by_date_range_failed errorMessage={}", e.getMessage());
            throw e;
        }
    }

    public Map<LocalDate, Integer> getSalesInvoiceCountByDateRange(EntityManager em, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        try {
            List<Object[]> rows = em.createNativeQuery(
                            "select cast(i.created_date as date) as d, count(*) as total " +
                                    "from tbl_invoice i " +
                                    "where i.is_returned = false and i.created_date >= :startDateTime and i.created_date < :endDateTime " +
                                    "group by cast(i.created_date as date) " +
                                    "order by d")
                    .setParameter("startDateTime", startDateTime)
                    .setParameter("endDateTime", endDateTime)
                    .getResultList();

            Map<LocalDate, Integer> result = new HashMap<>();
            for (Object[] row : rows) {
                LocalDate day = toLocalDate(row[0]);
                int value = ((Number) row[1]).intValue();
                result.put(day, value);
            }
            return result;
        } catch (Exception e) {
            log.error("event=dashboard_get_sales_invoice_count_by_date_range_failed errorMessage={}", e.getMessage());
            throw e;
        }
    }

    public Map<LocalDate, Integer> getReturnInvoiceCountByDateRange(EntityManager em, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        try {
            List<Object[]> rows = em.createNativeQuery(
                            "select cast(ir.created_date as date) as d, count(*) as total " +
                                    "from tbl_invoice_return ir " +
                                    "where ir.created_date >= :startDateTime and ir.created_date < :endDateTime " +
                                    "group by cast(ir.created_date as date) " +
                                    "order by d")
                    .setParameter("startDateTime", startDateTime)
                    .setParameter("endDateTime", endDateTime)
                    .getResultList();

            Map<LocalDate, Integer> result = new HashMap<>();
            for (Object[] row : rows) {
                LocalDate day = toLocalDate(row[0]);
                int value = ((Number) row[1]).intValue();
                result.put(day, value);
            }
            return result;
        } catch (Exception e) {
            log.error("event=dashboard_get_return_invoice_count_by_date_range_failed errorMessage={}", e.getMessage());
            throw e;
        }
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Date date) {
            return date.toLocalDate();
        }
        throw new IllegalArgumentException("Unsupported date value type: " + value.getClass().getName());
    }
}
