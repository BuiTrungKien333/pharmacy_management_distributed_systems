package com.pharmacy.entity;

import com.pharmacy.shared.util.enums.BatchStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = { "medicine", "supplier", "employee" })
@Entity
@Table(name = "tbl_batch")
public class Batch extends BaseEntity {

	/*
	Pattern: LO-{medicineId}-{yyMMdd}-{seq}
	Ví dụ: LO-15-260420-003
	 */
	@Column(name = "batch_number", length = 100, unique = true)
	private String batchNumber;

	@Column(name = "manufacturing_date")
	private LocalDate manufacturingDate;

	@Column(name = "expiration_date")
	private LocalDate expirationDate;

	@Column(name = "import_date")
	private LocalDateTime importDate;

	@Column(name = "import_quantity")
	private int importQuantity;

	@Column(name = "remaining_quantity")
	private int remainingQuantity;

	@Column(name = "import_price")
	private double importPrice;

	@Column(name = "total_amount")
	private double totalAmount;

	@Column(name = "selling_price")
	private double sellingPrice;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "medicine_id")
	private Medicine medicine;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "supplier_id")
	private Supplier supplier;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "employee_id")
	private Employee employee;

	@Enumerated(EnumType.STRING)
	@Column(name = "batch_status", length = 50)
	private BatchStatus batchStatus;
}
