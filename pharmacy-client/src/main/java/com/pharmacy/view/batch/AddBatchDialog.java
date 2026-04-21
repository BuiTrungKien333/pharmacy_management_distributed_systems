package com.pharmacy.view.batch;

import com.pharmacy.shared.dto.request.BatchCreateRequest;
import com.pharmacy.shared.dto.response.MedicineResponse;
import com.pharmacy.shared.dto.response.SupplierMiniResponse;
import com.pharmacy.shared.service.BatchService;
import com.pharmacy.shared.service.MedicineService;
import com.pharmacy.shared.service.SupplierService;
import com.pharmacy.shared.util.enums.MedicineType;
import com.pharmacy.util.ClientContext;
import com.pharmacy.util.FormatUtil;
import com.pharmacy.util.HelperImageIcon;


import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class AddBatchDialog extends javax.swing.JDialog {

    private BatchUI batchUI;
    private BatchService batchService;
    private SupplierService supplierService;
    private MedicineService medicineService;
    private MedicineResponse medicineResponse;

    public AddBatchDialog(java.awt.Frame parent, boolean modal, BatchUI batchUI) {
        super(parent, modal);

        this.batchUI = batchUI;
        this.batchService = ClientContext.getService(BatchService.class);
        this.supplierService = ClientContext.getService(SupplierService.class);
        this.medicineService = ClientContext.getService(MedicineService.class);

        medicineResponse = new MedicineResponse();

        initComponents();

        initEdit();

        initEvent();

        SwingUtilities.invokeLater(() -> txtBarcode.requestFocusInWindow());
    }

    private void initEvent() {

        btnSave.addActionListener(e -> saveBatch());

        btnClear.addActionListener(e -> clearData());

        txtGiaNhap.addActionListener(e -> {
            txtGiaBan.selectAll();
            txtGiaBan.requestFocus();
        });

        txtGiaNhap.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                txtThanhTien.setText(
                        FormatUtil.formatVND(batchService.calculateMoney(txtSoLuong.getText(), txtGiaNhap.getText())));

                suggestGiaBan();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                txtThanhTien.setText(
                        FormatUtil.formatVND(batchService.calculateMoney(txtSoLuong.getText(), txtGiaNhap.getText())));

                suggestGiaBan();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        txtSoLuong.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                txtThanhTien.setText(
                        FormatUtil.formatVND(batchService.calculateMoney(txtSoLuong.getText(), txtGiaNhap.getText())));
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                txtThanhTien.setText(
                        FormatUtil.formatVND(batchService.calculateMoney(txtSoLuong.getText(), txtGiaNhap.getText())));
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        txtBarcode.addActionListener(e -> {
            String barcode = txtBarcode.getText().trim();
            if (barcode.isEmpty())
                return;

            try {
                MedicineResponse medicineResponse = medicineService.getMedicineByBarcode(barcode);
                this.medicineResponse = medicineResponse;
                showProdChosen();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
                txtBarcode.selectAll();
                txtBarcode.requestFocus();
            }
        });
    }

    private void suggestGiaBan() {
        try {
            if (medicineResponse == null)
                return;

            MedicineType medicineType = medicineResponse.getMedicineType();
            double price = Double.parseDouble(txtGiaNhap.getText());

            if (medicineType == MedicineType.MEDICINE)
                price *= 1.4;
            else if (medicineType == MedicineType.FUNCTIONAL_FOODS)
                price *= 1.6;
            else
                price *= 2;

            txtGiaBan.setText(String.format("%.0f", price));
        } catch (NumberFormatException e) {
        }
    }

    private void showProdChosen() {
        txtMaThuoc.setText(String.format("%06d - %s", medicineResponse.getId(), medicineResponse.getMedicineName()));
        lblIcon.setIcon(HelperImageIcon.scaleIcon(medicineResponse.getAvatarUrl(), 190, 190));
        txtDonViTinh.setText(medicineResponse.getMeasuringUnit());
    }

    private void saveBatch() {
        try {
            BatchCreateRequest request = new BatchCreateRequest();

            if (txtMaThuoc.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng quét mã barcode để thêm lô mới.");
                txtBarcode.requestFocus();
                return;
            }

            request.setMedicineId(medicineResponse.getId());
            request.setEmployeeId(3L);
            request.setSupplierId(((SupplierMiniResponse) cmbNhaCungCap.getSelectedItem()).getId());

            LocalDate ngaySX = FormatUtil.convertStringToDate(txtNgaySX.getText()); // dd/MM/yyyy
            LocalDate hanSD = FormatUtil.convertStringToDate(txtHanSuDung.getText()); // dd/MM/yyyy

            batchService.checkDate(ngaySX, hanSD);

            long months = ChronoUnit.MONTHS.between(LocalDate.now(), hanSD);
            if (months < 1) {
                JOptionPane.showMessageDialog(this, "Không thể nhập lô có hạn sử dụng dưới 30 ngày.");
                txtHanSuDung.requestFocus();
                txtHanSuDung.selectAll();
                return;
            } else if (months <= 18) {
                int confirm = JOptionPane.showConfirmDialog(this, "Hạn sử dụng của lô thuốc này chỉ còn: " + months
                        + " tháng.\n Bạn có chắc chắn muốn nhập lô ?");
                if (confirm != JOptionPane.YES_OPTION)
                    return;
            }

            request.setManufacturingDate(ngaySX);
            request.setExpirationDate(hanSD);

            try {
                request.setImportQuantity(Integer.parseInt(txtSoLuong.getText().trim()));
                request.setImportPrice(Double.parseDouble(txtGiaNhap.getText().trim()));
                request.setSellingPrice(Double.parseDouble(txtGiaBan.getText().trim()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Các trường số lượng và giá phải là số hợp lệ!");
            }

            batchService.checkQuantityAndPrice(request.getImportPrice(), request.getSellingPrice(), request.getImportQuantity(), request.getImportQuantity());
            batchService.addBatch(request);

            batchUI.refreshData();
            JOptionPane.showMessageDialog(this, "Thêm lô mới thành công!", "Success", JOptionPane.INFORMATION_MESSAGE);
            this.dispose();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void initEdit() {
        txtNhanVien.setEditable(false);
        txtNhanVien.setText("ALA010001 - Bùi Trung Kiên");
        txtThanhTien.setEditable(false);
        txtNgayNhap.setEditable(false);
        txtMaThuoc.setEditable(false);
        txtDonViTinh.setEditable(false);
        txtNgayNhap.setText(FormatUtil.formatDate(LocalDate.now()));
    }

    public void clearData() {
        txtBarcode.setText("");
        txtGiaNhap.setText("");
        txtGiaBan.setText("");
        txtHanSuDung.setText("");
        txtNgayNhap.setText(FormatUtil.formatDate(LocalDate.now()));
        txtNgaySX.setText("");
        txtNhanVien.setText("ALA010001 - Bùi Trung Kiên");
        txtSoLuong.setText("");
        txtDonViTinh.setText("");
        txtThanhTien.setText("");
        txtMaThuoc.setText("");

        cmbNhaCungCap.removeAllItems();
        supplierService.getAllSupplier().forEach(ncc -> cmbNhaCungCap.addItem(ncc));
        cmbNhaCungCap.setSelectedIndex(0);

        lblIcon.setIcon(HelperImageIcon.scaleIcon("/images/prod/default.png", 190, 190));
    }

    private void initComponents() {

        jPanel1 = new JPanel();
        lblTitle = new JLabel();
        jPanel2 = new JPanel();
        jPanel3 = new JPanel();
        btnClear = new JButton();
        btnSave = new JButton();
        jPanel6 = new JPanel();
        jPanel4 = new JPanel();
        lblBarcode = new JLabel();
        lblMaNCC = new JLabel();
        lblNgaySX = new JLabel();
        txtNhanVien = new JTextField();
        lblHanSuDung = new JLabel();
        txtNgaySX = new JTextField();
        lblNgayNhap = new JLabel();
        txtHanSuDung = new JTextField();
        lblNhanVien = new JLabel();
        txtNgayNhap = new JTextField();
        txtBarcode = new JTextField();
        cmbNhaCungCap = new JComboBox<>();
        lblMaNCC1 = new JLabel();
        txtMaThuoc = new JTextField();
        jPanel5 = new JPanel();
        lblSoLuong = new JLabel();
        txtSoLuong = new JTextField();
        lblGiaNhap = new JLabel();
        txtGiaBan = new JTextField();
        lblThanhTien = new JLabel();
        txtGiaNhap = new JTextField();
        lblIcon = new JLabel();
        txtDonViTinh = new JTextField();
        lblThanhTien1 = new JLabel();
        txtThanhTien = new JTextField();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new Color(255, 255, 255));
        jPanel1.setPreferredSize(new Dimension(765, 50));

        lblTitle.setFont(new Font("Segoe UI", 1, 18)); // NOI18N
        lblTitle.setForeground(new Color(51, 51, 255));
        lblTitle.setText("Thêm mới lô thuốc");
        jPanel1.add(lblTitle);

        getContentPane().add(jPanel1, BorderLayout.PAGE_START);

        jPanel2.setLayout(new BorderLayout());

        jPanel3.setBackground(new Color(255, 255, 255));
        jPanel3.setPreferredSize(new Dimension(765, 50));

        btnClear.setBackground(new Color(204, 204, 204));
        btnClear.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
        btnClear.setForeground(new Color(255, 255, 255));
        btnClear.setText("Clear");
        btnClear.setPreferredSize(new Dimension(100, 30));
        jPanel3.add(btnClear);

        btnSave.setBackground(new Color(51, 153, 0));
        btnSave.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
        btnSave.setForeground(new Color(255, 255, 255));
        btnSave.setText("Save");
        btnSave.setPreferredSize(new Dimension(100, 30));
        jPanel3.add(btnSave);

        jPanel2.add(jPanel3, BorderLayout.PAGE_END);

        jPanel6.setBackground(new Color(255, 255, 255));
        jPanel6.setLayout(new GridLayout(1, 2));

        jPanel4.setBackground(new Color(255, 255, 255));

        lblBarcode.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
        lblBarcode.setText("Barcode");

        lblMaNCC.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
        lblMaNCC.setText("Nhà cung cấp");

        lblNgaySX.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
        lblNgaySX.setText("Ngày sản xuất");

        lblHanSuDung.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
        lblHanSuDung.setText("Hạn sử dụng");

        lblNgayNhap.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
        lblNgayNhap.setText("Ngày nhập");

        lblNhanVien.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
        lblNhanVien.setText("Nhân viên nhập");

        supplierService.getAllSupplier().forEach(ncc -> cmbNhaCungCap.addItem(ncc));

        lblMaNCC1.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
        lblMaNCC1.setText("Mã thuốc - Tên thuốc");

        GroupLayout jPanel4Layout = new GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanel4Layout.createSequentialGroup().addGap(26, 26, 26).addGroup(jPanel4Layout
                                .createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtMaThuoc, GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                                .addComponent(lblMaNCC1, GroupLayout.PREFERRED_SIZE, 133,
                                        GroupLayout.PREFERRED_SIZE)
                                .addGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(lblBarcode, GroupLayout.PREFERRED_SIZE, 133,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblMaNCC, GroupLayout.PREFERRED_SIZE, 133,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblNgaySX, GroupLayout.PREFERRED_SIZE, 133,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtNhanVien, GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                                        .addComponent(lblHanSuDung, GroupLayout.PREFERRED_SIZE, 133,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtNgaySX, GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                                        .addComponent(lblNgayNhap, GroupLayout.PREFERRED_SIZE, 133,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtHanSuDung, GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                                        .addComponent(lblNhanVien, GroupLayout.PREFERRED_SIZE, 133,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtNgayNhap, GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                                        .addComponent(txtBarcode, GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                                        .addComponent(cmbNhaCungCap, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addContainerGap(141, Short.MAX_VALUE)));
        jPanel4Layout.setVerticalGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanel4Layout.createSequentialGroup().addContainerGap().addComponent(lblBarcode)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtBarcode, GroupLayout.PREFERRED_SIZE, 30,
                                GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18).addComponent(lblMaNCC1)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtMaThuoc, GroupLayout.PREFERRED_SIZE, 30,
                                GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                        .addComponent(lblMaNCC).addGap(12, 12, 12)
                        .addComponent(cmbNhaCungCap, GroupLayout.PREFERRED_SIZE, 34,
                                GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(lblNgaySX)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtNgaySX, GroupLayout.PREFERRED_SIZE, 30,
                                GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblHanSuDung).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtHanSuDung, GroupLayout.PREFERRED_SIZE, 30,
                                GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(lblNgayNhap)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtNgayNhap, GroupLayout.PREFERRED_SIZE, 30,
                                GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(lblNhanVien)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(txtNhanVien,
                                GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)));

        jPanel6.add(jPanel4);

        jPanel5.setBackground(new Color(255, 255, 255));

        lblSoLuong.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
        lblSoLuong.setText("Số lượng nhập");

        lblGiaNhap.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
        lblGiaNhap.setText("Giá nhập");

        lblThanhTien.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
        lblThanhTien.setText("Thành tiền");

        lblIcon.setIcon(HelperImageIcon.scaleIcon("/images/prod/default.png", 190, 190));
        lblIcon.setPreferredSize(new Dimension(190, 190));

        lblThanhTien1.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
        lblThanhTien1.setText("Giá bán ra");

        GroupLayout jPanel5Layout = new GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(jPanel5Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup().addContainerGap().addGroup(jPanel5Layout
                                .createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(lblThanhTien1, GroupLayout.PREFERRED_SIZE, 133,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtThanhTien, GroupLayout.PREFERRED_SIZE, 240,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtGiaBan, GroupLayout.PREFERRED_SIZE, 240,
                                        GroupLayout.PREFERRED_SIZE)
                                .addGroup(jPanel5Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(lblIcon, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addGroup(jPanel5Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                .addComponent(lblSoLuong, GroupLayout.PREFERRED_SIZE, 133,
                                                        GroupLayout.PREFERRED_SIZE)
                                                .addGroup(jPanel5Layout.createSequentialGroup()
                                                        .addComponent(txtSoLuong, GroupLayout.PREFERRED_SIZE, 240,
                                                                GroupLayout.PREFERRED_SIZE)
                                                        .addGap(18, 18, 18).addComponent(txtDonViTinh,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                GroupLayout.PREFERRED_SIZE))
                                                .addComponent(lblGiaNhap, GroupLayout.PREFERRED_SIZE, 133,
                                                        GroupLayout.PREFERRED_SIZE)
                                                .addComponent(lblThanhTien, GroupLayout.PREFERRED_SIZE, 133,
                                                        GroupLayout.PREFERRED_SIZE)
                                                .addComponent(txtGiaNhap, GroupLayout.PREFERRED_SIZE, 240,
                                                        GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap(139, Short.MAX_VALUE)));
        jPanel5Layout.setVerticalGroup(jPanel5Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup().addContainerGap().addComponent(lblSoLuong)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtDonViTinh)
                                .addComponent(txtSoLuong, GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE))
                        .addGap(18, 18, 18).addComponent(lblGiaNhap)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtGiaNhap, GroupLayout.PREFERRED_SIZE, 30,
                                GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12).addComponent(lblThanhTien)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtThanhTien, GroupLayout.PREFERRED_SIZE, 30,
                                GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12).addComponent(lblThanhTien1)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtGiaBan, GroupLayout.PREFERRED_SIZE, 30,
                                GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblIcon, GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        jPanel6.add(jPanel5);

        jPanel2.add(jPanel6, BorderLayout.CENTER);

        getContentPane().add(jPanel2, BorderLayout.CENTER);

        pack();
    }

    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnSave;
    private javax.swing.JComboBox<SupplierMiniResponse> cmbNhaCungCap;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JLabel lblBarcode;
    private javax.swing.JLabel lblGiaNhap;
    private javax.swing.JLabel lblHanSuDung;
    private javax.swing.JLabel lblIcon;
    private javax.swing.JLabel lblMaNCC;
    private javax.swing.JLabel lblMaNCC1;
    private javax.swing.JLabel lblNgayNhap;
    private javax.swing.JLabel lblNgaySX;
    private javax.swing.JLabel lblNhanVien;
    private javax.swing.JLabel lblSoLuong;
    private javax.swing.JLabel lblThanhTien;
    private javax.swing.JLabel lblThanhTien1;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JTextField txtBarcode;
    private javax.swing.JTextField txtDonViTinh;
    private javax.swing.JTextField txtGiaBan;
    private javax.swing.JTextField txtGiaNhap;
    private javax.swing.JTextField txtHanSuDung;
    private javax.swing.JTextField txtMaThuoc;
    private javax.swing.JTextField txtNgayNhap;
    private javax.swing.JTextField txtNgaySX;
    private javax.swing.JTextField txtNhanVien;
    private javax.swing.JTextField txtSoLuong;
    private javax.swing.JTextField txtThanhTien;

}
