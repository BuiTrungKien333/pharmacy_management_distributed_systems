package com.pharmacy.view.refund;

import com.formdev.flatlaf.FlatClientProperties;
import com.pharmacy.shared.dto.request.InvoiceDetailRefundRequest;
import com.pharmacy.shared.dto.request.InvoiceRefundRequest;
import com.pharmacy.shared.dto.response.InvoiceDetailResponse;
import com.pharmacy.shared.dto.response.InvoiceRefundResponse;
import com.pharmacy.shared.dto.response.InvoiceResponse;
import com.pharmacy.shared.service.RefundService;
import com.pharmacy.util.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RefundUI extends JPanel {

    private final RefundService refundService;

    private DefaultTableModel modelSanPham;

    private DefaultTableModel modelHoaDon;

    private List<InvoiceDetailRefundRequest> listChosen = new ArrayList<>();

    private List<InvoiceDetailResponse> listAll = new ArrayList<>();

    private InvoiceResponse invoice;

    private double total = 0;

    private GenerateInvoiceReturn generateInvoice;

    public RefundUI() {
        this.refundService = ClientContext.getService(RefundService.class);

        initComponents();

        init();

        Translator.getInstance().addLanguageChangeListener(locale ->
                SwingUtilities.invokeLater(this::updateTexts)
        );

        updateTexts();

        initEvent();

        SwingUtilities.invokeLater(() -> txtSearch.requestFocusInWindow());
    }

    private void initEvent() {
        txtSearch.addActionListener(e -> {
            if (listAll.size() != 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng làm mới dữ liệu trước khi quét hoá đơn tiếp theo.");
                return;
            }

            String qrCode = txtSearch.getText().trim();
            if (qrCode.isEmpty() || qrCode.length() != 12)
                return;

            try {
                invoice = refundService.getInvoiceById(qrCode);
                loadData();

                listAll = refundService.getAllInvoiceDetailByQrCode(qrCode);
                viewTableHoaDon();

                TableColumn quantityColumn = tblHoaDon.getColumnModel().getColumn(3);
                QuantityCellEditor editor = (QuantityCellEditor) quantityColumn.getCellEditor();

                if (editor != null)
                    editor.setListInvoice(listAll);

            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            } finally {
                SwingUtilities.invokeLater(() -> {
                    txtSearch.setText("");
                    txtSearch.requestFocusInWindow();
                });
            }
        });

        btnThem.addActionListener(e -> addInvDetailForRefund());

        btnXoa.addActionListener(e -> removeInvDetailChosen());

        btnXoaRong.addActionListener(e -> clearTableChosen());

        btnTaoDon.addActionListener(e -> createInvoiceReturn());

        btnRefesh.addActionListener(e -> refreshData());

        modelHoaDon.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE && e.getColumn() == 3) {

                int row = e.getFirstRow();

                if (row < 0 || row >= modelHoaDon.getRowCount())
                    return;

                SwingUtilities.invokeLater(() -> {
                    try {
                        Object qtyObj = modelHoaDon.getValueAt(row, 3);
                        int soLuong = 0;
                        if (qtyObj instanceof Integer)
                            soLuong = (Integer) qtyObj;
                        else
                            soLuong = Integer.parseInt(qtyObj.toString());

                        double donGia = listAll.get(row).getUnitPrice();

                        double thanhTien = soLuong * donGia;

                        modelHoaDon.setValueAt(FormatUtil.formatVND(thanhTien), row, 5);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }
        });
    }

    private void createInvoiceReturn() {
        if (invoice == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng quét mã qr trên hóa đơn để thực hiện chức năng trả hàng.");
            return;
        }

        if (listChosen.size() == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm để trả hàng.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Xác nhận trả hàng.");
        if (confirm != JOptionPane.YES_OPTION)
            return;

        InvoiceRefundRequest invoiceRefundRequest = InvoiceRefundRequest.builder()
                .refundAmount(total)
                .reason(comboTinhTrang.getSelectedItem() + ": " + txtLyDo.getText())
                .employee(ClientSecurityContext.getCurrentUser())
                .customer(invoice.getCustomer())
                .invoiceCode(invoice.getInvoiceCode())
                .build();

        InvoiceRefundResponse invoiceRefundResponse = null;
        try {
            invoiceRefundResponse = refundService.processInvoiceRefund(invoiceRefundRequest, listChosen);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this, "Trả hàng thành công. Xác nhận xuất hóa đơn trả.");

        if (ok == JOptionPane.YES_OPTION) {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);

            JPanel glass = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(0, 0, 0, 110));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
            };

            glass.setOpaque(false);
            glass.setLayout(null);

            frame.setGlassPane(glass);
            glass.setVisible(true);

            if (generateInvoice == null)
                generateInvoice = new GenerateInvoiceReturn(frame, true);

            // Build an index for quick lookup by medicineId + batchId
            java.util.Map<String, InvoiceDetailResponse> detailIndex = new java.util.HashMap<>();
            for (InvoiceDetailResponse response : listAll) {
                String key = response.getMedicine().getId() + "|" + response.getBatch().getId();
                detailIndex.put(key, response);
            }

            List<InvoiceDetailResponse> temp = new ArrayList<>();
            for (InvoiceDetailRefundRequest request : listChosen) {
                String key = request.getMedicineId() + "|" + request.getBatchId();
                InvoiceDetailResponse temp1 = detailIndex.get(key);
                if (temp1 != null) {
                    temp1.setQuantity(request.getQuantity());
                    temp1.setUnitPrice(request.getUnitPrice());
                    temp1.setTotalAmount();
                    temp.add(temp1);
                }
            }

            List<InvoiceDetailResponse> result = new ArrayList<>(
                    temp.stream()
                            .collect(Collectors.toMap(InvoiceDetailResponse::getMedicine,
                                    p -> p, (p1, p2) -> {
                                        p1.setQuantity(p1.getQuantity() + p2.getQuantity());
                                        p1.setTotalAmount();
                                        return p1;
                                    })).values());

            generateInvoice.setInitData(invoiceRefundResponse, result);
            generateInvoice.setLocationRelativeTo(frame);
            generateInvoice.setVisible(true);

            glass.setVisible(false);
        }

        refreshData();
    }

    private void clearTableChosen() {
        listChosen.clear();
        modelSanPham.setRowCount(0);
        calculatorThanhTien();
    }

    private void removeInvDetailChosen() {
        int i = tblSanPham.getSelectedRow();
        if (i < 0)
            return;

        listChosen.remove(i);
        modelSanPham.removeRow(i);
        calculatorThanhTien();
    }

    private void addInvDetailForRefund() {

        if (tblHoaDon.isEditing())
            tblHoaDon.getCellEditor().stopCellEditing();

        modelSanPham.setRowCount(0);
        listChosen.clear();

        int index = 1;

        for (int i = 0; i < modelHoaDon.getRowCount(); i++) {
            Object checkedObj = modelHoaDon.getValueAt(i, 0);
            boolean checked = (checkedObj != null && (Boolean) checkedObj);

            if (checked) {
                InvoiceDetailResponse originalDetail = listAll.get(i);

                int soLuongHoanTra = 0;
                try {
                    Object quantityValue = modelHoaDon.getValueAt(i, 3);

                    if (quantityValue instanceof Integer)
                        soLuongHoanTra = (Integer) quantityValue;
                    else if (quantityValue != null)
                        soLuongHoanTra = Integer.parseInt(quantityValue.toString());

                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    continue;
                }

                double thanhTienHoanTra = soLuongHoanTra * originalDetail.getUnitPrice();

                InvoiceDetailRefundRequest request = InvoiceDetailRefundRequest.builder()
                        .quantity(soLuongHoanTra)
                        .unitPrice(originalDetail.getUnitPrice())
                        .totalAmount(thanhTienHoanTra)
                        .batchId(originalDetail.getBatch().getId())
                        .medicineId(originalDetail.getMedicine().getId())
                        .build();

                listChosen.add(request);

                modelSanPham.addRow(new Object[]{index++, originalDetail.getMedicine().getMedicineName(),
                        originalDetail.getBatch().getBatchNumber(), soLuongHoanTra});
            }
        }

        calculatorThanhTien();
    }

    private void calculatorThanhTien() {
        total = listChosen.stream().mapToDouble(InvoiceDetailRefundRequest::getTotalAmount).sum();
        txtTienHoan.setText(FormatUtil.formatVND(total));
    }

    private void refreshData() {
        invoice = null;
        listAll.clear();
        listChosen.clear();
        txtMaNV.setText("ALA010001");
        txtTenNV.setText("Bùi Trung Kiên");
        txtMaHD.setText("");
        txtNgayMua.setText("");
        txtSdt.setText("");
        txtTenKH.setText("");
        txtNgayDoiTra.setText("");
        comboTinhTrang.setSelectedIndex(0);
        txtTienHoan.setText("");
        txtLyDo.setText("");
        total = 0;

        modelHoaDon.setRowCount(0);

        modelSanPham.setRowCount(0);

        txtSearch.setText("");
        SwingUtilities.invokeLater(() -> txtSearch.requestFocusInWindow());
    }

    private void loadData() {
        txtMaNV.setText(ClientSecurityContext.getCurrentUser().getEmployeeCode());
        txtTenNV.setText(ClientSecurityContext.getCurrentUser().getFullName());
        txtMaHD.setText(invoice.getInvoiceCode());
        txtNgayMua.setText(FormatUtil.formatDate(invoice.getCreatedDate()));
        txtSdt.setText(invoice.getCustomer().getPhoneNumber());
        txtTenKH.setText(invoice.getCustomer().getFullName());
        txtNgayDoiTra.setText(FormatUtil.formatDate(LocalDate.now()));
    }

    private void viewTableHoaDon() {
        modelHoaDon.setRowCount(0);

        for (InvoiceDetailResponse invoice : listAll)
            modelHoaDon.addRow(new Object[]{false, invoice.getMedicine().getMedicineName(), invoice.getBatch().getBatchNumber(), invoice.getQuantity(), FormatUtil.formatVND(invoice.getUnitPrice()),
                    FormatUtil.formatVND(invoice.getTotalAmount())});
    }

    private void updateTexts() {
        Translator lang = Translator.getInstance();

        lblTieuDe1.setText(lang.getString("refund.title.employee_info"));
        lblMaNV.setText(lang.getString("refund.label.employee_id"));
        lblTenNV.setText(lang.getString("refund.label.employee_name"));

        lblTieuDe2.setText(lang.getString("refund.title.customer_info"));
        lblSdt.setText(lang.getString("refund.label.phone"));
        lblTenKH.setText(lang.getString("refund.label.customer_name"));

        lblTieuDe3.setText(lang.getString("refund.title.refund_info"));
        lblMaHD.setText(lang.getString("refund.label.invoice_id"));
        lblNgayMua.setText(lang.getString("refund.label.purchase_date"));
        lblNgayDoiTra.setText(lang.getString("refund.label.refund_date"));
        lblTrangThai.setText(lang.getString("refund.label.status"));
        lblTienHoan.setText(lang.getString("refund.label.refund_amount"));
        lblLyDo.setText(lang.getString("refund.label.reason"));
        lblTinhTrang.setText(lang.getString("refund.label.condition"));
        lblTieuDe.setText(lang.getString("refund.label.invoice_info"));
        lblTraHang1.setText(lang.getString("refund.label.refund"));

        btnXoa.setText(lang.getString("refund.button.delete"));
        btnThem.setText(lang.getString("refund.button.add"));
        btnTaoDon.setText(lang.getString("refund.button.create"));
        btnRefesh.setText("Refresh");
        btnXoaRong.setText(lang.getString("refund.button.delete_all"));
    }

    private void initComponents() {

        JpWest = new javax.swing.JPanel();
        lblTieuDe1 = new javax.swing.JLabel();
        lblMaNV = new javax.swing.JLabel();
        txtMaNV = new javax.swing.JTextField();
        lblTenNV = new javax.swing.JLabel();
        txtTenNV = new javax.swing.JTextField();
        lblTieuDe2 = new javax.swing.JLabel();
        lblSdt = new javax.swing.JLabel();
        lblTenKH = new javax.swing.JLabel();
        txtTenKH = new javax.swing.JTextField();
        lblTieuDe3 = new javax.swing.JLabel();
        lblMaHD = new javax.swing.JLabel();
        txtMaHD = new javax.swing.JTextField();
        lblNgayMua = new javax.swing.JLabel();
        txtNgayMua = new javax.swing.JTextField();
        lblNgayDoiTra = new javax.swing.JLabel();
        txtNgayDoiTra = new javax.swing.JTextField();
        lblTrangThai = new javax.swing.JLabel();
        scrollSP = new javax.swing.JScrollPane();
        lblTienHoan = new javax.swing.JLabel();
        txtTienHoan = new javax.swing.JTextField();
        lblLyDo = new javax.swing.JLabel();
        txtLyDo = new javax.swing.JTextField();
        btnXoa = new javax.swing.JButton();
        btnThem = new javax.swing.JButton();
        btnTaoDon = new javax.swing.JButton();
        lblTraHang1 = new javax.swing.JLabel();
        lblTinhTrang = new javax.swing.JLabel();
        comboTinhTrang = new javax.swing.JComboBox<>();
        txtSdt = new javax.swing.JTextField();
        JpCenter = new javax.swing.JPanel();
        JpNorth = new javax.swing.JPanel();
        txtSearch = new javax.swing.JTextField();
        JpCenter1 = new javax.swing.JPanel();
        lblTieuDe = new javax.swing.JLabel();
        scrollHD = new javax.swing.JScrollPane();
        btnXoaRong = new javax.swing.JButton();
        btnRefesh = new javax.swing.JButton();

        txtMaNV.setEditable(false);
        txtTenNV.setEditable(false);
        txtSdt.setEditable(false);
        txtMaHD.setEditable(false);
        txtNgayDoiTra.setEditable(false);
        txtNgayMua.setEditable(false);
        txtTenKH.setEditable(false);
        txtTienHoan.setEditable(false);

        setLayout(new BorderLayout());

        JpWest.setBackground(new java.awt.Color(241, 241, 241));
        JpWest.setPreferredSize(new java.awt.Dimension(300, 516));

        lblTieuDe1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTieuDe1.setForeground(new java.awt.Color(0, 0, 255));

        txtMaNV.setAlignmentX(0.0F);
        txtMaNV.setPreferredSize(new java.awt.Dimension(68, 35));

        txtTenNV.setAlignmentX(0.0F);
        txtTenNV.setPreferredSize(new java.awt.Dimension(68, 35));

        lblTieuDe2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTieuDe2.setForeground(new java.awt.Color(0, 0, 255));

        txtTenKH.setAlignmentX(0.0F);
        txtTenKH.setPreferredSize(new java.awt.Dimension(68, 35));

        lblTieuDe3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTieuDe3.setForeground(new java.awt.Color(0, 0, 255));

        txtMaHD.setAlignmentX(0.0F);
        txtMaHD.setPreferredSize(new java.awt.Dimension(68, 35));

        txtNgayMua.setAlignmentX(0.0F);
        txtNgayMua.setPreferredSize(new java.awt.Dimension(68, 35));

        txtNgayDoiTra.setAlignmentX(0.0F);
        txtNgayDoiTra.setPreferredSize(new java.awt.Dimension(68, 35));

        String[] availableHeaders = Translator.getInstance().getString("refund.tbl.available.headers").split(",");

        modelSanPham = new DefaultTableModel(availableHeaders, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0)
                    return Integer.class;
                if (columnIndex == 3)
                    return Integer.class;
                return String.class;
            }
        };

        tblSanPham = new JTable(modelSanPham);
        tblSanPham.setRowHeight(25);

        scrollSP.setViewportView(tblSanPham);
        tblSanPham.getColumnModel().getColumn(0).setMaxWidth(25);
        tblSanPham.getColumnModel().getColumn(3).setMaxWidth(70);
        tblSanPham.getColumnModel().getColumn(2).setPreferredWidth(150);

        txtTienHoan.setPreferredSize(new java.awt.Dimension(68, 35));

        txtLyDo.setPreferredSize(new java.awt.Dimension(68, 35));

        btnXoa.setBackground(new java.awt.Color(225, 225, 225));
        btnXoa.setPreferredSize(new java.awt.Dimension(72, 27));

        btnXoaRong.setBackground(new java.awt.Color(225, 225, 225));
        btnXoaRong.setPreferredSize(new java.awt.Dimension(80, 27));

        btnTaoDon.setBackground(new java.awt.Color(0, 102, 255));
        btnTaoDon.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnTaoDon.setForeground(new java.awt.Color(255, 255, 255));
        btnTaoDon.setPreferredSize(new java.awt.Dimension(116, 27));

        lblTraHang1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N

        comboTinhTrang.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[]{"Không còn nhu cầu", "Sản phẩm bị lỗi", "Hàng không đúng mô tả", "Mẫn cảm với thuốc"}));

        txtSdt.setAlignmentX(0.0F);
        txtSdt.setPreferredSize(new java.awt.Dimension(68, 35));
        txtSdt.setEditable(false);

        javax.swing.GroupLayout JpWestLayout = new javax.swing.GroupLayout(JpWest);
        JpWest.setLayout(JpWestLayout);
        JpWestLayout.setHorizontalGroup(JpWestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(JpWestLayout.createSequentialGroup().addGroup(JpWestLayout
                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(JpWestLayout.createSequentialGroup().addGap(21, 21, 21).addComponent(lblTieuDe1,
                                        javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(JpWestLayout.createSequentialGroup().addGap(10, 10, 10)
                                        .addGroup(JpWestLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(JpWestLayout.createSequentialGroup().addGroup(JpWestLayout
                                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                .addComponent(lblTienHoan, javax.swing.GroupLayout.PREFERRED_SIZE, 74,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(lblLyDo, javax.swing.GroupLayout.PREFERRED_SIZE, 74,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addGroup(JpWestLayout
                                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING,
                                                                        false)
                                                                .addComponent(txtLyDo, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        233, Short.MAX_VALUE)
                                                                .addComponent(txtTienHoan, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                                .addGroup(JpWestLayout.createSequentialGroup().addGap(10, 10, 10)
                                                        .addComponent(btnXoaRong, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(10, 10, 10).addComponent(btnXoa)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)

                                                        .addGap(10, 10, 10).addComponent(btnTaoDon,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 150,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addComponent(scrollSP, javax.swing.GroupLayout.PREFERRED_SIZE, 378,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(JpWestLayout.createSequentialGroup().addGap(10, 10, 10).addComponent(lblTieuDe3,
                                        javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(JpWestLayout.createSequentialGroup().addContainerGap()
                                        .addGroup(JpWestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(JpWestLayout.createSequentialGroup()
                                                        .addComponent(lblMaHD, javax.swing.GroupLayout.PREFERRED_SIZE, 114,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(txtMaHD, javax.swing.GroupLayout.PREFERRED_SIZE, 208,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(JpWestLayout.createSequentialGroup()
                                                        .addComponent(lblNgayMua, javax.swing.GroupLayout.PREFERRED_SIZE, 114,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(
                                                                txtNgayMua, javax.swing.GroupLayout.PREFERRED_SIZE, 208,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(JpWestLayout.createSequentialGroup().addGroup(JpWestLayout
                                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                .addComponent(lblTinhTrang, javax.swing.GroupLayout.PREFERRED_SIZE, 114,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(lblTrangThai, javax.swing.GroupLayout.PREFERRED_SIZE, 114,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(lblNgayDoiTra, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        114, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addGroup(JpWestLayout
                                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING,
                                                                        false)
                                                                .addComponent(comboTinhTrang, 0,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addComponent(lblTraHang1, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        208, Short.MAX_VALUE)
                                                                .addComponent(txtNgayDoiTra,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        Short.MAX_VALUE)))))
                                .addGroup(JpWestLayout.createSequentialGroup().addContainerGap()
                                        .addGroup(JpWestLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                .addGroup(JpWestLayout.createSequentialGroup()
                                                        .addComponent(lblTenKH, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(txtTenKH, javax.swing.GroupLayout.PREFERRED_SIZE, 208,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(JpWestLayout.createSequentialGroup()
                                                        .addComponent(lblSdt, javax.swing.GroupLayout.PREFERRED_SIZE, 114,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(txtSdt, javax.swing.GroupLayout.PREFERRED_SIZE, 208,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(JpWestLayout.createSequentialGroup()
                                                        .addGroup(JpWestLayout
                                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                .addComponent(lblMaNV, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        114, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(lblTenNV, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        114, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addPreferredGap(
                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addGroup(JpWestLayout
                                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                .addComponent(txtTenNV,
                                                                        javax.swing.GroupLayout.Alignment.TRAILING,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 208,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(txtMaNV,
                                                                        javax.swing.GroupLayout.Alignment.TRAILING,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 208,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                .addGroup(JpWestLayout.createSequentialGroup().addGap(10, 10, 10).addComponent(lblTieuDe2,
                                        javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        JpWestLayout.setVerticalGroup(JpWestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(JpWestLayout.createSequentialGroup().addContainerGap().addComponent(lblTieuDe1)
                        .addGap(10, 10, 10)
                        .addGroup(JpWestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblMaNV).addComponent(txtMaNV, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(JpWestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblTenNV).addComponent(txtTenNV, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10).addComponent(lblTieuDe2).addGap(10, 10, 10)
                        .addGroup(JpWestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblSdt).addComponent(txtSdt, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(JpWestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtTenKH, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblTenKH))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(lblTieuDe3)
                        .addGap(10, 10, 10)
                        .addGroup(JpWestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblMaHD).addComponent(txtMaHD, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(JpWestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblNgayMua).addComponent(txtNgayMua,
                                        javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(JpWestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtNgayDoiTra, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblNgayDoiTra))
                        .addGap(10, 10, 10)
                        .addGroup(JpWestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblTrangThai).addComponent(lblTraHang1))
                        .addGap(10, 10, 10)
                        .addGroup(JpWestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblTinhTrang).addComponent(comboTinhTrang,
                                        javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(scrollSP, javax.swing.GroupLayout.PREFERRED_SIZE, 151,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(JpWestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtTienHoan, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblTienHoan))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(JpWestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblLyDo).addComponent(txtLyDo, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(JpWestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(btnTaoDon, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(JpWestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnXoaRong, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnXoa)))

                        .addGap(15, 15, 15)));

        add(JpWest, java.awt.BorderLayout.LINE_END);

        JpCenter.setBackground(new java.awt.Color(255, 255, 255));
        JpCenter.setPreferredSize(new java.awt.Dimension(800, 883));
        JpCenter.setLayout(new java.awt.BorderLayout());

        JpNorth.setBackground(new java.awt.Color(255, 255, 255));
        JpNorth.setPreferredSize(new java.awt.Dimension(514, 50));

        txtSearch.setMinimumSize(new java.awt.Dimension(300, 27));
        txtSearch.setPreferredSize(new java.awt.Dimension(300, 35));
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                Translator.getInstance().getString("refund.text.search"));

        btnRefesh.setBackground(new java.awt.Color(225, 225, 225));
        btnRefesh.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N

        btnRefesh.setPreferredSize(new java.awt.Dimension(90, 35));

        btnThem.setBackground(new java.awt.Color(0, 102, 255));
        btnThem.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnThem.setForeground(new java.awt.Color(255, 255, 255));
        btnThem.setMinimumSize(new java.awt.Dimension(80, 35));

        javax.swing.GroupLayout JpNorthLayout = new javax.swing.GroupLayout(JpNorth);
        JpNorth.setLayout(JpNorthLayout);
        JpNorthLayout.setHorizontalGroup(JpNorthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(JpNorthLayout.createSequentialGroup().addGap(24).addComponent(txtSearch, 542, 542, 542)
                        .addGap(30).addComponent(btnRefesh, 95, 95, 95).addGap(20).addComponent(btnThem, 95, 95, 95)
                        .addContainerGap(36, Short.MAX_VALUE)));

        JpNorthLayout.setVerticalGroup(JpNorthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(JpNorthLayout.createSequentialGroup().addGap(14)
                        .addGroup(JpNorthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnRefesh, javax.swing.GroupLayout.PREFERRED_SIZE, 27,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnThem, javax.swing.GroupLayout.PREFERRED_SIZE, 27,
                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(17)));

        JpCenter.add(JpNorth, java.awt.BorderLayout.PAGE_START);

        JpCenter1.setBackground(new java.awt.Color(255, 255, 255));

        lblTieuDe.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        scrollHD.setBackground(new java.awt.Color(255, 255, 255));

        String[] detailHeaders = Translator.getInstance().getString("refund.tbl.detail.headers").split(",");

        modelHoaDon = new DefaultTableModel(detailHeaders, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0)
                    return Boolean.class;
                if (columnIndex == 3)
                    return Integer.class;
                return Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 3;
            }
        };

        tblHoaDon = new JTable(modelHoaDon);

        tblHoaDon.getTableHeader().setReorderingAllowed(false);
        scrollHD.setViewportView(tblHoaDon);

        TableColumn checkColumn = tblHoaDon.getColumnModel().getColumn(0);
        checkColumn.setMinWidth(40);
        checkColumn.setMaxWidth(50);
        checkColumn.setPreferredWidth(45);

        tblHoaDon.getColumnModel().getColumn(2).setPreferredWidth(150);
        tblHoaDon.getColumnModel().getColumn(3).setPreferredWidth(150);

        TableColumn quantityColumn = tblHoaDon.getColumnModel().getColumn(3);

        quantityColumn.setCellEditor(new QuantityCellEditor(listAll));
        quantityColumn.setCellRenderer(new QuantityCellRenderer());

        javax.swing.GroupLayout JpCenter1Layout = new javax.swing.GroupLayout(JpCenter1);
        JpCenter1.setLayout(JpCenter1Layout);
        JpCenter1Layout
                .setHorizontalGroup(JpCenter1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(JpCenter1Layout.createSequentialGroup().addGap(18, 18, 18)
                                .addComponent(lblTieuDe, javax.swing.GroupLayout.PREFERRED_SIZE, 160,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(622, Short.MAX_VALUE))
                        .addGroup(JpCenter1Layout.createSequentialGroup().addContainerGap().addComponent(scrollHD))

                );
        JpCenter1Layout
                .setVerticalGroup(
                        JpCenter1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(JpCenter1Layout.createSequentialGroup().addGap(14, 14, 14)
                                        .addComponent(lblTieuDe).addGap(18, 18, 18).addComponent(scrollHD,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, 775, Short.MAX_VALUE)
                                        .addContainerGap()));

        JpCenter.add(JpCenter1, java.awt.BorderLayout.CENTER);

        add(JpCenter, java.awt.BorderLayout.CENTER);
    }

    private javax.swing.JPanel JpCenter;
    private javax.swing.JPanel JpCenter1;
    private javax.swing.JPanel JpNorth;
    private javax.swing.JPanel JpWest;
    private javax.swing.JButton btnTaoDon;
    private javax.swing.JButton btnThem;
    private javax.swing.JButton btnRefesh;
    private javax.swing.JButton btnXoa;
    private javax.swing.JButton btnXoaRong;
    private javax.swing.JComboBox<String> comboTinhTrang;
    private javax.swing.JLabel lblLyDo;
    private javax.swing.JLabel lblMaHD;
    private javax.swing.JLabel lblMaNV;
    private javax.swing.JLabel lblNgayDoiTra;
    private javax.swing.JLabel lblNgayMua;
    private javax.swing.JLabel lblSdt;
    private javax.swing.JLabel lblTenKH;
    private javax.swing.JLabel lblTenNV;
    private javax.swing.JLabel lblTienHoan;
    private javax.swing.JLabel lblTieuDe;
    private javax.swing.JLabel lblTieuDe1;
    private javax.swing.JLabel lblTieuDe2;
    private javax.swing.JLabel lblTieuDe3;
    private javax.swing.JLabel lblTinhTrang;
    private javax.swing.JLabel lblTraHang1;
    private javax.swing.JLabel lblTrangThai;
    private javax.swing.JScrollPane scrollHD;
    private javax.swing.JScrollPane scrollSP;
    private javax.swing.JTable tblHoaDon;
    private javax.swing.JTable tblSanPham;
    private javax.swing.JTextField txtLyDo;
    private javax.swing.JTextField txtMaHD;
    private javax.swing.JTextField txtMaNV;
    private javax.swing.JTextField txtNgayDoiTra;
    private javax.swing.JTextField txtNgayMua;
    private javax.swing.JTextField txtSdt;
    private javax.swing.JTextField txtTenKH;
    private javax.swing.JTextField txtTenNV;
    private javax.swing.JTextField txtTienHoan;
    private javax.swing.JTextField txtSearch;

    private void init() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        int widthWest = (int) (screen.width * 0.25);
        int heightWest = screen.height;

        JpWest.setPreferredSize(new Dimension(widthWest, heightWest));

        int widthCenter = (int) (screen.width * 0.75);
        int heightCenter = screen.height;

        JpCenter.setPreferredSize(new Dimension(widthCenter, heightCenter));

        decorateTable(tblHoaDon);
    }

    private void decorateTable(JTable tblHoaDon) {
        tblHoaDon.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblHoaDon.setRowHeight(40);
        tblHoaDon.setGridColor(new Color(220, 220, 220));

        tblHoaDon.setSelectionBackground(new Color(250, 250, 250));
        tblHoaDon.setSelectionForeground(Color.BLACK);

        JTableHeader header = tblHoaDon.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setBackground(new Color(36, 104, 155));
        header.setForeground(Color.WHITE);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        tblHoaDon.setShowHorizontalLines(true);
        tblHoaDon.setShowVerticalLines(true);

        for (int i = 1; i < tblHoaDon.getColumnCount(); i++) {
            tblHoaDon.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

}

class QuantityCellRenderer extends AbstractCellEditor implements TableCellRenderer {

    CustomQuantitySpinner spinner;

    public QuantityCellRenderer() {
        spinner = new CustomQuantitySpinner();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        if (value != null)
            spinner.setValue(Integer.parseInt(value.toString()));

        spinner.setBackground(new Color(250, 250, 250));
        return spinner;
    }

    @Override
    public Object getCellEditorValue() {
        return spinner.getValue();
    }
}

class QuantityCellEditor extends AbstractCellEditor implements TableCellEditor {

    CustomQuantitySpinner spinner;

    JPanel container;

    private java.util.List<InvoiceDetailResponse> listAll;

    public QuantityCellEditor(java.util.List<InvoiceDetailResponse> listAll) {

        this.listAll = listAll != null ? listAll : new java.util.ArrayList<>();

        spinner = new CustomQuantitySpinner();

        spinner.addActionListener(e -> fireEditingStopped());

        container = new JPanel(new BorderLayout());
        container.add(spinner, BorderLayout.CENTER);
    }

    public void setListInvoice(java.util.List<InvoiceDetailResponse> listAll) {
        this.listAll = listAll;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value != null)
            spinner.setValue(Integer.parseInt(value.toString()));

        if (listAll != null && row < listAll.size()) {
            InvoiceDetailResponse currentDetail = listAll.get(row);

            int maxQty = currentDetail.getQuantity();

            spinner.setMaxQuantity(maxQty);
        }

        container.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, table.getGridColor()));

        return container;
    }

    @Override
    public Object getCellEditorValue() {
        return spinner.getValue();
    }

}