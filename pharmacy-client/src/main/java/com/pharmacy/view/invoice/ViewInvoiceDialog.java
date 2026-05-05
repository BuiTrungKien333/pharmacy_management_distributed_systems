package com.pharmacy.view.invoice;

import com.pharmacy.shared.dto.response.InvoiceDetailResponse;
import com.pharmacy.shared.dto.response.InvoiceResponse;
import com.pharmacy.util.FormatUtil;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

public class ViewInvoiceDialog extends javax.swing.JDialog {

	public ViewInvoiceDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		initComponents();

		btnClose.addActionListener(e -> this.dispose());
	}

	private void viewTable(List<InvoiceDetailResponse> list) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setRowCount(0);

		int index = 1;
		for (InvoiceDetailResponse inv : list) {
			Object[] obj = new Object[] { index++, inv.getMedicine().getMedicineName(), inv.getBatch().getBatchNumber(), inv.getQuantity(), FormatUtil.formatVND(inv.getUnitPrice()),
					FormatUtil.formatVND(inv.getTotalAmount()) };
			model.addRow(obj);
		}
	}

	public void setInitData(InvoiceResponse invoice, List<InvoiceDetailResponse> list) {
		viewData(invoice);
		viewTable(list);
	}

	private void viewData(InvoiceResponse invoice) {
		txtMaHD.setText(invoice.getInvoiceCode());

		txtNhanVien.setText(String.format("%s - %s", invoice.getEmployee().getEmployeeCode(),
				invoice.getEmployee().getFullName()));

		txtNgayLap.setText(FormatUtil.formatDate(invoice.getCreatedDate()));

		txtKhachHang.setText(invoice.getCustomer() == null ? "Vãng lai"
				: String.format("%s - %s", invoice.getCustomer().getFullName(),
						invoice.getCustomer().getPhoneNumber()));

		txtTongTien.setText(FormatUtil.formatVND(invoice.getTotalPayableAmount()) + " VND");

//		txtVoucher.setText(invoice.getVoucher() != null ? invoice.getMaVoucher() : "Không có");

		lblValueDaTra.setText(invoice.isReturned() ? "Đã trả" : "");
	}

	private void initComponents() {

		jPanel1 = new javax.swing.JPanel();
		jLabel1 = new JLabel();
		jPanel2 = new javax.swing.JPanel();
		btnClose = new javax.swing.JButton();
		jPanel3 = new javax.swing.JPanel();
		jPanel4 = new javax.swing.JPanel();
		jLabel2 = new JLabel();
		txtMaHD = new javax.swing.JTextField();
		jLabel3 = new JLabel();
		txtKhachHang = new javax.swing.JTextField();
		jLabel4 = new JLabel();
		txtNhanVien = new javax.swing.JTextField();
		jLabel5 = new JLabel();
		txtNgayLap = new javax.swing.JTextField();
		jLabel6 = new JLabel();
		txtVoucher = new javax.swing.JTextField();
		jLabel7 = new JLabel();
		txtTongTien = new javax.swing.JTextField();
		jLabel8 = new JLabel();
		jLabel9 = new JLabel();
		jLabel10 = new JLabel();
		lblValueDaTra = new JLabel();
		jPanel5 = new javax.swing.JPanel();
		jScrollPane1 = new javax.swing.JScrollPane();
		table = new JTable();

		txtKhachHang.setEditable(false);
		txtMaHD.setEditable(false);
		txtNgayLap.setEditable(false);
		txtNhanVien.setEditable(false);
		txtTongTien.setEditable(false);
		txtVoucher.setEditable(false);

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		jPanel1.setBackground(new Color(255, 255, 255));
		jPanel1.setPreferredSize(new Dimension(734, 50));

		jLabel1.setFont(new Font("Segoe UI", 1, 18)); // NOI18N
		jLabel1.setForeground(new Color(0, 0, 255));
		jLabel1.setText("THÔNG TIN HÓA ĐƠN");
		jPanel1.add(jLabel1);

		getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_START);

		jPanel2.setBackground(new Color(255, 255, 255));
		jPanel2.setPreferredSize(new Dimension(734, 50));

		btnClose.setBackground(new Color(242, 242, 242));
		btnClose.setFont(new Font("Segoe UI", 1, 14)); // NOI18N
		btnClose.setText("Close");

		javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
						.addContainerGap(638, Short.MAX_VALUE).addComponent(btnClose,
								javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap()));
		jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel2Layout.createSequentialGroup().addContainerGap()
						.addComponent(btnClose, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
						.addContainerGap()));

		getContentPane().add(jPanel2, java.awt.BorderLayout.PAGE_END);

		jPanel3.setLayout(new java.awt.BorderLayout());

		jPanel4.setBackground(new Color(255, 255, 255));
		jPanel4.setPreferredSize(new Dimension(734, 200));

		jLabel2.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
		jLabel2.setText("Mã hóa đơn");

		jLabel3.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
		jLabel3.setText("Ngày lập");

		jLabel4.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
		jLabel4.setText("Nhân viên");

		jLabel5.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
		jLabel5.setText("Khách hàng");

		jLabel6.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
		jLabel6.setText("Voucher");

		jLabel7.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
		jLabel7.setText("Tổng tiền thanh toán");

		jLabel8.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
		jLabel8.setText("Trạng thái");

		jLabel9.setFont(new Font("Segoe UI", 3, 15)); // NOI18N
		jLabel9.setForeground(new Color(0, 153, 51));
		jLabel9.setText("Đã bán");

		jLabel10.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
		jLabel10.setText("Đã trả");

		lblValueDaTra.setFont(new Font("Segoe UI", 3, 15)); // NOI18N
		lblValueDaTra.setForeground(new Color(255, 51, 0));
		lblValueDaTra.setText("Đã trả");

		javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
		jPanel4.setLayout(jPanel4Layout);
		jPanel4Layout.setHorizontalGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel4Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(jPanel4Layout.createSequentialGroup()
										.addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 84,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(txtMaHD, javax.swing.GroupLayout.PREFERRED_SIZE, 250,
												javax.swing.GroupLayout.PREFERRED_SIZE))
								.addGroup(jPanel4Layout.createSequentialGroup()
										.addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 84,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(txtNhanVien, javax.swing.GroupLayout.PREFERRED_SIZE, 250,
												javax.swing.GroupLayout.PREFERRED_SIZE))
								.addGroup(jPanel4Layout.createSequentialGroup()
										.addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 84,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(txtVoucher, javax.swing.GroupLayout.PREFERRED_SIZE, 250,
												javax.swing.GroupLayout.PREFERRED_SIZE))
								.addGroup(jPanel4Layout.createSequentialGroup()
										.addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 84,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 114,
												javax.swing.GroupLayout.PREFERRED_SIZE)))
						.addGap(41, 41, 41)
						.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(jPanel4Layout.createSequentialGroup()
										.addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 75,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(18, 18, 18).addComponent(txtNgayLap))
								.addGroup(jPanel4Layout.createSequentialGroup()
										.addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 75,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(18, 18, 18).addComponent(txtKhachHang))
								.addGroup(jPanel4Layout.createSequentialGroup()
										.addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 130,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(txtTongTien))
								.addGroup(jPanel4Layout.createSequentialGroup()
										.addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 73,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(18, 18, 18)
										.addComponent(lblValueDaTra, javax.swing.GroupLayout.PREFERRED_SIZE, 163,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(0, 87, Short.MAX_VALUE)))
						.addContainerGap()));
		jPanel4Layout.setVerticalGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel4Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabel2)
								.addComponent(txtMaHD, javax.swing.GroupLayout.PREFERRED_SIZE, 30,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabel3).addComponent(txtNgayLap, javax.swing.GroupLayout.PREFERRED_SIZE,
										30, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGap(18, 18, 18)
						.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabel4)
								.addComponent(txtNhanVien, javax.swing.GroupLayout.PREFERRED_SIZE, 30,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabel5).addComponent(txtKhachHang,
										javax.swing.GroupLayout.PREFERRED_SIZE, 30,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGap(18, 18, 18)
						.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(txtVoucher, javax.swing.GroupLayout.PREFERRED_SIZE, 30,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabel6).addComponent(jLabel7)
								.addComponent(txtTongTien, javax.swing.GroupLayout.PREFERRED_SIZE, 30,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGap(27, 27, 27)
						.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabel8).addComponent(jLabel9).addComponent(jLabel10)
								.addComponent(lblValueDaTra, javax.swing.GroupLayout.PREFERRED_SIZE, 29,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addContainerGap(12, Short.MAX_VALUE)));

		jPanel3.add(jPanel4, java.awt.BorderLayout.PAGE_START);

		jPanel5.setPreferredSize(new Dimension(734, 280));
		jPanel5.setLayout(new java.awt.BorderLayout());

		table.setModel(new DefaultTableModel(
				new Object[][] { { null, null, null, null, null, null }, { null, null, null, null, null, null },
						{ null, null, null, null, null, null }, { null, null, null, null, null, null } },
				new String[] { "STT", "Tên thuốc", "Số lô", "SL Bán", "Giá bán", "Thành tiền" }) {

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		});

		styleTable(table);

		jScrollPane1.setViewportView(table);

		jPanel5.add(jScrollPane1, java.awt.BorderLayout.CENTER);

		jPanel3.add(jPanel5, java.awt.BorderLayout.CENTER);

		getContentPane().add(jPanel3, java.awt.BorderLayout.CENTER);

		pack();
	}

	private void styleTable(JTable table) {
		table.setRowHeight(40);
		table.setShowVerticalLines(false);
		table.setGridColor(new Color(230, 230, 230));
		table.setSelectionBackground(new Color(220, 238, 255));
		table.setSelectionForeground(Color.BLACK);
		table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		table.setFillsViewportHeight(true);
		
		JTableHeader header = table.getTableHeader();
		header.setPreferredSize(new Dimension(header.getWidth(), 30));
		header.setFont(new Font("Segoe UI", Font.BOLD, 14));
		header.setBackground(new Color(46, 153, 217));
		header.setForeground(Color.WHITE);
		((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

		TableColumnModel columnModel = table.getColumnModel();

		columnModel.getColumn(0).setPreferredWidth(40);
		columnModel.getColumn(0).setMaxWidth(40);

		columnModel.getColumn(1).setMinWidth(150);

		columnModel.getColumn(2).setPreferredWidth(220);
		columnModel.getColumn(2).setMaxWidth(250);

		columnModel.getColumn(3).setPreferredWidth(70);
		columnModel.getColumn(3).setMaxWidth(80);

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);

		DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
		leftRenderer.setHorizontalAlignment(JLabel.LEFT);
		leftRenderer.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

		for (int i = 0; i < table.getColumnCount(); i++) {
			columnModel.getColumn(i).setCellRenderer(i == 1 ? leftRenderer : centerRenderer);
		}
	}

	private javax.swing.JButton btnClose;
	private JLabel jLabel1;
	private JLabel jLabel10;
	private JLabel jLabel2;
	private JLabel jLabel3;
	private JLabel jLabel4;
	private JLabel jLabel5;
	private JLabel jLabel6;
	private JLabel jLabel7;
	private JLabel jLabel8;
	private JLabel jLabel9;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JScrollPane jScrollPane1;
	private JLabel lblValueDaTra;
	private JTable table;
	private javax.swing.JTextField txtKhachHang;
	private javax.swing.JTextField txtMaHD;
	private javax.swing.JTextField txtNgayLap;
	private javax.swing.JTextField txtNhanVien;
	private javax.swing.JTextField txtTongTien;
	private javax.swing.JTextField txtVoucher;
}
