package com.pharmacy.shared.service;

import com.pharmacy.shared.dto.request.InvoiceDetailRefundRequest;
import com.pharmacy.shared.dto.request.InvoiceRefundRequest;
import com.pharmacy.shared.dto.response.InvoiceDetailResponse;
import com.pharmacy.shared.dto.response.InvoiceRefundResponse;
import com.pharmacy.shared.dto.response.InvoiceResponse;

import java.util.List;

public interface RefundService {

    InvoiceResponse getInvoiceById(String qrCode);

    List<InvoiceDetailResponse> getAllInvoiceDetailByQrCode(String qrCode);

    InvoiceRefundResponse processInvoiceRefund(InvoiceRefundRequest refundRequest, List<InvoiceDetailRefundRequest> list);

    boolean approveInvoiceRefund(List<InvoiceDetailRefundRequest> list);

}
