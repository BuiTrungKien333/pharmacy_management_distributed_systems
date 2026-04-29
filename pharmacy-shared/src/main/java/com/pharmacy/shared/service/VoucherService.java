package com.pharmacy.shared.service;

import com.pharmacy.shared.dto.response.CustomerMaxResponse;
import com.pharmacy.shared.dto.response.VoucherResponse;

import java.util.List;

public interface VoucherService {
    List<VoucherResponse> getAllVoucherByConditionCustomer(CustomerMaxResponse customerMaxResponse, double tongTienHang);
}
