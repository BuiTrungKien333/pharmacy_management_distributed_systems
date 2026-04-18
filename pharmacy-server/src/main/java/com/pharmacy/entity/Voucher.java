package com.pharmacy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_voucher")
public class Voucher {

    @Id
    @Column(name = "voucher_code", length = 100)
    private String voucherCode;

    @Column(name = "discount_value")
    private int discountValue;

    @Column(name = "min_order_amount")
    private double minOrderAmount;

    @Column(name = "max_discount_amount")
    private double maxDiscountAmount;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "used_count")
    private int usedCount;

    @Column(name = "max_use_count")
    private int maxUseCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_rank_id")
    private CustomerRank customerRank;

    @Column(name = "total_discounted_amount")
    private double totalDiscountedAmount;

}
