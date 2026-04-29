package com.pharmacy.service.impl;

import com.pharmacy.entity.*;
import com.pharmacy.mapper.CustomerMapper;
import com.pharmacy.persistence.JpaTransactionTemplate;
import com.pharmacy.repository.*;
import com.pharmacy.shared.dto.request.*;
import com.pharmacy.shared.dto.response.CustomerResponse;
import com.pharmacy.shared.dto.response.EmployeeMiniResponse;
import com.pharmacy.shared.dto.response.InvoiceResponse;
import com.pharmacy.shared.service.SellService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class SellServiceImpl implements SellService {

    private final SellRepository sellRepository;

    private final CustomerRepository customerRepository;

    private final VoucherRepository voucherRepository;

    private final InvoiceRepository invoiceRepository;

    private final InvoiceDetailRepository invoiceDetailRepository;

    private final BatchRepository batchRepository;

    @Override
    public MedicineBatchToSellRequest getMedicineAndBatchToSellByBarcode(String barcode) {

        try {
            MedicineBatchToSellRequest request = JpaTransactionTemplate.execute(
                    em -> sellRepository.getMedicineAndBatchToSellByBarcode(em, barcode)
            );

            if (request == null) {
                throw new IllegalArgumentException(
                        "Không tìm thấy hoặc thuốc không hợp lệ (hết hàng | sắp hết hạn) với barcode: "
                                + barcode);
            }

            if (request.getDeletedAt() != null) {
                throw new IllegalArgumentException("Thuốc đã ngừng kinh doanh. Không được phép bán.");
            }

            return request;
        } catch (Exception e) {
            log.error("event=sell_get_medicine_by_barcode_failed barcode={} errorMessage={}", barcode, e.getMessage(), e);
            throw new RuntimeException("Không thể lấy thông tin thuốc để bán: " + e.getMessage(), e);
        }
    }


    @Override
    public MedicineBatchToSellRequest processAddToCart(MedicineBatchToSellRequest request, int sellingQuantity) {
        if (sellingQuantity > request.getTotalQuantity()) {
            throw new IllegalArgumentException("Số lượng hàng trong kho không đủ.");
        }

        List<BatchSellRequest> listBatchNeedUsed = new ArrayList<>();
        double sellingPrice = 0;
        int tempQuantity = sellingQuantity;

        for (BatchSellRequest batch : request.getBatchSellRequestList()) {
            if (tempQuantity <= 0) {
                break;
            }

            int quantityNeedOfThisBatch = Math.min(tempQuantity, batch.getRemainingQuantity());
            sellingPrice += quantityNeedOfThisBatch * batch.getSellingPrice();

            listBatchNeedUsed.add(batch);

            tempQuantity -= quantityNeedOfThisBatch;
        }

        sellingPrice /= sellingQuantity;

        List<BatchDistributionRequest> batchItemsToAddToCart = new ArrayList<>();
        int remainingQuantity = sellingQuantity;

        for (BatchSellRequest batch : listBatchNeedUsed) {
            int quantityNeed = Math.min(remainingQuantity, batch.getRemainingQuantity());

            BatchDistributionRequest batchDistributionRequest = BatchDistributionRequest.builder()
                    .medicineId(request.getId())
                    .medicineName(request.getMedicineName())
                    .batchId(batch.getBatchId())
                    .batchNumber(batch.getBatchNumber())
                    .sellingQuantity(quantityNeed)
                    .sellingPrice(sellingPrice)
                    .expirationDate(batch.getExpirationDate())
                    .originalPrice(batch.getSellingPrice())
                    .remainingQuantity(batch.getRemainingQuantity())
                    .build();

            batchItemsToAddToCart.add(batchDistributionRequest);

            remainingQuantity -= quantityNeed;
        }

        request.setBatchDistributionRequestList(batchItemsToAddToCart);
        request.setSellingQuantity(sellingQuantity);
        request.setSellingPrice(sellingPrice);
        request.setTotalAmount();

        return request;
    }

    @Override
    public InvoiceResponse processPayment(InvoiceRequest request, List<MedicineBatchToSellRequest> sellRequestList) {
        if (request == null) {
            throw new IllegalArgumentException("Thông tin hóa đơn không được để trống.");
        }

        try {
            return JpaTransactionTemplate.execute(em -> {
                Customer attachedCustomer = null;
                if (request.getCustomer() != null) {
                    CustomerRequest customerRequest = request.getCustomer();
                    Customer customer = customerRepository.findByPhoneNumber(em, customerRequest.getPhoneNumber());

                    if (customer == null) {
                        Customer customerNew = CustomerMapper.toNewCustomer(customerRequest);
                        customerNew.setCustomerRank(em.getReference(CustomerRank.class, 1));
                        customerRepository.save(em, customerNew);
                        attachedCustomer = customerNew;
                    } else {
                        CustomerMapper.applyPaymentUpdate(customerRequest, customer);
                        customerRepository.update(em, customer);
                        attachedCustomer = customer;
                    }
                }

                Voucher attachedVoucher = null;
                String voucherCode = request.getVoucherCode();
                if (voucherCode != null) {
                    int used = voucherRepository.updateUsedCount(em, voucherCode);
                    if (used == 0) {
                        throw new IllegalArgumentException("Lỗi cập nhật số lượng voucher");
                    }
                    attachedVoucher = em.getReference(Voucher.class, voucherCode);
                }

                if (request.getEmployee() == null || request.getEmployee().getId() == null) {
                    throw new IllegalArgumentException("Thông tin nhân viên không được để trống.");
                }

                LocalDateTime now = LocalDateTime.now();
                String invoiceCode = generateInvoiceCode(em, now);

                // Mã hóa đơn (invoiceCode): HDDDMMYYXXXX (trong đó DDMMYY là định dạng ngày lập hóa đơn, còn XXXX tự động tăng)
                Invoice invoice = Invoice.builder()
                        .invoiceCode(invoiceCode)
                        .customer(attachedCustomer)
                        .employee(em.getReference(Employee.class, request.getEmployee().getId()))
                        .voucher(attachedVoucher)
                        .createdDate(now)
                        .totalGoodsAmount(request.getTotalGoodsAmount())
                        .totalPayableAmount(request.getTotalPayableAmount())
                        .note("")
                        .returned(false)
                        .build();

                invoiceRepository.save(em, invoice);

                for (MedicineBatchToSellRequest sellRequest : sellRequestList) {
                    for (BatchDistributionRequest distributionRequest : sellRequest.getBatchDistributionRequestList()) {
                        InvoiceDetail invoiceDetail = InvoiceDetail.builder()
                                .quantity(distributionRequest.getSellingQuantity())
                                .unitPrice(distributionRequest.getSellingPrice())
                                .invoice(em.getReference(Invoice.class, invoice.getInvoiceCode()))
                                .medicine(em.getReference(Medicine.class, distributionRequest.getMedicineId()))
                                .batch(em.getReference(Batch.class, distributionRequest.getBatchId()))
                                .build();
                        invoiceDetail.setTotalAmount();
                        invoiceDetailRepository.save(em, invoiceDetail);

                        batchRepository.deductBatchQuantity(em, distributionRequest.getBatchNumber(), distributionRequest.getSellingQuantity());
                    }
                }

                return InvoiceResponse.builder()
                        .invoiceCode(invoiceCode)
                        .createdDate(now)
                        .customer(attachedCustomer == null ? null : CustomerResponse.builder()
                                .id(attachedCustomer.getId())
                                .fullName(attachedCustomer.getFullName())
                                .phoneNumber(attachedCustomer.getPhoneNumber())
                                .build())
                        .employee(EmployeeMiniResponse.builder()
                                .id(request.getEmployee().getId())
                                .fullName(request.getEmployee().getFullName())
                                .employeeCode(request.getEmployee().getUsername())
                                .build())
                        .totalGoodsAmount(request.getTotalGoodsAmount())
                        .totalPayableAmount(request.getTotalPayableAmount())
                        .build();
            });
        } catch (Exception e) {
            log.error("event=sell_process_payment_failed errorMessage={}", e.getMessage(), e);
            throw new RuntimeException("Không thể thanh toán: " + e.getMessage(), e);
        }
    }

    private String generateInvoiceCode(EntityManager em, LocalDateTime createdDate) {
        LocalDate date = createdDate.toLocalDate();
        long count = getInvoiceCount(em);
        String datePart = date.format(DateTimeFormatter.ofPattern("ddMMyy"));
        String seq = String.format("%04d", (count + 1) % 10000);
        return "HD" + datePart + seq;
    }

    private long getInvoiceCount(EntityManager em) {
        Long count = em.createQuery(
                        "select count(i) from Invoice i",
                        Long.class)
                .getSingleResult();
        return count == null ? 0 : count;
    }

    @Override
    public double calculateMoneyChange(String str, double priceToPayment) {
        if (str.isEmpty())
            return 0;

        try {
            double moneyCustomer = Double.parseDouble(str);
            return moneyCustomer < priceToPayment ? 0 : moneyCustomer - priceToPayment;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public double calculateTotalMoneyToPayment(double totalPrice, double discountPrice) {
        double res = totalPrice - discountPrice;
        if (res < 0)
            throw new IllegalArgumentException("Error calculate money < 0");

        return res;
    }

    @Override
    public double calculateTotalMoneyByMedicineAndBatch(List<MedicineBatchToSellRequest> list) {
        return list.stream().mapToDouble(MedicineBatchToSellRequest::getTotalAmount).sum();
    }

}
