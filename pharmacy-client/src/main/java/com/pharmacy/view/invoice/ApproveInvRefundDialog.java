package com.pharmacy.view.invoice;

import com.pharmacy.shared.dto.request.InvoiceDetailRefundRequest;
import com.pharmacy.shared.dto.response.InvoiceDetailResponse;
import com.pharmacy.shared.dto.response.InvoiceRefundResponse;
import com.pharmacy.shared.service.RefundService;
import com.pharmacy.util.ClientContext;
import com.pharmacy.util.ClientSecurityContext;
import com.pharmacy.util.FormatUtil;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class ApproveInvRefundDialog extends javax.swing.JDialog {

	private final RefundService refundService;

	private InvoiceRefundResponse invoiceReturn;

	private List<InvoiceDetailResponse> listAll;

	private DefaultTableModel modelChiTietHDT;

	private boolean isViewOnly = false;

	public ApproveInvRefundDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);

		refundService = ClientContext.getService(RefundService.class);
		initComponents();
		
		btnConfirm.addActionListener(e -> processApproveInvoiceReturn());
	}

	private void processApproveInvoiceReturn() {
		if (table.isEditing()) {
			table.getCellEditor().stopCellEditing();
		}

		int rowCount = modelChiTietHDT.getRowCount();
		if (rowCount == 0) {
			JOptionPane.showMessageDialog(this, "Không có dữ liệu để phê duyệt.", "Thông báo",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		List<InvoiceDetailRefundRequest> listToUpdate = new ArrayList<>();

		for (int i = 0; i < rowCount; i++) {
			String huongXuLy = (String) modelChiTietHDT.getValueAt(i, 4);

			Object lyDoObj = modelChiTietHDT.getValueAt(i, 5);
			String lyDo = (lyDoObj != null) ? lyDoObj.toString() : "";

			InvoiceDetailResponse detailOriginal = listAll.get(i);
			InvoiceDetailRefundRequest invoiceDetailRefundRequest = new InvoiceDetailRefundRequest();
			invoiceDetailRefundRequest.setInvoiceRefundCode(detailOriginal.getInvoiceId());
			invoiceDetailRefundRequest.setId(detailOriginal.getId());
			invoiceDetailRefundRequest.setQuantity(detailOriginal.getQuantity());
			invoiceDetailRefundRequest.setBatchId(detailOriginal.getBatch().getId());

			if ("Chờ xử lý".equals(huongXuLy)) {
				JOptionPane.showMessageDialog(this,
						"Sản phẩm ở dòng " + (i + 1)
								+ " chưa xác nhận hướng xử lý.\nVui lòng chọn 'Bán tiếp' hoặc 'Hủy hàng'.",
						"Chưa hoàn tất", JOptionPane.WARNING_MESSAGE);

				table.setRowSelectionInterval(i, i);
				table.scrollRectToVisible(table.getCellRect(i, 4, true));
				return;
			}

			if ("Hủy hàng".equals(huongXuLy) && lyDo.isEmpty()) {
				JOptionPane.showMessageDialog(this,
						"Bạn đã chọn 'Hủy hàng' ở dòng " + (i + 1) + ".\nVui lòng nhập lý do hủy.", "Thiếu thông tin",
						JOptionPane.WARNING_MESSAGE);

				table.setRowSelectionInterval(i, i);
				table.editCellAt(i, 5);
				table.getEditorComponent().requestFocus();
				return;
			}

			invoiceDetailRefundRequest.setResolution(huongXuLy);
			invoiceDetailRefundRequest.setReason(lyDo);
			listToUpdate.add(invoiceDetailRefundRequest);
		}

		int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn phê duyệt phiếu trả hàng này?\n"
				+ "Kho sẽ được cập nhật dựa trên quyết định của bạn và không thể chỉnh sửa sau khi đã thay đổi.",
				"Xác nhận phê duyệt", JOptionPane.YES_NO_OPTION);

		if (confirm == JOptionPane.YES_OPTION) {
			try {
				refundService.approveInvoiceRefund(listToUpdate);

				JOptionPane.showMessageDialog(this, "Phê duyệt thành công!");

				this.dispose();
			} catch (IllegalArgumentException e) {
				JOptionPane.showMessageDialog(this, e.getMessage());
			}
		}
	}

	public void setInitData(InvoiceRefundResponse invoiceReturn, List<InvoiceDetailResponse> list) {
		this.invoiceReturn = invoiceReturn;
		this.listAll = list;

		this.isViewOnly = invoiceReturn.isApproved() || !ClientSecurityContext.hasPermission("INVOICE_RETURN_APPROVE");
		btnConfirm.setEnabled(!isViewOnly);

		viewData();
		viewTable();
	}

	private void viewTable() {
		modelChiTietHDT.setRowCount(0);

		int index = 1;
		for (InvoiceDetailResponse inv : listAll) {
			modelChiTietHDT.addRow(new Object[] { index, inv.getMedicine().getMedicineName(), inv.getBatch().getBatchNumber(), inv.getQuantity(), "", "" });
		}
	}

	private void viewData() {
		txtCus.setText(String.format("%s - %s", invoiceReturn.getCustomer().getFullName(),
				invoiceReturn.getCustomer().getPhoneNumber()));

		txtHDBan.setText(String.format("%s - %s", invoiceReturn.getInvoice().getInvoiceCode(),
				FormatUtil.formatDate(invoiceReturn.getInvoice().getCreatedDate())));

		txtNhanVien.setText(String.format("%s - %s", invoiceReturn.getEmployee().getEmployeeCode(),
				invoiceReturn.getEmployee().getFullName()));

		txtLyDo.setText(invoiceReturn.getReason());

		txtNgayLap.setText(FormatUtil.formatDate(invoiceReturn.getCreatedDate()));

		txtTienHoan.setText(FormatUtil.formatVND(invoiceReturn.getRefundAmount()) + " VND");

		txtSoHDTra.setText(invoiceReturn.getReturnInvoiceCode());

		updateStatusLabel();
	}

	private void updateStatusLabel() {
		boolean isApproved = invoiceReturn.isApproved();

		String statusText = isApproved ? "Đã xử lý" : "Chờ xử lý";
		lblValueStatus.setText(statusText);

		Color foregroundColor;
		Color backgroundColor;

		if (isApproved) {
			foregroundColor = new Color(0, 102, 0);
			backgroundColor = new Color(197, 255, 197);
		} else {
			foregroundColor = new Color(204, 102, 0);
			backgroundColor = new Color(255, 240, 197);
		}

		lblValueStatus.setForeground(foregroundColor);
		lblValueStatus.setBackground(backgroundColor);

		lblValueStatus.setOpaque(true);

		lblValueStatus.setFont(lblValueStatus.getFont().deriveFont(Font.BOLD, 14f));
		lblValueStatus.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
	}

	private void initComponents() {

		jPanel1 = new javax.swing.JPanel();
		lblTitle = new JLabel();
		jPanel2 = new javax.swing.JPanel();
		btnConfirm = new javax.swing.JButton();
		jPanel3 = new javax.swing.JPanel();
		jPanel4 = new javax.swing.JPanel();
		lblHDTra = new JLabel();
		txtNhanVien = new JTextField();
		lblNgayLap = new JLabel();
		txtNgayLap = new JTextField();
		lblEmp = new JLabel();
		txtSoHDTra = new JTextField();
		lblCus = new JLabel();
		txtCus = new JTextField();
		lblHDBan = new JLabel();
		txtTienHoan = new JTextField();
		lblStatus = new JLabel();
		lblValueStatus = new JLabel();
		lblTienHoan = new JLabel();
		txtHDBan = new JTextField();
		lblLyDo = new JLabel();
		txtLyDo = new JTextField();
		jPanel5 = new javax.swing.JPanel();
		jScrollPane1 = new javax.swing.JScrollPane();

		txtCus.setEditable(false);
		txtHDBan.setEditable(false);
		txtLyDo.setEditable(false);
		txtNgayLap.setEditable(false);
		txtNhanVien.setEditable(false);
		txtSoHDTra.setEditable(false);
		txtTienHoan.setEditable(false);

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(750, 650));

		jPanel1.setBackground(new Color(255, 255, 255));
		jPanel1.setPreferredSize(new Dimension(681, 50));

		lblTitle.setFont(new Font("Segoe UI", 1, 20)); // NOI18N
		lblTitle.setForeground(new Color(0, 0, 255));
		lblTitle.setText("THÔNG TIN HÓA ĐƠN TRẢ HÀNG");
		jPanel1.add(lblTitle);

		getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_START);

		jPanel2.setBackground(new Color(255, 255, 255));
		jPanel2.setPreferredSize(new Dimension(681, 50));

		btnConfirm.setBackground(new Color(255, 153, 0));
		btnConfirm.setFont(new Font("Segoe UI", 1, 14)); // NOI18N
		btnConfirm.setForeground(new Color(255, 255, 255));
		btnConfirm.setText("Xác nhận");

		javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
						.addContainerGap(542, Short.MAX_VALUE).addComponent(btnConfirm,
								javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap()));
		jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel2Layout.createSequentialGroup().addContainerGap()
						.addComponent(btnConfirm, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
						.addContainerGap()));

		getContentPane().add(jPanel2, java.awt.BorderLayout.PAGE_END);

		jPanel3.setLayout(new java.awt.BorderLayout());

		jPanel4.setBackground(new Color(255, 255, 255));
		jPanel4.setPreferredSize(new Dimension(681, 250));

		lblHDTra.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
		lblHDTra.setText("Số hoá đơn trả:");

		lblNgayLap.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
		lblNgayLap.setText("Ngày lập:");

		lblEmp.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
		lblEmp.setText("Nhân viên tiếp nhận:");

		lblCus.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
		lblCus.setText("Khách hàng:");

		lblHDBan.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
		lblHDBan.setText("Hóa đơn bán:");

		lblStatus.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
		lblStatus.setText("Trạng thái:");

		lblValueStatus.setText("Chờ xử lý");

		lblTienHoan.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
		lblTienHoan.setText("Tiền hoàn:");

		lblLyDo.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
		lblLyDo.setText("Lý do:");

		javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
		jPanel4.setLayout(jPanel4Layout);
		jPanel4Layout.setHorizontalGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel4Layout.createSequentialGroup().addContainerGap().addGroup(jPanel4Layout
						.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(jPanel4Layout.createSequentialGroup().addGroup(jPanel4Layout
								.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel4Layout
										.createSequentialGroup().addGroup(jPanel4Layout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
												.addComponent(lblEmp, javax.swing.GroupLayout.PREFERRED_SIZE, 125,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(lblHDTra, javax.swing.GroupLayout.PREFERRED_SIZE, 91,
														javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addGroup(jPanel4Layout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
												.addComponent(txtSoHDTra, javax.swing.GroupLayout.DEFAULT_SIZE, 210,
														Short.MAX_VALUE)
												.addComponent(txtNhanVien, javax.swing.GroupLayout.DEFAULT_SIZE, 210,
														Short.MAX_VALUE))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addGroup(jPanel4Layout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
												.addComponent(lblNgayLap, javax.swing.GroupLayout.Alignment.TRAILING,
														javax.swing.GroupLayout.PREFERRED_SIZE, 76,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(lblCus, javax.swing.GroupLayout.Alignment.TRAILING,
														javax.swing.GroupLayout.PREFERRED_SIZE, 76,
														javax.swing.GroupLayout.PREFERRED_SIZE)))
								.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
										jPanel4Layout.createSequentialGroup().addGroup(jPanel4Layout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
												.addGroup(jPanel4Layout.createSequentialGroup()
														.addComponent(lblTienHoan,
																javax.swing.GroupLayout.PREFERRED_SIZE, 125,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
														.addComponent(txtTienHoan, javax.swing.GroupLayout.DEFAULT_SIZE,
																210, Short.MAX_VALUE))
												.addGroup(jPanel4Layout.createSequentialGroup()
														.addComponent(lblHDBan, javax.swing.GroupLayout.PREFERRED_SIZE,
																125, javax.swing.GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
														.addComponent(txtHDBan)))
												.addGap(40, 40, 40).addComponent(lblStatus,
														javax.swing.GroupLayout.PREFERRED_SIZE, 76,
														javax.swing.GroupLayout.PREFERRED_SIZE)))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanel4Layout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
										.addComponent(txtNgayLap)
										.addComponent(txtCus, javax.swing.GroupLayout.DEFAULT_SIZE, 200,
												Short.MAX_VALUE)
										.addComponent(lblValueStatus, javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
						.addGroup(jPanel4Layout.createSequentialGroup()
								.addComponent(lblLyDo, javax.swing.GroupLayout.PREFERRED_SIZE, 125,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addComponent(txtLyDo)))
						.addContainerGap()));
		jPanel4Layout.setVerticalGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel4Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(lblHDTra).addComponent(lblNgayLap)
								.addComponent(txtNgayLap, javax.swing.GroupLayout.PREFERRED_SIZE, 30,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(txtSoHDTra, javax.swing.GroupLayout.PREFERRED_SIZE, 30,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGap(18, 18, 18)
						.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(lblEmp)
								.addComponent(txtNhanVien, javax.swing.GroupLayout.PREFERRED_SIZE, 30,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(lblCus).addComponent(txtCus, javax.swing.GroupLayout.PREFERRED_SIZE, 30,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGap(18, 18, 18)
						.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(lblHDBan).addComponent(lblStatus)
								.addComponent(lblValueStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 30,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(txtHDBan, javax.swing.GroupLayout.PREFERRED_SIZE, 30,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGap(18, 18, 18)
						.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(lblTienHoan).addComponent(txtTienHoan,
										javax.swing.GroupLayout.PREFERRED_SIZE, 30,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(lblLyDo).addComponent(txtLyDo, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addContainerGap(23, Short.MAX_VALUE)));

		jPanel3.add(jPanel4, java.awt.BorderLayout.PAGE_START);

		jPanel5.setPreferredSize(new Dimension(681, 220));
		jPanel5.setLayout(new java.awt.BorderLayout());

		final String[] HUONG_XU_LY_OPTIONS = { "Chờ xử lý", "Bán tiếp", "Hủy hàng" };

		modelChiTietHDT = new DefaultTableModel(
				new String[] { "STT", "Tên thuốc", "Số lô", "SL Trả", "Hướng xử lý", "Lý do" }, 0) {

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if (columnIndex == 0 || columnIndex == 3)
					return Integer.class;
				return Object.class;
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				if (isViewOnly)
					return column == 5 || false;

				return column == 4 || column == 5;
			}
		};

		table = new JTable(modelChiTietHDT);

		TableColumn xuLyColumn = table.getColumnModel().getColumn(4);
		Font fontBold = new Font("Segoe UI", Font.BOLD, 13);

		JComboBox<String> cboXuLy = new JComboBox<>(HUONG_XU_LY_OPTIONS);
		xuLyColumn.setCellEditor(new DefaultCellEditor(cboXuLy));

		xuLyColumn.setCellRenderer(new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
						column);
				label.setHorizontalAlignment(JLabel.CENTER);
				label.setFont(fontBold);

				String status = (value != null) ? value.toString() : "Chờ xử lý";

				if (!isSelected) {
					switch (status) {
					case "Bán tiếp":
						label.setForeground(new Color(0, 128, 0));
						break;
					case "Hủy hàng":
						label.setForeground(new Color(200, 0, 0));
						break;
					case "Chờ xử lý":
					default:
						label.setForeground(new Color(255, 140, 0));
						break;
					}
				} else {
					label.setForeground(table.getSelectionForeground());
				}
				return label;
			}
		});

		styleTable(table);

		JTextField txtLyDo = new JTextField();
		txtLyDo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		table.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(txtLyDo));

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

		columnModel.getColumn(4).setPreferredWidth(120);
		columnModel.getColumn(4).setMaxWidth(150);
		
		columnModel.getColumn(5).setMinWidth(150);

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);

		DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
		leftRenderer.setHorizontalAlignment(JLabel.LEFT);
		leftRenderer.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

		columnModel.getColumn(0).setCellRenderer(centerRenderer);
		columnModel.getColumn(1).setCellRenderer(leftRenderer);
		columnModel.getColumn(2).setCellRenderer(centerRenderer);
		columnModel.getColumn(3).setCellRenderer(centerRenderer);
		columnModel.getColumn(5).setCellRenderer(leftRenderer);
	}

	private javax.swing.JButton btnConfirm;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JScrollPane jScrollPane1;
	private JLabel lblCus;
	private JLabel lblEmp;
	private JLabel lblHDBan;
	private JLabel lblHDTra;
	private JLabel lblLyDo;
	private JLabel lblNgayLap;
	private JLabel lblStatus;
	private JLabel lblTienHoan;
	private JLabel lblTitle;
	private JLabel lblValueStatus;
	private JTable table;
	private JTextField txtCus;
	private JTextField txtHDBan;
	private JTextField txtLyDo;
	private JTextField txtNgayLap;
	private JTextField txtNhanVien;
	private JTextField txtSoHDTra;
	private JTextField txtTienHoan;

}
