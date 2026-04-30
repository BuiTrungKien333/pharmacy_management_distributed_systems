package com.pharmacy.service.impl;

import com.pharmacy.entity.Customer;
import com.pharmacy.entity.CustomerRank;
import com.pharmacy.persistence.JpaTransactionTemplate;
import com.pharmacy.repository.CustomerRepository;
import com.pharmacy.shared.dto.response.CustomerMaxResponse;
import com.pharmacy.shared.dto.response.CustomerRankResponse;
import com.pharmacy.shared.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public CustomerMaxResponse getCustomerByPhoneNumber(String phone) {
        String normalizedPhone = normalize(phone);
        if (normalizedPhone == null) {
            throw new IllegalArgumentException("Số điện thoại không được để trống.");
        }

        try {
            return JpaTransactionTemplate.execute(em -> {
                Customer customer = customerRepository.findByPhoneNumber(em, normalizedPhone);
                if (customer == null) {
                    return null;
                }

                CustomerRank rank = customer.getCustomerRank();
                CustomerRankResponse rankResponse = rank == null
                        ? null
                        : new CustomerRankResponse(rank.getId(), rank.getRankName());

                int rewardPoints = customer.getRewardPoints() == null ? 0 : customer.getRewardPoints();

                return new CustomerMaxResponse(
                        customer.getId(),
                        customer.getFullName(),
                        customer.getPhoneNumber(),
                        rewardPoints,
                        rankResponse
                );
            });
        } catch (Exception e) {
            log.error("event=customer_find_by_phone_failed phoneNumber={} errorMessage={}", normalizedPhone, e.getMessage(), e);
            throw new RuntimeException("Không thể lấy thông tin khách hàng.");
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
