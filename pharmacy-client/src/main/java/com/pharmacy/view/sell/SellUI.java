package com.pharmacy.view.sell;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.pharmacy.shared.dto.request.CustomerRequest;
import com.pharmacy.shared.dto.request.EmployeeMiniRequest;
import com.pharmacy.shared.dto.request.InvoiceRequest;
import com.pharmacy.shared.dto.request.MedicineBatchToSellRequest;
import com.pharmacy.shared.dto.response.CustomerMaxResponse;
import com.pharmacy.shared.dto.response.InvoiceResponse;
import com.pharmacy.shared.dto.response.VoucherResponse;
import com.pharmacy.shared.service.CustomerService;
import com.pharmacy.shared.service.SellService;
import com.pharmacy.shared.service.VoucherService;
import com.pharmacy.util.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SellUI extends javax.swing.JPanel {

    private static final int WIDTH_IMAGE = 110;

    private static final int HEIGHT_IMAGE = 110;

    private List<MedicineBatchToSellRequest> list;

    private final SellService sellService;

    private final CustomerService customerService;

    private final VoucherService voucherService;

    private CustomerMaxResponse customerMaxResponse;

    private CustomerRequest customerRequest;

    private double tongTienHang = 0;

    private double soTienCanThanhToan = 0;

    private double soTienDuocGiam = 0;

    private JTextArea txaGhiChu;

    private GenerateInvoice generateInvoice;

    public SellUI() {

        this.sellService = ClientContext.getService(SellService.class);
        this.customerService = ClientContext.getService(CustomerService.class);
        this.voucherService = ClientContext.getService(VoucherService.class);

        list = new ArrayList<>();

        initComponents();

        Translator.getInstance().addLanguageChangeListener(locale ->
                SwingUtilities.invokeLater(this::updateTexts)
        );

        updateTexts();

        initEvent();

        SwingUtilities.invokeLater(() -> txtSearch.requestFocusInWindow());
    }

    private void updateTexts() {
        Translator lang = Translator.getInstance();

        lblTTHD.setText(lang.getString("sell.invoice.info"));
        lblThanhTien.setText(lang.getString("sell.invoice.total_amount"));
        btnThanhToan.setText(lang.getString("sell.invoice.pay"));
        lblNhanVien.setText(lang.getString("sell.employee"));
        lblThongTinKH.setText(lang.getString("sell.customer.info"));
        lblHoTen.setText(lang.getString("sell.customer.name") + " (F8)");
        lblPhone.setText(lang.getString("sell.customer.phone") + " (F7)");
        lblCTTT.setText(lang.getString("sell.payment.detail"));

        lblTongTienHang.setText(lang.getString("sell.product.total"));
        lblVoucher.setText(lang.getString("sell.voucher.apply"));
        lblSoTienGiam.setText(lang.getString("sell.voucher.discount_amount"));

        lblPTTT.setText(lang.getString("sell.payment.method"));
        lblTienKhachDua.setText(lang.getString("sell.payment.cash_given"));
        lblTienThua.setText(lang.getString("sell.payment.change"));

        lblDMSPDC.setText(lang.getString("sell.selected_product.list"));
    }

    private void setTongTienForUI() {

        tongTienHang = sellService.calculateTotalMoneyByMedicineAndBatch(list);

        loadVoucher();

        soTienCanThanhToan = sellService.calculateTotalMoneyToPayment(tongTienHang, soTienDuocGiam);

        txtTongTienHang.setText(FormatUtil.formatVND(tongTienHang));
        txtSoTienGiam.setText(FormatUtil.formatVND(soTienDuocGiam));
        lblMoney.setText(FormatUtil.formatVND(soTienCanThanhToan) + " VND");

        initSuggestMoney();

        handleTienThua();
    }

    private void initEvent() {
        btnRefresh.addActionListener(e -> refreshData());

        btnRemoveAll.addActionListener(e -> removeAllItemChosen());

        btnThanhToan.addActionListener(e -> eventThanhToan());

        txtSearch.addActionListener(e -> {
            String barcode = txtSearch.getText().trim();
            if (barcode.isEmpty())
                return;

            try {
                MedicineBatchToSellRequest request = sellService.getMedicineAndBatchToSellByBarcode(barcode);

                Optional<MedicineBatchToSellRequest> existedItem = list.stream()
                        .filter(item -> item.getId() != null && item.getId().equals(request.getId()))
                        .findFirst();

                if (existedItem.isPresent()) {
                    MedicineBatchToSellRequest existing = existedItem.get();
                    MedicineBatchToSellRequest updated = sellService.processAddToCart(
                            existing,
                            existing.getSellingQuantity() + 1
                    );
                    applyCartUpdate(existing, updated);
                } else {
                    MedicineBatchToSellRequest updated = sellService.processAddToCart(request, 1);
                    list.add(updated);
                }

                showProdChosen();

            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            } finally {
                SwingUtilities.invokeLater(() -> {
                    txtSearch.setText("");
                    txtSearch.requestFocusInWindow();
                });
            }
        });

        txtPhone.addActionListener(e -> handleSearchCustomer());

        txtTienKhachDua.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                handleTienThua();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                handleTienThua();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        cmbPTTT.addActionListener(e -> {
            boolean ok = cmbPTTT.getSelectedIndex() == 0;

            if (!ok) {
                txtTienKhachDua.setText("");
                txtTienThua.setText("");
            }

            txtTienKhachDua.setEditable(ok);
            btnMoney1.setEnabled(ok);
            btnMoney2.setEnabled(ok);
            btnMoney3.setEnabled(ok);
            btnMoney4.setEnabled(ok);
            btnMoney5.setEnabled(ok);
            btnMoney6.setEnabled(ok);
        });

        cmbVoucher.addActionListener(e -> {
            loadSoTienDuocGiam();
        });

        btnMoney1.addActionListener(e -> handleSuggestMoney(btnMoney1));

        btnMoney2.addActionListener(e -> handleSuggestMoney(btnMoney2));

        btnMoney3.addActionListener(e -> handleSuggestMoney(btnMoney3));

        btnMoney4.addActionListener(e -> handleSuggestMoney(btnMoney4));

        btnMoney5.addActionListener(e -> handleSuggestMoney(btnMoney5));

        btnMoney6.addActionListener(e -> handleSuggestMoney(btnMoney6));

        initHotkeys();
    }

    private void handleSuggestMoney(JButton btn) {
        Object val = btn.getClientProperty("MONEY_VAL");

        if (val != null) {
            long money = (long) val;
            txtTienKhachDua.setText(String.valueOf(money));
            handleTienThua();
        }
    }

    public void initHotkeys() {
        addHotkey(btnMoney1, KeyEvent.VK_F1);
        addHotkey(btnMoney2, KeyEvent.VK_F2);
        addHotkey(btnMoney3, KeyEvent.VK_F3);
        addHotkey(btnMoney4, KeyEvent.VK_F4);
        addHotkey(btnMoney5, KeyEvent.VK_F5);
        addHotkey(btnMoney6, KeyEvent.VK_F6);

        InputMap inputMap = txtSearch.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = txtSearch.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0), "focus_scan_barcode");
        actionMap.put("focus_scan_barcode", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtSearch.requestFocusInWindow();
            }
        });

        InputMap inputMapPhone = txtPhone.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMapPhone = txtPhone.getActionMap();
        inputMapPhone.put(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0), "focus_phone");
        actionMapPhone.put("focus_phone", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtPhone.requestFocusInWindow();
            }
        });

        InputMap inputMapName = txtHotenKH.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMapName = txtHotenKH.getActionMap();
        inputMapName.put(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), "focus_name");
        actionMapName.put("focus_name", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtHotenKH.requestFocusInWindow();
            }
        });
    }

    private void addHotkey(JButton btn, int keyEvent) {
        InputMap inputMap = btn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = btn.getActionMap();

        String actionKey = "click_" + keyEvent;

        inputMap.put(KeyStroke.getKeyStroke(keyEvent, 0), actionKey);

        actionMap.put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btn.doClick();
            }
        });
    }

    private void initSuggestMoney() {
        List<Long> list = SuggestMoney.suggest((long) soTienCanThanhToan);

        while (list.size() < 6)
            list.add(0L);

        JButton[] arrBtn = {btnMoney1, btnMoney2, btnMoney3, btnMoney4, btnMoney5, btnMoney6};

        for (int i = 0; i < arrBtn.length; i++) {
            long moneyValue = list.get(i);

            arrBtn[i].setText(FormatUtil.formatVND(moneyValue) + " (F" + (i + 1) + ")");
            arrBtn[i].putClientProperty("MONEY_VAL", moneyValue);
        }
    }

    private void eventThanhToan() {
        if (list.size() == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm để mua hàng.");
            return;
        }

        /**
         * Lấy thông tin khách hàng (Có thể có hoặc không)
         */
        String sdt = txtPhone.getText().trim();
        String tenKH = txtHotenKH.getText();

        if (!sdt.isEmpty() || !tenKH.isEmpty()) { // nếu có nhập số điện thoại hoặc tên thì phải nhập đủ hết

            if (sdt.isEmpty() || !sdt.matches("^0\\d{9}$")) {
                JOptionPane.showMessageDialog(this, "Số điện thoại phải bắt đầu bằng số 0, theo sau là 9 chữ số.");
                txtPhone.requestFocus();
                txtPhone.selectAll();
                return;
            }

            if (tenKH.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tên khách hàng không được để trống.");
                txtHotenKH.requestFocus();
                txtHotenKH.selectAll();
                return;
            }

            int index = tenKH.lastIndexOf('-');
            if (index != -1)
                tenKH = tenKH.substring(0, index);

            customerRequest = CustomerRequest.builder()
                    .fullName(tenKH)
                    .phoneNumber(sdt)
                    .build();

            customerRequest.setRewardPoints(tongTienHang);
        } else { // nếu là khách vãng lai thì không nhập gì cả
            customerRequest = null;
        }

        /**
         * Lấy thông tin voucher đang được sử dụng từ combobox (có thể có hoặc không)
         */
        VoucherResponse voucher = null;
        if (cmbVoucher.getItemCount() > 0) {
            voucher = (VoucherResponse) cmbVoucher.getSelectedItem();
        }

        String voucherCode = voucher == null ? null : voucher.getVoucherCode();

        int confirm = JOptionPane.showConfirmDialog(this, "Xác nhận mua hàng.");
        if (confirm == JOptionPane.NO_OPTION)
            return;

        try {
            InvoiceRequest invoice = InvoiceRequest.builder()
                    .note("")
                    .voucherCode(voucherCode)
                    .employee(EmployeeMiniRequest.builder().id(3L).build())
                    .totalGoodsAmount(tongTienHang)
                    .customer(customerRequest)
                    .totalPayableAmount(soTienCanThanhToan)
                    .build();

            InvoiceResponse invoiceResponse = sellService.processPayment(invoice, list);

            // Sau khi tất cả thành công thì xuất hóa đơn

            int ok = JOptionPane.showConfirmDialog(this, "Bán hàng thành công. Xác nhận xuất hóa đơn.");

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

                glass.addMouseListener(new MouseAdapter() {
                });
                glass.addMouseMotionListener(new MouseMotionAdapter() {
                });

                frame.setGlassPane(glass);
                glass.setVisible(true);

                // In hóa đơn
                if (generateInvoice == null)
                    generateInvoice = new GenerateInvoice(frame, true);

                generateInvoice.setInitData(invoiceResponse, list);
                generateInvoice.setLocationRelativeTo(frame);
                generateInvoice.setVisible(true);

                glass.setVisible(false);
            }

            // Làm mới lại giao diện bán hàng
            refreshData();

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void showProdChosen() {
        int count = 1;

        pnlItemChosen.setVisible(false);
        pnlItemChosen.removeAll();

        for (MedicineBatchToSellRequest request : list) {
            pnlItemChosen.add(initCardItemChosen(request, count++));
        }

        pnlItemChosen.revalidate();
        pnlItemChosen.repaint();
        pnlItemChosen.setVisible(true);

        setTongTienForUI();
    }

    private void handleTienThua() {
        String s = txtTienKhachDua.getText();
        txtTienThua.setText(FormatUtil.formatVND(sellService.calculateMoneyChange(s, soTienCanThanhToan)) + " VND");
    }

    private void loadSoTienDuocGiam() {
        VoucherResponse voucher = (VoucherResponse) cmbVoucher.getSelectedItem();

        if (voucher == null) {
            soTienDuocGiam = 0;
            return;
        }

        soTienDuocGiam = voucher.getTotalDiscountedAmount();

        txtSoTienGiam.setText(FormatUtil.formatVND(soTienDuocGiam));

        soTienCanThanhToan = sellService.calculateTotalMoneyToPayment(tongTienHang, soTienDuocGiam);

        lblMoney.setText(FormatUtil.formatVND(soTienCanThanhToan) + " VND");

        handleTienThua();

        initSuggestMoney();
    }

    public void handleSearchCustomer() {
        String phone = txtPhone.getText();
        customerMaxResponse = customerService.getCustomerByPhoneNumber(phone);

        if (customerMaxResponse == null) {
            txtHotenKH.setText("Nhập mới tên khách hàng.");
            cmbVoucher.removeAllItems();
            soTienDuocGiam = 0;
            setTongTienForUI();
            txtHotenKH.selectAll();
            txtHotenKH.requestFocus();
            return;
        }

        txtHotenKH.setText(customerMaxResponse.getFullName());

        loadVoucher();
    }

    private void loadVoucher() {
        if (customerMaxResponse == null)
            return;

        List<VoucherResponse> listVoucher = voucherService.getAllVoucherByConditionCustomer(customerMaxResponse, tongTienHang);

        cmbVoucher.removeAllItems();

        listVoucher.forEach(item -> cmbVoucher.addItem(item));

        if (cmbVoucher.getItemCount() > 0)
            cmbVoucher.setSelectedIndex(0);

        loadSoTienDuocGiam();
    }

    private void removeAllItemChosen() {
        list.clear();
        showProdChosen();

        SwingUtilities.invokeLater(() -> txtSearch.requestFocusInWindow());
    }

    private void refreshData() {
        tongTienHang = 0;
        soTienCanThanhToan = 0;
        soTienDuocGiam = 0;
        customerRequest = null;
        customerMaxResponse = null;
        list.clear();
        txtSearch.setText("");
        SwingUtilities.invokeLater(() -> txtSearch.requestFocusInWindow());
        removeAllItemChosen();
        txtPhone.setText("");
        txtHotenKH.setText("");
        txtTongTienHang.setText("");
        txtSoTienGiam.setText("");
        txtTienKhachDua.setText("");
        txtTienThua.setText("");
        cmbPTTT.setSelectedIndex(0);
        cmbVoucher.removeAllItems();
        lblMoney.setText("0 VND");
    }

    private JPanel initCardItemChosen(MedicineBatchToSellRequest request, int index) {
        JPanel pnl = new JPanel();
        pnl.setLayout(new MigLayout("wrap, fill", "[grow 0][grow 0][fill]", "[][]"));

        JLabel lblIcon = new JLabel(HelperImageIcon.scaleIcon(request.getAvatarUrl(), WIDTH_IMAGE, HEIGHT_IMAGE));

        JLabel lblName = new JLabel(String.format("%d. %s", index, request.getMedicineName()));
        lblName.putClientProperty(FlatClientProperties.STYLE, "font:$h4.font; foreground:#333333;");

        JButton btnDel = new JButton();
        btnDel.setIcon(new FlatSVGIcon("icon/svg/delete-button.svg", 20, 20));
        btnDel.setBackground(Color.WHITE);

        JButton btnSoLo = new JButton("Số lô");
        btnSoLo.setBackground(Color.WHITE);
        btnSoLo.setForeground(new Color(23, 120, 207));

        JPanel pnlTmp = new JPanel(new MigLayout("wrap, fill", "[][]40[]70[]", "[]"));

        int initialQuantity = Math.max(1, request.getSellingQuantity());
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(initialQuantity, 1, Integer.MAX_VALUE, 1));

        JLabel lblUnit = new JLabel(request.getMeasuringUnit());

        JLabel lblGiaBan = new JLabel(FormatUtil.formatVND(request.getSellingPrice()));

        JLabel lblPrice = new JLabel(FormatUtil.formatVND(request.getTotalAmount()) + " đ");
        lblPrice.putClientProperty(FlatClientProperties.STYLE, "font:$h5.font; foreground:#333333;");

        pnlTmp.add(spinner);
        pnlTmp.add(lblUnit);
        pnlTmp.add(lblGiaBan);
        pnlTmp.add(lblPrice);
        pnlTmp.setBackground(Color.WHITE);

        pnl.add(lblIcon, "span 1 2");
        pnl.add(lblName);
        pnl.add(btnDel, "gapleft push");
        pnl.add(btnSoLo);
        pnl.add(pnlTmp, "gapleft push");

        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true));

        btnDel.addActionListener(e -> removeProdItemChosen(request, pnl));

        btnSoLo.addActionListener(e -> viewPhanBoLo(request));

        spinner.addChangeListener(e -> handleForSpinner(spinner, request, lblGiaBan, lblPrice));

        return pnl;
    }

    private void viewPhanBoLo(MedicineBatchToSellRequest request) {
        BatchDistributionUI view = new BatchDistributionUI(null, false, request);
        view.setLocationRelativeTo(null);
        view.setVisible(true);
    }

    private void handleForSpinner(JSpinner spinner, MedicineBatchToSellRequest request, JLabel lblGiaBan, JLabel lblThanhTien) {
        try {
            spinner.commitEdit();
        } catch (ParseException e) {
            spinner.setValue(request.getSellingQuantity());
            JOptionPane.showMessageDialog(this, "Số lượng không hợp lệ.");
            return;
        }

        int newValue = (int) spinner.getValue();

        if (newValue > request.getTotalQuantity()) {
            spinner.setValue(request.getSellingQuantity());
            JOptionPane.showMessageDialog(this, "Số lượng hàng trong kho không đủ.");
            return;
        }

        try {
            MedicineBatchToSellRequest updated = sellService.processAddToCart(request, newValue);
            applyCartUpdate(request, updated);
        } catch (RuntimeException e) {
            spinner.setValue(request.getSellingQuantity());
            JOptionPane.showMessageDialog(this, e.getMessage());
            return;
        }

        lblGiaBan.setText(FormatUtil.formatVND(request.getSellingPrice()));
        lblThanhTien.setText(FormatUtil.formatVND(request.getTotalAmount()) + " đ");

        setTongTienForUI();
    }

    private void removeProdItemChosen(MedicineBatchToSellRequest request, JPanel pnlToRemove) {
        list.remove(request);

        pnlItemChosen.remove(pnlToRemove);

        pnlItemChosen.revalidate();
        pnlItemChosen.repaint();

        SwingUtilities.invokeLater(() -> txtSearch.requestFocusInWindow());

        setTongTienForUI();
    }

    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        lblTTHD = new javax.swing.JLabel();
        pnlThanhToan = new javax.swing.JPanel();
        lblThanhTien = new javax.swing.JLabel();
        lblMoney = new javax.swing.JLabel();
        btnThanhToan = new javax.swing.JButton();
        pnlInfo = new javax.swing.JPanel();
        lblNhanVien = new javax.swing.JLabel();
        txtNhanVien = new javax.swing.JTextField();
        lblThongTinKH = new javax.swing.JLabel();
        lblHoTen = new javax.swing.JLabel();
        txtHotenKH = new javax.swing.JTextField();
        lblPhone = new javax.swing.JLabel();
        lblCTTT = new javax.swing.JLabel();
        lblTongTienHang = new javax.swing.JLabel();
        txtPhone = new javax.swing.JTextField();
        txtTongTienHang = new javax.swing.JTextField();
        lblVoucher = new javax.swing.JLabel();
        cmbVoucher = new javax.swing.JComboBox<>();
        txtSoTienGiam = new javax.swing.JTextField();
        lblSoTienGiam = new javax.swing.JLabel();
        lblPTTT = new javax.swing.JLabel();
        cmbPTTT = new javax.swing.JComboBox<>();
        lblTienKhachDua = new javax.swing.JLabel();
        txtTienKhachDua = new javax.swing.JTextField();
        btnMoney1 = new javax.swing.JButton();
        btnMoney5 = new javax.swing.JButton();
        btnMoney2 = new javax.swing.JButton();
        btnMoney3 = new javax.swing.JButton();
        btnMoney6 = new javax.swing.JButton();
        btnMoney4 = new javax.swing.JButton();
        lblTienThua = new javax.swing.JLabel();
        txtTienThua = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        txtSearch = new javax.swing.JTextField();
        btnRefresh = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        lblDMSPDC = new javax.swing.JLabel();
        btnRemoveAll = new javax.swing.JButton();
        pnlItemChosen = new javax.swing.JPanel();
        txaGhiChu = new JTextArea();

        txtNhanVien.setEditable(false);
        txtNhanVien.setText("ALA010001 - Bùi Trung Kiên");

        txtTongTienHang.setEditable(false);
        txtSoTienGiam.setEditable(false);
        txtTienThua.setEditable(false);

        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Scan the barcode of the product... (F12)");
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_ICON,
                new FlatSVGIcon("icon/svg/search.svg", 0.4f));

        btnRemoveAll.setIcon(new FlatSVGIcon("icon/svg/delete1.svg", 0.35f));

        setPreferredSize(new java.awt.Dimension(1250, 780));
        setLayout(new java.awt.BorderLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setPreferredSize(new java.awt.Dimension(450, 637));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel6.setBackground(new java.awt.Color(238, 249, 255));
        jPanel6.setPreferredSize(new java.awt.Dimension(450, 55));

        lblTTHD.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblTTHD.setForeground(new java.awt.Color(51, 102, 255));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel6Layout
                        .createSequentialGroup().addContainerGap().addComponent(lblTTHD,
                                javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(188, Short.MAX_VALUE)));
        jPanel6Layout.setVerticalGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel6Layout.createSequentialGroup().addContainerGap()
                        .addComponent(lblTTHD, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                        .addContainerGap()));

        jPanel1.add(jPanel6, java.awt.BorderLayout.PAGE_START);

        pnlThanhToan.setBackground(new java.awt.Color(255, 255, 255));
        pnlThanhToan.setPreferredSize(new java.awt.Dimension(450, 120));

        lblThanhTien.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        lblMoney.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblMoney.setForeground(new java.awt.Color(0, 0, 255));
        lblMoney.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblMoney.setText("0 VND");

        btnThanhToan.setBackground(new java.awt.Color(59, 172, 240));
        btnThanhToan.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnThanhToan.setForeground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout pnlThanhToanLayout = new javax.swing.GroupLayout(pnlThanhToan);
        pnlThanhToan.setLayout(pnlThanhToanLayout);
        pnlThanhToanLayout.setHorizontalGroup(pnlThanhToanLayout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlThanhToanLayout.createSequentialGroup().addContainerGap()
                        .addComponent(lblThanhTien, javax.swing.GroupLayout.PREFERRED_SIZE, 118,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 136, Short.MAX_VALUE)
                        .addComponent(lblMoney, javax.swing.GroupLayout.PREFERRED_SIZE, 171,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(19, 19, 19))
                .addGroup(pnlThanhToanLayout.createSequentialGroup().addGap(128, 128, 128)
                        .addComponent(btnThanhToan, javax.swing.GroupLayout.PREFERRED_SIZE, 183,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        pnlThanhToanLayout.setVerticalGroup(pnlThanhToanLayout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlThanhToanLayout.createSequentialGroup().addContainerGap()
                        .addGroup(pnlThanhToanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblThanhTien, javax.swing.GroupLayout.PREFERRED_SIZE, 25,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblMoney, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18).addComponent(btnThanhToan, javax.swing.GroupLayout.PREFERRED_SIZE, 40,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(21, Short.MAX_VALUE)));

        jPanel1.add(pnlThanhToan, java.awt.BorderLayout.PAGE_END);

        lblNhanVien.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N

        lblThongTinKH.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N

        lblCTTT.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N

        cmbVoucher.setModel(new javax.swing.DefaultComboBoxModel<>());

        cmbPTTT.setModel(
                new javax.swing.DefaultComboBoxModel<>(new String[]{"Thanh toán tiền mặt", "Chuyển khoản"}));

        Dimension size = new Dimension(80, 33);
        btnMoney1.setText("1.000 (F1)");
        btnMoney1.setPreferredSize(size);

        btnMoney2.setText("10.000 (F2)");
        btnMoney2.setPreferredSize(size);

        btnMoney3.setText("20.000 (F3)");
        btnMoney3.setPreferredSize(size);

        btnMoney4.setText("50.000 (F4)");
        btnMoney4.setPreferredSize(size);

        btnMoney5.setText("100.000 (F5)");
        btnMoney5.setPreferredSize(size);

        btnMoney6.setText("500.000 (F6)");
        btnMoney6.setPreferredSize(size);

        javax.swing.GroupLayout pnlInfoLayout = new javax.swing.GroupLayout(pnlInfo);
        pnlInfo.setLayout(pnlInfoLayout);
        pnlInfoLayout.setHorizontalGroup(
                pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(pnlInfoLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(pnlInfoLayout.createSequentialGroup()
                                                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(lblPhone, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(lblHoTen))
                                                .addGap(18, 18, 18)
                                                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(txtHotenKH, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addComponent(txtNhanVien, javax.swing.GroupLayout.PREFERRED_SIZE, 426, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblThongTinKH, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblNhanVien, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblCTTT, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(pnlInfoLayout.createSequentialGroup()
                                                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(lblTongTienHang, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(lblVoucher, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(lblSoTienGiam, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(18, 18, 18)
                                                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(txtSoTienGiam)
                                                        .addComponent(txtTongTienHang)
                                                        .addComponent(cmbVoucher, 0, 305, Short.MAX_VALUE)))
                                        .addGroup(pnlInfoLayout.createSequentialGroup()
                                                .addComponent(lblPTTT)
                                                .addGap(18, 18, 18)
                                                .addComponent(cmbPTTT, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(pnlInfoLayout.createSequentialGroup()
                                                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(lblTienKhachDua, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
                                                        .addComponent(lblTienThua)
                                                        .addComponent(btnMoney1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(btnMoney4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addGap(18, 18, 18)
                                                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(txtTienKhachDua)
                                                        .addGroup(pnlInfoLayout.createSequentialGroup()
                                                                .addGap(24, 24, 24)
                                                                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                        .addComponent(btnMoney2, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                                                                        .addComponent(btnMoney5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                        .addComponent(btnMoney3, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                                                                        .addComponent(btnMoney6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                                        .addGroup(pnlInfoLayout.createSequentialGroup()
                                                                .addComponent(txtTienThua, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(0, 2, Short.MAX_VALUE)))))
                                .addContainerGap(16, Short.MAX_VALUE))
        );
        pnlInfoLayout.setVerticalGroup(
                pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(pnlInfoLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(lblNhanVien)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtNhanVien, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblThongTinKH)
                                .addGap(15, 15, 15)
                                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblPhone)
                                        .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(10, 10, 10)
                                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblHoTen)
                                        .addComponent(txtHotenKH, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(16, 16, 16)
                                .addComponent(lblCTTT)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblTongTienHang)
                                        .addComponent(txtTongTienHang, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(12, 12, 12)
                                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblVoucher)
                                        .addComponent(cmbVoucher, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtSoTienGiam, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblSoTienGiam))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblPTTT)
                                        .addComponent(cmbPTTT, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtTienKhachDua, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblTienKhachDua))
                                .addGap(18, 18, 18)
                                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnMoney1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnMoney2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnMoney3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnMoney6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnMoney5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnMoney4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtTienThua, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblTienThua))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.add(pnlInfo, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.LINE_END);

        jPanel2.setBackground(new java.awt.Color(245, 245, 245));
        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel3.setBackground(new java.awt.Color(238, 249, 255));
        jPanel3.setPreferredSize(new java.awt.Dimension(726, 55));

        txtSearch.setPreferredSize(new java.awt.Dimension(64, 40));

        btnRefresh.setBackground(new java.awt.Color(23, 120, 207));
        btnRefresh.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnRefresh.setForeground(new java.awt.Color(255, 255, 255));
        btnRefresh.setText("Refresh");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup().addGap(33, 33, 33)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 399,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 264, Short.MAX_VALUE)
                        .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 119,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap()));
        jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup().addContainerGap()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 34,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtSearch, javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap(9, Short.MAX_VALUE)));

        jPanel2.add(jPanel3, java.awt.BorderLayout.PAGE_START);

        jPanel4.setLayout(new java.awt.BorderLayout());

        jPanel5.setPreferredSize(new java.awt.Dimension(821, 50));

        lblDMSPDC.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblDMSPDC.setForeground(new java.awt.Color(51, 102, 255));

        btnRemoveAll.setBackground(new java.awt.Color(214, 31, 44));
        btnRemoveAll.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnRemoveAll.setForeground(new java.awt.Color(255, 255, 255));
        btnRemoveAll.setText("Remove All");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup().addContainerGap()
                        .addComponent(lblDMSPDC, javax.swing.GroupLayout.PREFERRED_SIZE, 277,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 412, Short.MAX_VALUE)
                        .addComponent(btnRemoveAll, javax.swing.GroupLayout.PREFERRED_SIZE, 120,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap()));
        jPanel5Layout.setVerticalGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup().addContainerGap()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel5Layout.createSequentialGroup().addGap(6, 6, 6)
                                        .addComponent(btnRemoveAll, javax.swing.GroupLayout.DEFAULT_SIZE, 32,
                                                Short.MAX_VALUE)
                                        .addContainerGap())
                                .addGroup(jPanel5Layout.createSequentialGroup()
                                        .addComponent(lblDMSPDC, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGap(14, 14, 14)))));

        jPanel4.add(jPanel5, java.awt.BorderLayout.PAGE_START);

        pnlItemChosen.setBackground(new java.awt.Color(245, 245, 245));

        javax.swing.GroupLayout pnlItemChosenLayout = new javax.swing.GroupLayout(pnlItemChosen);
        pnlItemChosen.setLayout(pnlItemChosenLayout);
        pnlItemChosenLayout.setHorizontalGroup(pnlItemChosenLayout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 821, Short.MAX_VALUE));
        pnlItemChosenLayout.setVerticalGroup(pnlItemChosenLayout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 532, Short.MAX_VALUE));

        jPanel4.add(new JScrollPane(pnlItemChosen), java.awt.BorderLayout.CENTER);

        JScrollPane scroll = (JScrollPane) pnlItemChosen.getParent().getParent();
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE,
                "background:$Table.background;track:$Table.background;trackArc:999");
        scroll.getVerticalScrollBar().setUnitIncrement(30);

        pnlItemChosen.setLayout(new MigLayout("wrap, fillx, aligny top", "[fill]", ""));

        jPanel2.add(jPanel4, java.awt.BorderLayout.CENTER);

        add(jPanel2, java.awt.BorderLayout.CENTER);
    }

    private javax.swing.JButton btnMoney1;
    private javax.swing.JButton btnMoney2;
    private javax.swing.JButton btnMoney3;
    private javax.swing.JButton btnMoney4;
    private javax.swing.JButton btnMoney5;
    private javax.swing.JButton btnMoney6;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnRemoveAll;
    private javax.swing.JButton btnThanhToan;
    private javax.swing.JComboBox<String> cmbPTTT;
    private javax.swing.JComboBox<VoucherResponse> cmbVoucher;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JLabel lblCTTT;
    private javax.swing.JLabel lblDMSPDC;
    private javax.swing.JLabel lblHoTen;
    private javax.swing.JLabel lblMoney;
    private javax.swing.JLabel lblNhanVien;
    private javax.swing.JLabel lblPTTT;
    private javax.swing.JLabel lblPhone;
    private javax.swing.JLabel lblSoTienGiam;
    private javax.swing.JLabel lblTTHD;
    private javax.swing.JLabel lblThanhTien;
    private javax.swing.JLabel lblThongTinKH;
    private javax.swing.JLabel lblTienKhachDua;
    private javax.swing.JLabel lblTienThua;
    private javax.swing.JLabel lblTongTienHang;
    private javax.swing.JLabel lblVoucher;
    private javax.swing.JPanel pnlInfo;
    private javax.swing.JPanel pnlItemChosen;
    private javax.swing.JPanel pnlThanhToan;
    private javax.swing.JTextField txtHotenKH;
    private javax.swing.JTextField txtNhanVien;
    private javax.swing.JTextField txtPhone;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtSoTienGiam;
    private javax.swing.JTextField txtTienKhachDua;
    private javax.swing.JTextField txtTienThua;
    private javax.swing.JTextField txtTongTienHang;

    private void applyCartUpdate(MedicineBatchToSellRequest target, MedicineBatchToSellRequest updated) {
        if (target == null || updated == null) {
            return;
        }

        target.setBatchDistributionRequestList(updated.getBatchDistributionRequestList());
        target.setSellingQuantity(updated.getSellingQuantity());
        target.setSellingPrice(updated.getSellingPrice());
        target.setTotalAmount(updated.getTotalAmount());
        target.setTotalQuantity(updated.getTotalQuantity());
    }
}
