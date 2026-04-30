package com.pharmacy.mapper;

import com.pharmacy.entity.Customer;
import com.pharmacy.shared.dto.request.CustomerRequest;

public final class CustomerMapper {

    private CustomerMapper() {
    }

    public static Customer toNewCustomer(CustomerRequest request) {
        if (request == null) {
            return null;
        }

        return Customer.builder()
                .phoneNumber(request.getPhoneNumber())
                .fullName(request.getFullName())
                .rewardPoints(request.getRewardPoints())
                .build();
    }

    public static void applyPaymentUpdate(CustomerRequest request, Customer customer) {
        if (request == null || customer == null) {
            return;
        }

        customer.setFullName(request.getFullName());
        int currentPoints = customer.getRewardPoints() == null ? 0 : customer.getRewardPoints();
        customer.setRewardPoints(currentPoints + request.getRewardPoints());
    }
}

