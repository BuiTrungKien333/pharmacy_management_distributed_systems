package com.pharmacy.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "tbl_customer_rank")
public class CustomerRank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rank_name", columnDefinition = "VARCHAR(150)")
    private String rankName;

    @Column(name = "min_points")
    private Integer minPoints;

    @Column(name = "description", columnDefinition = "VARCHAR(150)")
    private String description;
}
