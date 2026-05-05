package com.pharmacy.shared.service;

import com.pharmacy.shared.dto.response.InvoiceDetailResponse;
import com.pharmacy.shared.dto.response.InvoiceRefundResponse;
import com.pharmacy.shared.dto.response.InvoiceResponse;
import com.pharmacy.shared.util.Pagination;

import java.time.LocalDate;
import java.util.List;

public interface InvoiceService {

    int getTotalRecordFiltered();

    List<?> getListInvoiceFiltered(int type, int filterApprove, int filterDate, LocalDate startDate, LocalDate endDate, Pagination pagination);

    List<?> getListInvoiceFilteredAndSearchById(int type, int filterApprove, int filterDate, LocalDate startDate, LocalDate endDate, Pagination pagination, String keyword);

    InvoiceRefundResponse getInvoiceRefundById(String invoiceCode);

    List<InvoiceDetailResponse> getAllInvoiceDetailByInvoiceRefundCode(String invCode);

    InvoiceResponse getInvoiceById(String invoiceCode);

    List<InvoiceDetailResponse> getAllInvoiceDetailByInvCode(String invoiceCode);

    List<?> getAllInvoiceFilteredAndSearchByBatchNumber(int type, int filterApprove, int filterDate, LocalDate startDate,
                                                        LocalDate endDate, Pagination pagination, String keyword);

    List<?> getAllInvoiceAndSearchByIdToExportCSV(int type, int filterApprove, int filterDate, LocalDate startDate, LocalDate endDate, String keyword);

    List<?> getAllInvoiceAndSearchByBatchNumberToExportCSV(int type, int filterApprove, int filterDate, LocalDate startDate,
                                                           LocalDate endDate, String keyword);

    List<InvoiceResponse> getAllInvoiceByCustomer(int customerId);

    double calculateTotalRevenue(List<InvoiceResponse> list);

    double calculateTotalRefund(List<InvoiceRefundResponse> list);

    LocalDate convertStringToLocalDate(int filterDate, String text);

}
