package com.pharmacy.shared.service;

import java.util.Map;

public interface DashboardService {

    int getCountDailyInvoices();

    double getCalculateDailyRevenue();

    double getCalculateDailyProfit();

    int getCountDailyNewCustomers();

    Map<String, Double> getRevenueLast7Days();

    Map<String, Integer> getSalesInvoiceCountLast5Days();

    Map<String, Integer> getReturnInvoiceCountLast5Days();
}
