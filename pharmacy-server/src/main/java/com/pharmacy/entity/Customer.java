package com.pharmacy.entity;

import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "customerRank")
@Entity
@Table(name = "tbl_customer")
public class Customer extends BaseEntity {

    @Column(name = "full_name", columnDefinition = "VARCHAR(255)")
    private String fullName;

    @Column(name = "phone_number", length = 20, unique = true)
    private String phoneNumber;

    @Column(name = "reward_points")
    private Integer rewardPoints = 0;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_rank_id")
    private CustomerRank customerRank;

}
