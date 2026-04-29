package com.pharmacy.repository;

import com.pharmacy.shared.dto.response.CustomerMaxResponse;
import com.pharmacy.shared.dto.response.CustomerRankResponse;
import com.pharmacy.shared.dto.response.VoucherResponse;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class VoucherRepository {

    public int updateUsedCount(EntityManager em, String voucherCode) {
        String jpql = """
                    update Voucher v
                    set v.usedCount = v.usedCount + 1
                    where v.voucherCode = :voucherCode
                """;

        return em.createQuery(jpql)
                .setParameter("voucherCode", voucherCode)
                .executeUpdate();
    }

    public List<VoucherResponse> getAllVoucherByConditionCustomer(EntityManager em, CustomerMaxResponse customer, double totalMoney) {
        try {
            StringBuilder jpql = new StringBuilder(
                    "select new com.pharmacy.shared.dto.response.VoucherResponse(" +
                            "v.voucherCode, v.discountValue, v.minOrderAmount, v.maxDiscountAmount, " +
                            "v.startDate, v.endDate, v.usedCount, v.maxUseCount, " +
                            "new com.pharmacy.shared.dto.response.CustomerRankResponse(r.id, r.rankName), 0) " +
                            "from Voucher v join v.customerRank r " +
                            "where v.deletedAt is null " +
                            "and v.usedCount < v.maxUseCount " +
                            "and v.minOrderAmount <= :totalMoney " +
                            "and :today between v.startDate and v.endDate"
            );

            boolean filterByRank = customer != null && customer.getCustomerRank() != null;
            if (filterByRank) {
                jpql.append(" and r.id <= :rankId");
            }

            jpql.append(" order by case when (:totalMoney * v.discountValue / 100) < v.maxDiscountAmount " +
                    "then (:totalMoney * v.discountValue / 100) else v.maxDiscountAmount end desc");

            var query = em.createQuery(jpql.toString(), VoucherResponse.class)
                    .setParameter("totalMoney", totalMoney)
                    .setParameter("today", LocalDate.now());

            if (filterByRank) {
                query.setParameter("rankId", customer.getCustomerRank().getId());
            }

            List<VoucherResponse> responses = query.getResultList();
            List<VoucherResponse> result = new ArrayList<>(responses.size());

            for (VoucherResponse response : responses) {
                response.setTotalDiscountedAmount(totalMoney);
                result.add(response);
            }

            return result;
        } catch (Exception e) {
            log.error("event=get_all_voucher_by_condition_customer_failed errorMessage={}", e.getMessage());
            throw e;
        }
    }
}
