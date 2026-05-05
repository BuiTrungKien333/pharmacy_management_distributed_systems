package com.pharmacy.service.impl;

import com.pharmacy.entity.*;
import com.pharmacy.persistence.JpaTransactionTemplate;
import com.pharmacy.repository.BatchRepository;
import com.pharmacy.repository.RefundDetailRepository;
import com.pharmacy.repository.RefundRepository;
import com.pharmacy.shared.dto.request.InvoiceDetailRefundRequest;
import com.pharmacy.shared.dto.request.InvoiceRefundRequest;
import com.pharmacy.shared.dto.response.InvoiceDetailResponse;
import com.pharmacy.shared.dto.response.InvoiceRefundResponse;
import com.pharmacy.shared.dto.response.InvoiceResponse;
import com.pharmacy.shared.service.RefundService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final RefundRepository refundRepository;

    private final RefundDetailRepository refundDetailRepository;

    private final BatchRepository batchRepository;

    @Override
    public InvoiceResponse getInvoiceById(String qrCode) {
        try {
            InvoiceResponse invoice = JpaTransactionTemplate.execute(
                    em -> refundRepository.findInvoiceByCode(em, qrCode)
            );

            if (invoice == null) {
                throw new IllegalArgumentException("Hóa đơn không tồn tại.");
            }
            if (invoice.getCustomer() == null) {
                throw new IllegalArgumentException("Không áp dụng đổi trả với khách hàng vãng lai.");
            }
            if (invoice.isReturned()) {
                throw new IllegalArgumentException("Hóa đơn đã được trả trước đó.");
            }

            return invoice;
        } catch (Exception e) {
            log.error("event=refund_get_invoice_failed qrCode={} errorMessage={}", qrCode, e.getMessage(), e);
            throw new RuntimeException("Không thể lấy hóa đơn: " + e.getMessage(), e);
        }
    }

    @Override
    public List<InvoiceDetailResponse> getAllInvoiceDetailByQrCode(String qrCode) {
        try {
            return JpaTransactionTemplate.execute(
                    em -> refundRepository.findInvoiceDetailsByCode(em, qrCode)
            );
        } catch (Exception e) {
            log.error("event=refund_get_invoice_detail_failed qrCode={} errorMessage={}", qrCode, e.getMessage(), e);
            throw new RuntimeException("Không thể lấy chi tiết hóa đơn: " + e.getMessage(), e);
        }
    }

    private String generateInvoiceRefundCode(EntityManager em) {
        LocalDate date = LocalDate.now();
        long count = getInvoiceRefundCount(em);
        String datePart = date.format(DateTimeFormatter.ofPattern("ddMMyy"));
        String seq = String.format("%04d", (count + 1) % 10000);
        return "HDT" + datePart + seq;
    }

    private long getInvoiceRefundCount(EntityManager em) {
        Long count = em.createQuery(
                        "select count(i) from InvoiceReturn i",
                        Long.class)
                .getSingleResult();
        return count == null ? 0 : count;
    }

    @Override
    public InvoiceRefundResponse processInvoiceRefund(InvoiceRefundRequest refundRequest, List<InvoiceDetailRefundRequest> list) {
        try {
            return JpaTransactionTemplate.execute(em -> {

                InvoiceReturn invoiceReturn = InvoiceReturn.builder()
                        .createdDate(LocalDateTime.now())
                        .refundAmount(refundRequest.getRefundAmount())
                        .reason(refundRequest.getReason())
                        .invoice(em.getReference(Invoice.class, refundRequest.getInvoiceCode()))
                        .customer(em.getReference(Customer.class, refundRequest.getCustomer().getId()))
                        .employee(em.getReference(Employee.class, refundRequest.getEmployee().getId()))
                        .approved(false)
                        .build();

                String invoiceRefundCode = generateInvoiceRefundCode(em);
                invoiceReturn.setReturnInvoiceCode(invoiceRefundCode);

                refundRepository.save(em, invoiceReturn);

                refundRepository.updateStatusReturnOfInvoice(em, refundRequest.getInvoiceCode());

                for (InvoiceDetailRefundRequest refund : list) {
                    InvoiceDetailReturn invoiceDetailReturn = InvoiceDetailReturn.builder()
                            .quantity(refund.getQuantity())
                            .unitPrice(refund.getUnitPrice())
                            .totalAmount(refund.getTotalAmount())
                            .status(false)
                            .batch(em.getReference(Batch.class, refund.getBatchId()))
                            .medicine(em.getReference(Medicine.class, refund.getMedicineId()))
                            .invoiceReturn(em.getReference(InvoiceReturn.class, invoiceRefundCode))
                            .build();

                    refundDetailRepository.save(em, invoiceDetailReturn);
                }

                return InvoiceRefundResponse.builder()
                        .returnInvoiceCode(invoiceRefundCode)
                        .refundAmount(refundRequest.getRefundAmount())
                        .createdDate(LocalDateTime.now())
                        .reason(refundRequest.getReason())
                        .customer(refundRequest.getCustomer())
                        .employee(refundRequest.getEmployee())
                        .build();
            });
        } catch (Exception e) {
            log.error("event=process_invoice_refund_failed errorMessage={}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean approveInvoiceRefund(List<InvoiceDetailRefundRequest> list) {
        if (list == null || list.isEmpty()) {
            return false;
        }

        return JpaTransactionTemplate.execute(em -> {
            String invoiceRefundCode = list.get(0).getInvoiceRefundCode();
            if (invoiceRefundCode == null || invoiceRefundCode.isBlank()) {
                throw new IllegalArgumentException("Mã phiếu trả không được để trống.");
            }

            try {
                refundRepository.updateStatusApporvOfInvoiceRefund(em, invoiceRefundCode);

                for (InvoiceDetailRefundRequest request : list) {
                    if (request == null) {
                        log.warn("event=invoice_refund_approve_skip_null_request invoiceRefundCode={}", invoiceRefundCode);
                        continue;
                    }

                    refundDetailRepository.update(em, request);

                    int newQty = "Bán tiếp".equals(request.getResolution()) ? request.getQuantity() : 0;
                    System.out.println(request);
                    System.out.println(request.getResolution());
                    System.out.println(newQty);
                    System.out.println("-------------");
                    if (newQty > 0) {
                        batchRepository.updateQuantityWhenApproveInvoiceRefund(em, request.getBatchId(), newQty);
                    }
                }

                return true;
            } catch (Exception e) {
                log.error("event=invoice_refund_approve_failed invoiceRefundCode={} errorMessage={}",
                        invoiceRefundCode, e.getMessage(), e);
                throw e;
            }
        });
    }
}
