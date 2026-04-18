package com.pharmacy.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "customer", "employee", "voucher" })
@Entity
@Table(name = "tbl_invoice")
public class Invoice {

	@Id
	@Column(name = "invoice_code", length = 100)
	private String invoiceCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id")
	private Customer customer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "employee_id")
	private Employee employee;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "voucher_id")
	private Voucher voucher;

	@Column(name = "created_date")
	private LocalDateTime createdDate;

	@Column(name = "total_goods_amount")
	private double totalGoodsAmount;

	@Column(name = "total_payable_amount")
	private double totalPayableAmount;

	@Column(name = "note", length = 255)
	private String note;

	@Column(name = "is_returned")
	private boolean returned;

}
