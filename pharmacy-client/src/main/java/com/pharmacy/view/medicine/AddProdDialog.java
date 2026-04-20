package com.pharmacy.view.medicine;

import com.pharmacy.shared.dto.request.MedicineRequest;
import com.pharmacy.shared.service.MedicineService;
import com.pharmacy.shared.util.enums.MedicineType;
import com.pharmacy.util.*;
import lombok.extern.slf4j.Slf4j;
import raven.modal.Toast;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

@Slf4j
public class AddProdDialog extends javax.swing.JDialog {

    private static final int WIDTH_IMG = 250;

    private static final int HEIGHT_IMG = 250;

    private String tempAvatarFileName = null;

    private MedicineUI medicineUI;

    private MedicineService medicineService;

    private Frame frame;

    public AddProdDialog(Frame parent, boolean modal, MedicineUI medicineUI) {
        super(parent, modal);

        this.setTitle("Thêm mới sản phẩm");

        this.frame = parent;
        this.medicineService = ClientContext.getService(MedicineService.class);
        this.medicineUI = medicineUI;

        initComponents();

        btnSave.addActionListener(e -> addNewProd());

        btnUpload.addActionListener(e -> uploadImage());

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                SupportImageUtil.cleanupTempFiles();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                SupportImageUtil.cleanupTempFiles();
            }
        });
    }

    private void uploadImage() {
        FileDialog fileDialog = new FileDialog(frame, "Select file to open", FileDialog.LOAD);
        fileDialog.setFile("*.png;*.jpg;*.jpeg");
        fileDialog.setVisible(true);

        String fileName = fileDialog.getFile();
        if (fileName == null)
            return;

        File selectedFile = new File(fileDialog.getDirectory(), fileName);

        try {
            File tempImageDir = new File("temp_images");
            if (!tempImageDir.exists()) {
                tempImageDir.mkdirs();
            }

            String fileExtension = SupportImageUtil.getFileExtension(fileName);
            String tempFileName = "temp_" + System.currentTimeMillis() + "." + fileExtension;
            File tempFile = new File(tempImageDir, tempFileName);

            SupportImageUtil.copyFile(selectedFile, tempFile);

            tempAvatarFileName = tempFileName;

            ImageIcon scaledIcon = HelperImageIcon.scaleIcon(tempFile.getAbsolutePath(), WIDTH_IMG, HEIGHT_IMG);
            lblAvatar.setIcon(scaledIcon);

            log.info("[GUI] Preview image loaded: {}", tempFile.getAbsolutePath());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void saveAvatarToPermanentLocation(String barcode) {
        try {
            File rootDir = new File(PathUtil.getAppPath());
            File imageDir = new File(rootDir, "images/prod");

            if (!imageDir.exists())
                imageDir.mkdirs();

            String fileExtension = SupportImageUtil.getFileExtension(tempAvatarFileName); // png
            String permanentFileName = String.format("%s.%s", barcode, fileExtension); // 8936136161143.png
            File permanentFile = new File(imageDir, permanentFileName);

            File tempFile = new File("temp_images/" + tempAvatarFileName);

            SupportImageUtil.copyFile(tempFile, permanentFile);

            tempAvatarFileName = permanentFileName;

            HelperImageIcon.clearCacheForImage(permanentFileName);

            log.info("[GUI] save avatar successfully with url: {}", permanentFile.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void dispose() {
        SupportImageUtil.cleanupTempFiles();
        super.dispose();
    }

    private MedicineType getMedicineType(int type) {
        return switch (type) {
            case 0 -> MedicineType.ALL;
            case 1 -> MedicineType.MEDICINE;
            case 2 -> MedicineType.FUNCTIONAL_FOODS;
            case 3 -> MedicineType.MEDICAL_INSTRUMENT;
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    private void addNewProd() {
        try {
            MedicineRequest request = new MedicineRequest();

            String barcode = txtBarcode.getText();
            if (!barcode.matches("^[0-9]{6,13}$")) {
                Toast.show(this, Toast.Type.WARNING, "Barcode phải là số và dài từ 6 đến 13 ký tự.");
                txtBarcode.selectAll();
                txtBarcode.requestFocus();
                return;
            }
            request.setBarcode(barcode);

            String tenSanPham = txtTensp.getText();
            if (tenSanPham.isEmpty()) {
                Toast.show(this, Toast.Type.WARNING, "Tên sản phẩm không được để trống.");
                txtTensp.requestFocus();
                return;
            }
            request.setMedicineName(tenSanPham);
            request.setNameWithoutAccents(VietnameseUtil.removeAccents(tenSanPham).toLowerCase());

            String hoatChat = txaHoatChat.getText();

            request.setActiveIngredients(hoatChat);

            request.setDosage((String) cmbBaoChe.getSelectedItem());

            request.setAdministrationRoute((String) cmbDuongDung.getSelectedItem());

            String soDangKy = txtSoDangKi.getText();
            request.setRegistrationNumber(soDangKy);

            request.setQualityStandard((String) cmbTieuChuan.getSelectedItem());
            request.setManufacturingCountry((String) cmbNuocSX.getSelectedItem());
            request.setMeasuringUnit((String) cmbDonViTinh.getSelectedItem());

            String nhaSX = txtNhaSX.getText();
            request.setManufacturer(nhaSX);

            String quyCach = txtQuyCach.getText();
            request.setPackagingSpecification(quyCach);

            String chiDinh = txaChiDinh.getText();
            request.setIndications(chiDinh);

            String chongChiDinh = txaChongChiDinh.getText();
            request.setContraindications(chongChiDinh);

            String lieuDung = txaLieuDung.getText();
            request.setDosage(lieuDung);

            request.setMedicineType(getMedicineType(cmbLoai.getSelectedIndex() + 1));
            request.setDescription(txaMoTa.getText());

            // check avatar is not selected
            if (tempAvatarFileName == null) {
                Toast.show(this, Toast.Type.WARNING, "Vui lòng chọn ảnh sản phẩm.");
                return;
            }

            request.setAvatarUrl(String.format("%s.%s", barcode, SupportImageUtil.getFileExtension(tempAvatarFileName)));

            // save product to db
            medicineService.addMedicine(request);

            saveAvatarToPermanentLocation(barcode);

            JOptionPane.showMessageDialog(this, "Thêm sản phẩm thành công!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            // chuyển sang trang cuối cùng
            medicineUI.showLastPage();

            log.info("[GUI] Product added successfully with id: {}.", request.getMedicineName());

            SupportImageUtil.cleanupTempFiles();

            this.dispose();

        } catch (RuntimeException e) {
            Toast.show(this, Toast.Type.ERROR, e.getMessage());
        }
    }

    public void clearData() {
        txtBarcode.setText("");
        txtTensp.setText("");
        txtSoDangKi.setText("");
        txtNhaSX.setText("");
        txtQuyCach.setText("");

        txaHoatChat.setText("");
        txaChiDinh.setText("");
        txaChongChiDinh.setText("");
        txaLieuDung.setText("");
        txaMoTa.setText("");

        cmbBaoChe.setSelectedIndex(0);
        cmbDuongDung.setSelectedIndex(0);
        cmbTieuChuan.setSelectedIndex(0);
        cmbNuocSX.setSelectedIndex(0);
        cmbLoai.setSelectedIndex(0);
        cmbDonViTinh.setSelectedIndex(0);

        tempAvatarFileName = null;
        lblAvatar.setIcon(new ImageIcon(getClass().getResource("/images/prod/default.png")));
    }

    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        btnUpload = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        lblAvatar = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        txaMoTa = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        lblMaSP = new javax.swing.JLabel();
        txtTensp = new javax.swing.JTextField();
        lblHoatChat = new javax.swing.JLabel();
        txtBarcode = new javax.swing.JTextField();
        lblTensp = new javax.swing.JLabel();
        lblTieuChuan = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txaHoatChat = new javax.swing.JTextArea();
        lblDuongDung = new javax.swing.JLabel();
        lblBaoChe = new javax.swing.JLabel();
        cmbBaoChe = new javax.swing.JComboBox<>();
        cmbDuongDung = new javax.swing.JComboBox<>();
        lblSoDangKi = new javax.swing.JLabel();
        lblNuocSX = new javax.swing.JLabel();
        lblNhaSX = new javax.swing.JLabel();
        txtQuyCach = new javax.swing.JTextField();
        cmbNuocSX = new javax.swing.JComboBox<>();
        cmbTieuChuan = new javax.swing.JComboBox<>();
        txtSoDangKi = new javax.swing.JTextField();
        lblLieuDung = new javax.swing.JLabel();
        lblChiDinh = new javax.swing.JLabel();
        lblChongChiDinh = new javax.swing.JLabel();
        lblLoai = new javax.swing.JLabel();
        txtNhaSX = new javax.swing.JTextField();
        lblDonViTinh = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txaChiDinh = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        txaChongChiDinh = new javax.swing.JTextArea();
        jScrollPane5 = new javax.swing.JScrollPane();
        txaLieuDung = new javax.swing.JTextArea();
        cmbLoai = new javax.swing.JComboBox<>();
        btnSave = new javax.swing.JButton();
        cmbDonViTinh = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(1100, 800));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setPreferredSize(new java.awt.Dimension(440, 850));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setPreferredSize(new java.awt.Dimension(450, 300));
        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setPreferredSize(new java.awt.Dimension(450, 50));

        btnUpload.setBackground(new java.awt.Color(51, 153, 255));
        btnUpload.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnUpload.setForeground(new java.awt.Color(255, 255, 255));
        btnUpload.setText("Upload");
        btnUpload.setPreferredSize(new java.awt.Dimension(100, 35));

        jPanel6.add(btnUpload);

        jPanel3.add(jPanel6, java.awt.BorderLayout.PAGE_END);

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));
        jPanel7.setPreferredSize(new java.awt.Dimension(260, 320));

        lblAvatar.setIcon(new ImageIcon(getClass().getResource("/images/prod/default.png"))); // NOI18N
        lblAvatar.setPreferredSize(new java.awt.Dimension(250, 250));
        jPanel7.add(lblAvatar);

        jPanel3.add(jPanel7, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel3, java.awt.BorderLayout.PAGE_START);

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        jLabel1.setText("Mô tả chung:");

        txaMoTa.setColumns(20);
        txaMoTa.setRows(5);
        jScrollPane4.setViewportView(txaMoTa);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup().addGap(14, 14, 14)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 103,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 411,
                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(15, Short.MAX_VALUE)));
        jPanel5Layout
                .setVerticalGroup(
                        jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel5Layout.createSequentialGroup().addGap(14, 14, 14)
                                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 29,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 356,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap(145, Short.MAX_VALUE)));

        jPanel1.add(jPanel5, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.LINE_START);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setForeground(new java.awt.Color(51, 51, 51));
        jPanel2.setPreferredSize(new java.awt.Dimension(660, 850));

        lblMaSP.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        lblMaSP.setText("Barcode");

        lblHoatChat.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        lblHoatChat.setText("Hoạt chất hàm lượng");

        lblTensp.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        lblTensp.setText("Tên sản phẩm");

        lblTieuChuan.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        lblTieuChuan.setText("Tiêu chuẩn chất lượng");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        jLabel5.setText("Quy cách đóng gói");

        txaHoatChat.setColumns(20);
        txaHoatChat.setRows(5);
        txaHoatChat.setPreferredSize(new java.awt.Dimension(232, 60));
        jScrollPane1.setViewportView(txaHoatChat);

        lblDuongDung.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        lblDuongDung.setText("Đường dùng");

        lblBaoChe.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        lblBaoChe.setText("Dạng bào chế");

        cmbBaoChe.setModel(new javax.swing.DefaultComboBoxModel<>(medicineService.getDosage()));

        cmbDuongDung.setModel(new javax.swing.DefaultComboBoxModel<>(medicineService.getRouteUse()));

        lblSoDangKi.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        lblSoDangKi.setText("Số đăng kí");

        lblNuocSX.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        lblNuocSX.setText("Nước sản xuất");

        lblNhaSX.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        lblNhaSX.setText("Nhà sản xuất");

        cmbNuocSX.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Việt Nam", "Pháp", "Mỹ", "Trung Quốc",
                "Thụy Sĩ", "Nga", "Anh", "Đức", "Cuba", "Nhật Bản", "Hàn Quốc", "Ấn độ", "Argentina"}));

        cmbTieuChuan.setModel(new javax.swing.DefaultComboBoxModel<>(medicineService.getQuantityStandards()));

        lblLieuDung.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        lblLieuDung.setText("Liều dùng");

        lblChiDinh.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        lblChiDinh.setText("Chỉ định");

        lblChongChiDinh.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        lblChongChiDinh.setText("Chống chỉ định");

        lblLoai.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        lblLoai.setText("Loại sản phẩm");

        lblDonViTinh.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        lblDonViTinh.setText("Đơn vị tính");

        txaChiDinh.setColumns(20);
        txaChiDinh.setRows(5);
        jScrollPane2.setViewportView(txaChiDinh);

        txaChongChiDinh.setColumns(20);
        txaChongChiDinh.setRows(5);
        jScrollPane3.setViewportView(txaChongChiDinh);

        txaLieuDung.setColumns(20);
        txaLieuDung.setRows(5);
        jScrollPane5.setViewportView(txaLieuDung);

        cmbLoai.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[]{"Thuốc", "Thực phẩm chức năng", "Dụng cụ y tế"}));

        btnSave.setBackground(new java.awt.Color(0, 153, 51));
        btnSave.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnSave.setText("Save");

        cmbDonViTinh.setModel(new javax.swing.DefaultComboBoxModel<>(medicineService.getUnit()));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup().addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(lblMaSP, javax.swing.GroupLayout.PREFERRED_SIZE, 78,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(txtBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 300,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(182, 182, 182))
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(lblDuongDung, javax.swing.GroupLayout.PREFERRED_SIZE, 81,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(cmbDuongDung, javax.swing.GroupLayout.PREFERRED_SIZE, 230,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(252, 252, 252))
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(lblNuocSX, javax.swing.GroupLayout.PREFERRED_SIZE, 122,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(cmbNuocSX, javax.swing.GroupLayout.PREFERRED_SIZE, 230,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(252, 252, 252))
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(lblSoDangKi, javax.swing.GroupLayout.PREFERRED_SIZE, 122,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(
                                                txtSoDangKi, javax.swing.GroupLayout.PREFERRED_SIZE, 300,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(182, 182, 182))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                        jPanel2Layout.createSequentialGroup().addGroup(jPanel2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addGroup(jPanel2Layout.createSequentialGroup().addGroup(jPanel2Layout
                                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addGroup(jPanel2Layout
                                                                                .createSequentialGroup()
                                                                                .addComponent(lblTensp,
                                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                        Short.MAX_VALUE)
                                                                                .addPreferredGap(
                                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                                                .addGroup(jPanel2Layout.createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                                false)
                                                                                        .addComponent(
                                                                                                lblTieuChuan,
                                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                130, Short.MAX_VALUE)
                                                                                        .addComponent(
                                                                                                lblHoatChat,
                                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                Short.MAX_VALUE)
                                                                                        .addComponent(
                                                                                                lblBaoChe,
                                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                Short.MAX_VALUE))
                                                                                .addGap(42, 42, 42)))
                                                                .addGroup(jPanel2Layout
                                                                        .createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addGroup(jPanel2Layout.createParallelGroup(
                                                                                        javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                        false)
                                                                                .addComponent(jScrollPane1,
                                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                        450, Short.MAX_VALUE)
                                                                                .addComponent(txtTensp))
                                                                        .addComponent(cmbBaoChe,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 230,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(
                                                                                cmbTieuChuan,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 230,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING,
                                                                jPanel2Layout.createSequentialGroup().addGroup(jPanel2Layout
                                                                                .createParallelGroup(
                                                                                        javax.swing.GroupLayout.Alignment.TRAILING)
                                                                                .addGroup(jPanel2Layout
                                                                                        .createSequentialGroup()
                                                                                        .addComponent(
                                                                                                lblChiDinh,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                122,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                        .addGap(500, 500, 500))
                                                                                .addGroup(jPanel2Layout.createSequentialGroup()
                                                                                        .addComponent(lblNhaSX,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                122,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                        .addGap(50, 50, 50)
                                                                                        .addComponent(txtNhaSX,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                450,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                                        .addGap(0, 0, Short.MAX_VALUE))
                                                        .addGroup(jPanel2Layout
                                                                .createSequentialGroup()
                                                                .addGroup(jPanel2Layout
                                                                        .createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(jLabel5,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 122,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(lblChongChiDinh,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 122,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(
                                                                                lblLieuDung,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 122,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(lblLoai,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 122,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(lblDonViTinh))
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addGroup(jPanel2Layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                        .addComponent(txtQuyCach).addComponent(jScrollPane2)
                                                                        .addComponent(
                                                                                jScrollPane3,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE, 450,
                                                                                Short.MAX_VALUE)
                                                                        .addComponent(jScrollPane5)
                                                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                                                .addGroup(jPanel2Layout.createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                                false)
                                                                                        .addComponent(cmbDonViTinh,
                                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                                0,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                Short.MAX_VALUE)
                                                                                        .addComponent(cmbLoai,
                                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                                0, 240, Short.MAX_VALUE))
                                                                                .addPreferredGap(
                                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                                        76, Short.MAX_VALUE)
                                                                                .addComponent(btnSave,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        134,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                                .addGap(32, 32, 32)))));
        jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup().addGap(16, 16, 16)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblMaSP, javax.swing.GroupLayout.PREFERRED_SIZE, 29,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtTensp, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblTensp, javax.swing.GroupLayout.PREFERRED_SIZE, 29,
                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(9, 9, 9)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 77,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblHoatChat, javax.swing.GroupLayout.PREFERRED_SIZE, 29,
                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(cmbBaoChe, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblBaoChe, javax.swing.GroupLayout.PREFERRED_SIZE, 29,
                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblDuongDung, javax.swing.GroupLayout.PREFERRED_SIZE, 29,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cmbDuongDung, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblSoDangKi, javax.swing.GroupLayout.PREFERRED_SIZE, 29,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtSoDangKi, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblTieuChuan, javax.swing.GroupLayout.PREFERRED_SIZE, 29,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cmbTieuChuan, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel2Layout.createSequentialGroup().addGap(15, 15, 15).addComponent(
                                        cmbNuocSX, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel2Layout.createSequentialGroup().addGap(18, 18, 18).addComponent(
                                        lblNuocSX, javax.swing.GroupLayout.PREFERRED_SIZE, 29,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblNhaSX, javax.swing.GroupLayout.PREFERRED_SIZE, 29,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtNhaSX, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(9, 9, 9)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 29,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtQuyCach, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblChiDinh, javax.swing.GroupLayout.PREFERRED_SIZE, 29,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(8, 8, 8)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblChongChiDinh, javax.swing.GroupLayout.PREFERRED_SIZE, 33,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 37,
                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblLieuDung, javax.swing.GroupLayout.PREFERRED_SIZE, 29,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 37,
                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblDonViTinh, javax.swing.GroupLayout.PREFERRED_SIZE, 29,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cmbDonViTinh, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(cmbLoai, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblLoai, javax.swing.GroupLayout.PREFERRED_SIZE, 29,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(18, Short.MAX_VALUE)));

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        pack();
    }

    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnUpload;
    private javax.swing.JComboBox<String> cmbBaoChe;
    private javax.swing.JComboBox<String> cmbDonViTinh;
    private javax.swing.JComboBox<String> cmbDuongDung;
    private javax.swing.JComboBox<String> cmbLoai;
    private javax.swing.JComboBox<String> cmbNuocSX;
    private javax.swing.JComboBox<String> cmbTieuChuan;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTextArea txaMoTa;
    private javax.swing.JLabel lblAvatar;
    private javax.swing.JLabel lblBaoChe;
    private javax.swing.JLabel lblChiDinh;
    private javax.swing.JLabel lblChongChiDinh;
    private javax.swing.JLabel lblDonViTinh;
    private javax.swing.JLabel lblDuongDung;
    private javax.swing.JLabel lblHoatChat;
    private javax.swing.JLabel lblLieuDung;
    private javax.swing.JLabel lblLoai;
    private javax.swing.JLabel lblMaSP;
    private javax.swing.JLabel lblNhaSX;
    private javax.swing.JLabel lblNuocSX;
    private javax.swing.JLabel lblSoDangKi;
    private javax.swing.JLabel lblTensp;
    private javax.swing.JLabel lblTieuChuan;
    private javax.swing.JTextArea txaChiDinh;
    private javax.swing.JTextArea txaChongChiDinh;
    private javax.swing.JTextArea txaHoatChat;
    private javax.swing.JTextArea txaLieuDung;
    private javax.swing.JTextField txtBarcode;
    private javax.swing.JTextField txtNhaSX;
    private javax.swing.JTextField txtQuyCach;
    private javax.swing.JTextField txtSoDangKi;
    private javax.swing.JTextField txtTensp;
}
