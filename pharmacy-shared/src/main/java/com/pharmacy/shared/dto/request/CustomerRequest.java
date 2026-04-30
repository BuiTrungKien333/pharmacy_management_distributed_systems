package com.pharmacy.shared.dto.request;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class CustomerRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fullName;

    private String phoneNumber;

    private int rewardPoints;

    public void setRewardPoints(double totalMoney) {
        this.rewardPoints = (int) totalMoney / 1000;
    }
}
