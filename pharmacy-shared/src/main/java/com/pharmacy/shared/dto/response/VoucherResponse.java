package com.pharmacy.shared.dto.response;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class VoucherResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String voucherCode;
    private int discountValue;
    private double minOrderAmount;
    private double maxDiscountAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private int usedCount;
    private int maxUseCount;
    private CustomerRankResponse customerRank;
    private double totalDiscountedAmount;

    public void setTotalDiscountedAmount(double tongTien) {
        this.totalDiscountedAmount = Math.min(tongTien * discountValue / 100, maxDiscountAmount);
    }

}
