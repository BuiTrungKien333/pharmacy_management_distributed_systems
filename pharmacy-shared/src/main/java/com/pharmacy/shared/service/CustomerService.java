package com.pharmacy.shared.service;

import com.pharmacy.shared.dto.response.CustomerMaxResponse;

public interface CustomerService {
    CustomerMaxResponse getCustomerByPhoneNumber(String phone);
}
