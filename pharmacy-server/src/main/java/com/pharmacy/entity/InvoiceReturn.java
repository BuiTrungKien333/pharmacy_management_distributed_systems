package com.pharmacy.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "invoice", "customer", "employee" })
@Builder
@Entity
@Table(name = "tbl_invoice_return")
public class InvoiceReturn {

	@Id
	@Column(name = "return_invoice_code", length = 100)
	private String returnInvoiceCode;

	@Column(name = "created_date")
	private LocalDateTime createdDate;

	@Column(name = "refund_amount")
	private double refundAmount;

	@Column(name = "reason", columnDefinition = "VARCHAR(255)")
	private String reason;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "invoice_code")
	private Invoice invoice;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id")
	private Customer customer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "employee_id")
	private Employee employee;

	@Column(name = "is_approved")
	private boolean approved;

}
