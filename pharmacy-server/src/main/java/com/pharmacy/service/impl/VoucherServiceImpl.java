package com.pharmacy.service.impl;

import com.pharmacy.persistence.JpaTransactionTemplate;
import com.pharmacy.repository.VoucherRepository;
import com.pharmacy.shared.dto.response.CustomerMaxResponse;
import com.pharmacy.shared.dto.response.VoucherResponse;
import com.pharmacy.shared.service.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;

    @Override
    public List<VoucherResponse> getAllVoucherByConditionCustomer(CustomerMaxResponse customerMaxResponse, double tongTienHang) {
        return JpaTransactionTemplate.execute(em ->
                voucherRepository.getAllVoucherByConditionCustomer(em, customerMaxResponse, tongTienHang)
        );
    }
}
