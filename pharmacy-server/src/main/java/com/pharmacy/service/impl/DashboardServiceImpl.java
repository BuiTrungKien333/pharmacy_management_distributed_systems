package com.pharmacy.service.impl;

import com.pharmacy.persistence.JpaTransactionTemplate;
import com.pharmacy.repository.DashboardRepository;
import com.pharmacy.shared.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final DateTimeFormatter DAY_LABEL_FORMATTER = DateTimeFormatter.ofPattern("dd/MM");

    private final DashboardRepository dashboardRepository;

    @Override
    public int getCountDailyInvoices() {
        LocalDateTime startOfDay = getStartOfDay();
        LocalDateTime endOfDay = getEndOfDay(startOfDay);

        try {
            return JpaTransactionTemplate.execute(
                    em -> dashboardRepository.countDailyInvoices(em, startOfDay, endOfDay)
            );
        } catch (Exception e) {
            log.error("event=service_get_count_daily_invoices_failed errorMessage={}", e.getMessage(), e);
            throw new RuntimeException("Không thể thống kê số hóa đơn bán trong ngày.", e);
        }
    }

    @Override
    public double getCalculateDailyRevenue() {
        LocalDateTime startOfDay = getStartOfDay();
        LocalDateTime endOfDay = getEndOfDay(startOfDay);

        try {
            return JpaTransactionTemplate.execute(
                    em -> dashboardRepository.calculateDailyRevenue(em, startOfDay, endOfDay)
            );
        } catch (Exception e) {
            log.error("event=service_get_calculate_daily_revenue_failed errorMessage={}", e.getMessage(), e);
            throw new RuntimeException("Không thể tính doanh thu trong ngày.", e);
        }
    }

    @Override
    public double getCalculateDailyProfit() {
        LocalDateTime startOfDay = getStartOfDay();
        LocalDateTime endOfDay = getEndOfDay(startOfDay);

        try {
            return JpaTransactionTemplate.execute(
                    em -> dashboardRepository.calculateDailyProfit(em, startOfDay, endOfDay)
            );
        } catch (Exception e) {
            log.error("event=service_get_calculate_daily_profit_failed errorMessage={}", e.getMessage(), e);
            throw new RuntimeException("Không thể tính lợi nhuận trong ngày.", e);
        }
    }

    @Override
    public int getCountDailyNewCustomers() {
        LocalDateTime startOfDay = getStartOfDay();
        LocalDateTime endOfDay = getEndOfDay(startOfDay);

        try {
            return JpaTransactionTemplate.execute(
                    em -> dashboardRepository.countDailyNewCustomers(em, startOfDay, endOfDay)
            );
        } catch (Exception e) {
            log.error("event=service_get_count_daily_new_customers_failed errorMessage={}", e.getMessage(), e);
            throw new RuntimeException("Không thể thống kê khách hàng mới trong ngày.", e);
        }
    }

    @Override
    public Map<String, Double> getRevenueLast7Days() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);

        try {
            Map<LocalDate, Double> dbData = JpaTransactionTemplate.execute(
                    em -> dashboardRepository.getRevenueByDateRange(
                            em,
                            startDate.atStartOfDay(),
                            endDate.plusDays(1).atStartOfDay()
                    )
            );

            Map<String, Double> result = new LinkedHashMap<>();
            for (LocalDate day = startDate; !day.isAfter(endDate); day = day.plusDays(1)) {
                result.put(day.format(DAY_LABEL_FORMATTER), dbData.getOrDefault(day, 0.0));
            }
            return result;
        } catch (Exception e) {
            log.error("event=service_get_revenue_last_7_days_failed errorMessage={}", e.getMessage(), e);
            throw new RuntimeException("Không thể thống kê doanh thu 7 ngày gần đây.", e);
        }
    }

    @Override
    public Map<String, Integer> getSalesInvoiceCountLast5Days() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(4);

        try {
            Map<LocalDate, Integer> dbData = JpaTransactionTemplate.execute(
                    em -> dashboardRepository.getSalesInvoiceCountByDateRange(
                            em,
                            startDate.atStartOfDay(),
                            endDate.plusDays(1).atStartOfDay()
                    )
            );

            Map<String, Integer> result = new LinkedHashMap<>();
            for (LocalDate day = startDate; !day.isAfter(endDate); day = day.plusDays(1)) {
                result.put(day.format(DAY_LABEL_FORMATTER), dbData.getOrDefault(day, 0));
            }
            return result;
        } catch (Exception e) {
            log.error("event=service_get_sales_invoice_count_last_5_days_failed errorMessage={}", e.getMessage(), e);
            throw new RuntimeException("Không thể thống kê số hóa đơn bán 5 ngày gần đây.", e);
        }
    }

    @Override
    public Map<String, Integer> getReturnInvoiceCountLast5Days() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(4);

        try {
            Map<LocalDate, Integer> dbData = JpaTransactionTemplate.execute(
                    em -> dashboardRepository.getReturnInvoiceCountByDateRange(
                            em,
                            startDate.atStartOfDay(),
                            endDate.plusDays(1).atStartOfDay()
                    )
            );

            Map<String, Integer> result = new LinkedHashMap<>();
            for (LocalDate day = startDate; !day.isAfter(endDate); day = day.plusDays(1)) {
                result.put(day.format(DAY_LABEL_FORMATTER), dbData.getOrDefault(day, 0));
            }
            return result;
        } catch (Exception e) {
            log.error("event=service_get_return_invoice_count_last_5_days_failed errorMessage={}", e.getMessage(), e);
            throw new RuntimeException("Không thể thống kê số hóa đơn trả 5 ngày gần đây.", e);
        }
    }

    private LocalDateTime getStartOfDay() {
        return LocalDate.now().atStartOfDay();
    }

    private LocalDateTime getEndOfDay(LocalDateTime startOfDay) {
        return startOfDay.plusDays(1);
    }
}
