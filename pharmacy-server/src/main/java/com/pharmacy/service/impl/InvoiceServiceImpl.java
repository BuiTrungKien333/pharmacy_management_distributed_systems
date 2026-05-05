package com.pharmacy.service.impl;

import com.pharmacy.persistence.JpaTransactionTemplate;
import com.pharmacy.repository.InvoiceRefundRepository;
import com.pharmacy.repository.InvoiceRepository;
import com.pharmacy.shared.dto.response.InvoiceDetailResponse;
import com.pharmacy.shared.dto.response.InvoiceRefundResponse;
import com.pharmacy.shared.dto.response.InvoiceResponse;
import com.pharmacy.shared.service.InvoiceService;
import com.pharmacy.shared.util.Pagination;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceRefundRepository invoiceRefundRepository;

    private int totalRecordFiltered = 0;

    private static final DateTimeFormatter UI_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public int getTotalRecordFiltered() {
        return this.totalRecordFiltered;
    }

    @Override
    public InvoiceRefundResponse getInvoiceRefundById(String invoiceCode) {
        String normalized = normalize(invoiceCode);
        if (normalized == null) {
            throw new IllegalArgumentException("Mã phiếu trả không được để trống.");
        }

        try {
            InvoiceRefundResponse invoice = JpaTransactionTemplate.execute(
                    em -> invoiceRefundRepository.findInvoiceRefundByCode(em, normalized)
            );
            if (invoice == null) {
                throw new IllegalArgumentException("Phiếu trả không tồn tại.");
            }
            return invoice;
        } catch (Exception e) {
            log.error("event=invoice_refund_get_failed code={} errorMessage={}", normalized, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<InvoiceDetailResponse> getAllInvoiceDetailByInvoiceRefundCode(String invCode) {
        String normalized = normalize(invCode);
        if (normalized == null) {
            throw new IllegalArgumentException("Mã phiếu trả không được để trống.");
        }

        try {
            return JpaTransactionTemplate.execute(
                    em -> invoiceRefundRepository.findInvoiceRefundDetailsByCode(em, normalized)
            );
        } catch (Exception e) {
            log.error("event=invoice_refund_detail_get_failed code={} errorMessage={}", normalized, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public InvoiceResponse getInvoiceById(String invoiceCode) {
        String normalized = normalize(invoiceCode);
        if (normalized == null) {
            throw new IllegalArgumentException("Mã hóa đơn không được để trống.");
        }

        try {
            InvoiceResponse invoice = JpaTransactionTemplate.execute(
                    em -> invoiceRepository.findInvoiceByCode(em, normalized)
            );
            if (invoice == null) {
                throw new IllegalArgumentException("Hóa đơn không tồn tại.");
            }
            return invoice;
        } catch (Exception e) {
            log.error("event=invoice_get_failed code={} errorMessage={}", normalized, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<InvoiceDetailResponse> getAllInvoiceDetailByInvCode(String invoiceCode) {
        String normalized = normalize(invoiceCode);
        if (normalized == null) {
            throw new IllegalArgumentException("Mã hóa đơn không được để trống.");
        }

        try {
            return JpaTransactionTemplate.execute(
                    em -> invoiceRepository.findInvoiceDetailsByCode(em, normalized)
            );
        } catch (Exception e) {
            log.error("event=invoice_detail_get_failed code={} errorMessage={}", normalized, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<?> getListInvoiceFiltered(int type, int filterApprove, int filterDate, LocalDate startDate, LocalDate endDate, Pagination pagination) {
        validatePagination(pagination);

        return JpaTransactionTemplate.execute(em -> {
            if (type == 0) {
                totalRecordFiltered = invoiceRepository.countFiltered(em, 0, filterDate, startDate, endDate);
                return invoiceRepository.findFiltered(em, pagination.getSkip(), pagination.getPageSize(), 0, filterDate, startDate, endDate);
            }

            totalRecordFiltered = invoiceRefundRepository.countFiltered(em, filterApprove, filterDate, startDate, endDate);
            return invoiceRefundRepository.findFiltered(em, pagination.getSkip(), pagination.getPageSize(), filterApprove, filterDate, startDate, endDate);
        });
    }

    @Override
    public List<?> getListInvoiceFilteredAndSearchById(int type, int filterApprove, int filterDate, LocalDate startDate, LocalDate endDate, Pagination pagination, String keyword) {
        validatePagination(pagination);

        String normalizedKeyword = normalize(keyword);

        return JpaTransactionTemplate.execute(em -> {
            if (type == 0) {
                totalRecordFiltered = invoiceRepository.countFilteredAndSearchByCode(em, 0, filterDate, startDate, endDate, normalizedKeyword);
                return invoiceRepository.findFilteredAndSearchByCode(em, pagination.getSkip(), pagination.getPageSize(), 0, filterDate, startDate, endDate, normalizedKeyword);
            }

            totalRecordFiltered = invoiceRefundRepository.countFilteredAndSearchByCode(em, filterApprove, filterDate, startDate, endDate, normalizedKeyword);
            return invoiceRefundRepository.findFilteredAndSearchByCode(em, pagination.getSkip(), pagination.getPageSize(), filterApprove, filterDate, startDate, endDate, normalizedKeyword);
        });
    }

    @Override
    public List<?> getAllInvoiceFilteredAndSearchByBatchNumber(int type, int filterApprove, int filterDate, LocalDate startDate, LocalDate endDate, Pagination pagination, String keyword) {
        validatePagination(pagination);

        String normalizedKeyword = normalize(keyword);

        return JpaTransactionTemplate.execute(em -> {
            if (type == 0) {
                totalRecordFiltered = invoiceRepository.countFilteredAndSearchByBatchNumber(em, 0, filterDate, startDate, endDate, normalizedKeyword);
                return invoiceRepository.findFilteredAndSearchByBatchNumber(em, pagination.getSkip(), pagination.getPageSize(), 0, filterDate, startDate, endDate, normalizedKeyword);
            }

            totalRecordFiltered = invoiceRefundRepository.countFilteredAndSearchByBatchNumber(em, filterApprove, filterDate, startDate, endDate, normalizedKeyword);
            return invoiceRefundRepository.findFilteredAndSearchByBatchNumber(em, pagination.getSkip(), pagination.getPageSize(), filterApprove, filterDate, startDate, endDate, normalizedKeyword);
        });
    }

    @Override
    public List<?> getAllInvoiceAndSearchByIdToExportCSV(int type, int filterApprove, int filterDate, LocalDate startDate, LocalDate endDate, String keyword) {
        return List.of();
    }

    @Override
    public List<?> getAllInvoiceAndSearchByBatchNumberToExportCSV(int type, int filterApprove, int filterDate, LocalDate startDate, LocalDate endDate, String keyword) {
        return List.of();
    }

    @Override
    public List<InvoiceResponse> getAllInvoiceByCustomer(int customerId) {
        if (customerId <= 0) {
            throw new IllegalArgumentException("Mã khách hàng không hợp lệ.");
        }

        try {
            return JpaTransactionTemplate.execute(em -> invoiceRepository.findAllByCustomerId(em, customerId));
        } catch (Exception e) {
            log.error("event=invoice_get_by_customer_failed customerId={} errorMessage={}", customerId, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public double calculateTotalRevenue(List<InvoiceResponse> list) {
        return list.stream().mapToDouble(InvoiceResponse::getTotalPayableAmount).sum();
    }

    @Override
    public double calculateTotalRefund(List<InvoiceRefundResponse> list) {
        return list.stream().mapToDouble(InvoiceRefundResponse::getRefundAmount).sum();
    }

    @Override
    public LocalDate convertStringToLocalDate(int filterDate, String text) {
        if (filterDate != 4) {
            return LocalDate.now();
        }

        if (text == null || text.isBlank()) {
            return null;
        }

        String trimmed = text.trim();
        if (trimmed.contains("/")) {
            return LocalDate.parse(trimmed, UI_DATE_FORMATTER);
        }

        return LocalDate.parse(trimmed);
    }

    private void validatePagination(Pagination page) {
        if (page == null) {
            throw new IllegalArgumentException("Thông tin phân trang không được để trống.");
        }
        if (page.getPageNumber() <= 0) {
            throw new IllegalArgumentException("pageNumber phải lớn hơn 0.");
        }
        if (page.getPageSize() <= 0) {
            throw new IllegalArgumentException("pageSize phải lớn hơn 0.");
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
