package com.pharmacy.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"batch", "product", "invoiceReturn"})
@Entity
@Table(name = "tbl_invoice_detail_return")
public class InvoiceDetailReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "unit_price")
    private double unitPrice;

    @Column(name = "total_amount")
    private double totalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_number")
    private Batch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_invoice_code")
    private InvoiceReturn invoiceReturn;

    @Column(name = "status")
    private boolean status;

    @Column(name = "resolution", columnDefinition = "VARCHAR(255)")
    private String resolution;

    @Column(name = "reason", columnDefinition = "VARCHAR(255)")
    private String reason;

}
