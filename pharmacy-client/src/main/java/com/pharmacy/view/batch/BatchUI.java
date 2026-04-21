package com.pharmacy.view.batch;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.pharmacy.shared.dto.request.BatchUpdateRequest;
import com.pharmacy.shared.dto.response.BatchAllResponse;
import com.pharmacy.shared.dto.response.BatchResponse;
import com.pharmacy.shared.dto.response.MedicineResponse;
import com.pharmacy.shared.dto.response.SupplierMiniResponse;
import com.pharmacy.shared.service.BatchService;
import com.pharmacy.shared.service.SupplierService;
import com.pharmacy.shared.util.Pagination;
import com.pharmacy.shared.util.enums.BatchStatus;
import com.pharmacy.shared.util.enums.MedicineType;
import com.pharmacy.util.*;
import com.toedter.calendar.JDateChooser;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BatchUI extends JPanel {

    private static final int pageSize = 20;

    private static final String activePageButtonStyle = "" + "background:$primary;" + "foreground:$white;" + "arc:999;"
            + "margin:5,5,5,5;" + "hoverBackground:$primary;" + "hoverForeground:$white;" + "font:$h6.font;";

    private static final String normalPageButtonStyle = "" + "background:$white;" + "foreground:$black;" + "arc:999;"
            + "margin:5,5,5,5;" + "borderColor:$gray;" + "hoverBackground:#E0E0E0;" + "hoverForeground:$black;"
            + "hoverBorderColor:#007BFF;" + "font:$h6.font;";

    private JPanel pnlMainHeader;

    private JPanel pnlMainBody;

    private BatchService batchService;

    private SupplierService supplierService;

    private AddBatchDialog addBatchDialog;

    private Pagination pagination;

    private JPanel pnlPage;

    private JDateChooser dateFrom;

    private JDateChooser dateTo;

    private boolean useSearchBySoLo = true;

    private int type = 0; // tất cả: 0, đang lưu hành: 1, đã huỷ: 2, đã bán hết: 3, 4: đã hết hạn

    private int filter = 0; // tất cả: 0, 1 -> 7: tùy chỉnh theo rad

    private int option = 0; // 0: ngay nhap, 1: han su dung

    private JLabel lblHeaderSoLo;

    private JLabel lblHeaderProd;

    private JLabel lblHeaderNgayNhap;

    private JLabel lblSoLuongNhap;

    private JLabel lblSoLuongCon;

    private JLabel lblGiaBan;

    public BatchUI() {
        this.batchService = ClientContext.getService(BatchService.class);
        this.supplierService = ClientContext.getService(SupplierService.class);

        batchService.updateBatchStatusWhenExpired();

        initComponents();

        applyPermissions();

        Translator.getInstance().addLanguageChangeListener(locale -> {
            SwingUtilities.invokeLater(this::updateTexts);
        });

        updateTexts();

        initEvent();

        loadRefreshDataToDb();
    }

    private void applyPermissions() {
        btnAdd.setEnabled(ClientSecurityContext.hasPermission("BATCH_ADD"));
        btnExport.setEnabled(ClientSecurityContext.hasPermission("BATCH_EXPORT"));
        btnImport.setEnabled(ClientSecurityContext.hasPermission("BATCH_EXPORT"));
    }

    public void loadRefreshDataToDb() {
        int total = batchService.getTotalBatch();
        pagination = new Pagination(1, pageSize, total);
        txtTotalRecord.setText(String.valueOf(total));

        initPage(batchService.getAllBatchByPage(pagination, option));
    }

    private void initEvent() {
        btnAdd.addActionListener(e -> addBatch());

        btnExport.addActionListener(e -> exportBatchToExcel());

        txtSearchSoLo.addActionListener(e -> searchBySoLo());

        txtSearchMaSP.addActionListener(e -> searchByBarcode());

        radStatusAll.addActionListener(e -> {
            type = 0;
            applyFilters();
        });

        radDangLuuHanh.addActionListener(e -> {
            type = 1;
            applyFilters();
        });

        radDaHuy.addActionListener(e -> {
            type = 2;
            applyFilters();
        });

        radDaBanHet.addActionListener(e -> {
            type = 3;
            applyFilters();
        });

        radDaHetHan.addActionListener(e -> {
            type = 4;
            applyFilters();
        });

        radTimeAll.addActionListener(e -> {
            filter = 0;
            applyFilters();
        });

        radToday.addActionListener(e -> {
            filter = 1;
            applyFilters();
        });

        radWeek.addActionListener(e -> {
            filter = 2;
            applyFilters();
        });

        radMonth.addActionListener(e -> {
            filter = 3;
            applyFilters();
        });

        radDiff.addChangeListener(e -> {
            filter = 4;

            boolean enable = radDiff.isSelected();

            txtTimeStart.setEnabled(enable);
            txtTimeEnd.setEnabled(enable);

            if (!enable) {
                txtTimeStart.setText("");
                txtTimeEnd.setText("");
                dateFrom.setVisible(false);
                dateTo.setVisible(false);
            }
        });

        radHetHan7Ngay.addActionListener(e -> {
            filter = 5;
            applyFilters();
        });
        radHetHan30Ngay.addActionListener(e -> {
            filter = 6;
            applyFilters();
        });

        radHetHan3Thang.addActionListener(e -> {
            filter = 7;
            applyFilters();
        });

        txtTimeStart.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!radDiff.isSelected())
                    return;

                dateFrom.setVisible(true);
                dateFrom.getCalendarButton().doClick();
            }
        });

        txtTimeEnd.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!radDiff.isSelected())
                    return;

                dateTo.setVisible(true);
                dateTo.getCalendarButton().doClick();
            }
        });

        dateFrom.getDateEditor().addPropertyChangeListener("date", evt -> {
            Date date = dateFrom.getDate();
            if (date != null) {
                txtTimeStart.setText(new SimpleDateFormat("dd/MM/yyyy").format(date));
                applyFilters();
            }
        });

        dateTo.getDateEditor().addPropertyChangeListener("date", evt -> {
            Date date = dateTo.getDate();
            if (date != null) {
                txtTimeEnd.setText(new SimpleDateFormat("dd/MM/yyyy").format(date));
                applyFilters();
            }
        });

        cmbOption.addActionListener(e -> {
            option = cmbOption.getSelectedIndex();
            applyFilters();
        });
    }

    private void exportBatchToExcel() {
        LocalDate startDate = batchService.convertStringToLocalDate(filter, txtTimeStart.getText());
        LocalDate endDate = batchService.convertStringToLocalDate(filter, txtTimeEnd.getText());

        List<BatchResponse> list = batchService.getAllBatchToExportCSV(type, filter, startDate, endDate, option);
        String[] headers = {"Số lô", "Barcode - Tên thuốc", "Nhà cung cấp", "Nhân viên nhập", "Ngày sản xuất",
                "Hạn sử dụng", "Ngày nhập", "Số lượng nhập", "DVT", "Giá nhập", "Thành tiền", "Số lượng còn", "Giá bán",
                "Trạng thái lô"};

        List<Object[]> data = new ArrayList<>();
        for (BatchResponse b : list) {
            data.add(new Object[]{});
        }

        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

        FileDialog fileDialog = new FileDialog(parentFrame, "Xuất danh sách lô thuốc ra Excel", FileDialog.SAVE);

        fileDialog.setFile("Danh_Sach_Lo_Thuoc.xlsx");

        fileDialog.setFilenameFilter((dir, name) -> name.endsWith(".xlsx"));

        fileDialog.setVisible(true);

        String directory = fileDialog.getDirectory();
        String filename = fileDialog.getFile();

        if (filename != null && directory != null) {
            String filePath = directory + filename;

            if (!filePath.toLowerCase().endsWith(".xlsx"))
                filePath += ".xlsx";

            try {
                ExcelExporterUtil.exportDataToExcel(filePath, "DanhSachLoThuoc", headers, data);
                JOptionPane.showMessageDialog(this, "Xuất file thành công!");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi xuất file!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void applyFilters() {
        LocalDate startDate = batchService.convertStringToLocalDate(filter, txtTimeStart.getText());
        LocalDate endDate = batchService.convertStringToLocalDate(filter, txtTimeEnd.getText());

        String keyword = useSearchBySoLo ? txtSearchSoLo.getText() : txtSearchMaSP.getText();

        // Bước 1: tính tổng record
        int totalRecords;
        if (keyword.isEmpty()) {
            totalRecords = batchService.countRecordFilteredByStatusAndDate(type, filter, startDate, endDate);
        } else {
            totalRecords = useSearchBySoLo
                    ? batchService.countRecordFilteredByStatusAndDateAndSearchByBatchNumber(type, filter, startDate, endDate, keyword)
                    : batchService.countRecordFilteredByStatusAndDateAndSearchByBarcode(type, filter, startDate, endDate, keyword);
        }

        txtTotalRecord.setText(String.valueOf(totalRecords));

        // Bước 2: khởi tạo pagination
        pagination = new Pagination(1, pageSize, totalRecords);

        // Show list batch filtered
        List<BatchResponse> list = keyword.isEmpty()
                ? batchService.getBatchByStatusAndDate(pagination, type, filter, startDate, endDate, option)
                : useSearchBySoLo
                ? batchService.getBatchByStatusAndDateAndSearchByBatchNumber(pagination, type, filter, startDate, endDate, option, keyword)
                : batchService.getBatchByStatusAndDateAndSearchByBarcode(pagination, type, filter, startDate, endDate, option, keyword);

        initPage(list);
    }

    private void searchBySoLo() {
        useSearchBySoLo = true;
        txtSearchMaSP.setText("");
        applyFilters();
    }

    private void searchByBarcode() {
        useSearchBySoLo = false;
        txtSearchSoLo.setText("");
        applyFilters();
    }

    public void showPage(int pageNum) {
        if (pageNum == 0 || pageNum > pagination.getTotalPages())
            pageNum = 1;

        pagination.setPageNumber(pageNum);

        LocalDate startDate = batchService.convertStringToLocalDate(filter, txtTimeStart.getText());
        LocalDate endDate = batchService.convertStringToLocalDate(filter, txtTimeEnd.getText());

        String keyword = useSearchBySoLo ? txtSearchSoLo.getText() : txtSearchMaSP.getText();

        List<BatchResponse> list = keyword.isEmpty()
                ? batchService.getBatchByStatusAndDate(pagination, type, filter, startDate, endDate, option)
                : useSearchBySoLo
                ? batchService.getBatchByStatusAndDateAndSearchByBatchNumber(pagination, type, filter, startDate, endDate, option, keyword)
                : batchService.getBatchByStatusAndDateAndSearchByBarcode(pagination, type, filter, startDate, endDate, option, keyword);

        updatePageButtons(pageNum);
        loadData(list);
    }

    private void updateTexts() {
        Translator lang = Translator.getInstance();

        lblHeaderSoLo.setText(lang.getString("batch.lbl.batch"));
        lblHeaderProd.setText(lang.getString("batch.lbl.product"));
        lblHeaderNgayNhap.setText(lang.getString("batch.lbl.entry_date"));
        lblHanSuDung.setText(lang.getString("batch.lbl.exp_date"));
        lblSoLuongNhap.setText(lang.getString("batch.lbl.qty_in"));
        lblSoLuongCon.setText(lang.getString("batch.lbl.qty_left"));
        lblGiaBan.setText(lang.getString("batch.lbl.sale_price"));
        lblTrangThai.setText(lang.getString("batch.lbl.status"));

        radDangLuuHanh.setText(lang.getString("batch.rad.active"));
        radStatusAll.setText(lang.getString("batch.rad.all"));
        radDaBanHet.setText(lang.getString("batch.rad.sold_out"));
        radDaHuy.setText(lang.getString("batch.rad.cancelled"));
        radDaHetHan.setText(lang.getString("batch.rad.expired"));

        radTimeAll.setText(lang.getString("batch.rad.time_all"));
        radToday.setText(lang.getString("batch.rad.today"));
        radWeek.setText(lang.getString("batch.rad.week"));
        radMonth.setText(lang.getString("batch.rad.month"));
        radDiff.setText(lang.getString("batch.rad.other"));

        radHetHan7Ngay.setText(lang.getString("batch.rad.exp_7days"));
        radHetHan30Ngay.setText(lang.getString("batch.rad.exp_30days"));
        radHetHan3Thang.setText(lang.getString("batch.rad.exp_3months"));

        // Labels & buttons
        lblThoiGian.setText(lang.getString("batch.lbl.time_filter"));
        lblNgayNhap.setText(lang.getString("batch.lbl.entry_date"));
        lblHanSuDung.setText(lang.getString("batch.lbl.exp_date"));

        lblQuanLyLoHang.setText(lang.getString("batch.lbl.management"));
        btnAdd.setText(lang.getString("batch.btn.add"));

        txtSearchSoLo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, lang.getString("batch.search.batch"));
        txtSearchMaSP.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, lang.getString("batch.search.prod"));
    }

    private void initPage(List<BatchResponse> list) {
        pnlPage.removeAll();

        JButton btnPageFirst = new JButton(new FlatSVGIcon("icon/svg/prev.svg", 30, 30));
        btnPageFirst.setFocusPainted(false);
        btnPageFirst.setBorderPainted(false);
        btnPageFirst.setContentAreaFilled(false);
        btnPageFirst.addActionListener(e -> showPage(1));
        pnlPage.add(btnPageFirst);

        for (int i = 1; i <= pagination.getTotalPages(); i++) {
            JButton btn = new JButton(String.valueOf(i));
            stylePageButton(btn, i == pagination.getPageNumber());
            final int pageNum = i;
            btn.addActionListener(e -> showPage(pageNum));
            pnlPage.add(btn);
        }

        JButton btnPageEnd = new JButton(new FlatSVGIcon("icon/svg/next.svg", 30, 30));
        btnPageEnd.setFocusPainted(false);
        btnPageEnd.setBorderPainted(false);
        btnPageEnd.setContentAreaFilled(false);
        btnPageEnd.addActionListener(e -> showPage(pagination.getTotalPages()));
        pnlPage.add(btnPageEnd);

        pnlPage.revalidate();
        pnlPage.repaint();

        loadData(list); // hiển thị dữ liệu page hiện tại
    }

    private void updatePageButtons(int activePage) {
        for (Component c : pnlPage.getComponents()) {
            if (c instanceof JButton b && b.getText() != null && b.getText().matches("\\d+")) {
                int pageNum = Integer.parseInt(b.getText());
                stylePageButton(b, pageNum == activePage);
            }
        }
    }

    private void stylePageButton(JButton btn, boolean active) {
        btn.putClientProperty(FlatClientProperties.STYLE, active ? activePageButtonStyle : normalPageButtonStyle);
    }

    public void loadData(List<BatchResponse> list) {
        if (list == null)
            return;

        try {
            pnlMainBody.setVisible(false);
            pnlMainBody.removeAll();

            for (int i = 0; i < list.size(); i++) {
                BatchResponse batchResponse = list.get(i);
                pnlMainBody.add(createAccordionSection(batchResponse));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pnlMainBody.revalidate();
            pnlMainBody.repaint();
            pnlMainBody.setVisible(true);
        }
    }

    private void addBatch() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);

        JPanel glass = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 90));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        glass.setOpaque(false);

        frame.setGlassPane(glass);
        glass.setVisible(true);

        if (addBatchDialog == null)
            addBatchDialog = new AddBatchDialog(frame, true, this);
        else
            addBatchDialog.clearData();

        addBatchDialog.setLocationRelativeTo(frame);
        addBatchDialog.setVisible(true);

        glass.setVisible(false);
    }

    public void refreshData() {
        txtSearchMaSP.setText("");
        cmbOption.setSelectedIndex(0);
        txtSearchSoLo.setText("");
        radStatusAll.setSelected(true);
        radTimeAll.setSelected(true);

        loadRefreshDataToDb();
    }

    // tạo phần accordion như bootstrap
    private JPanel createAccordionSection(BatchResponse batchResponse) {
        JPanel pnlMain = new JPanel();
        pnlMain.setLayout(new BorderLayout());
        pnlMain.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        pnlMain.setBackground(Color.WHITE);

        // header, dòng tiêu đề nội dung khi chưa show xuống
        JPanel pnlHeader = new JPanel();
        pnlHeader.setBackground(Color.WHITE);

        pnlHeader.setLayout(new MigLayout("fillx, insets 10",
                "[30!]10[200!]10[fill]10[130!]15[80!]15[80!]15[80!]15[100!][180!]", "[]"));

        JButton btn = new JButton();
        btn.setIcon(new FlatSVGIcon("icon/svg/down.svg", 15, 15));

        btn.setPreferredSize(new Dimension(25, 25));
        btn.setMaximumSize(new Dimension(25, 25));
        btn.setMinimumSize(new Dimension(25, 25));

        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setBackground(Color.WHITE);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.STYLE, "background:null; borderWidth:0; focusWidth:0;");

        pnlHeader.add(btn);

        JLabel lblMaGiamGia = new JLabel();
        lblMaGiamGia.setText(batchResponse.getBatchNumber());
        pnlHeader.add(lblMaGiamGia);

        JLabel lblGiaTri = new JLabel();
        lblGiaTri.setText(batchResponse.getMedicine().getMedicineName());
        pnlHeader.add(lblGiaTri);

        JLabel lblNgayTao = new JLabel();
        lblNgayTao.setText(FormatUtil.formatDate(batchResponse.getImportDate()));

        pnlHeader.add(lblNgayTao);

        JLabel lblHanSuDung = new JLabel();
        lblHanSuDung.setText(FormatUtil.formatDate(batchResponse.getExpirationDate()));
        lblHanSuDung.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlHeader.add(lblHanSuDung);

        JLabel lblSoLuot = new JLabel();
        lblSoLuot.setText(String.format("%d %s", batchResponse.getImportQuantity(), batchResponse.getMedicine().getMeasuringUnit()));
        pnlHeader.add(lblSoLuot);

        JLabel lblSoLuot1 = new JLabel();
        lblSoLuot1.setText(String.format("%d %s", batchResponse.getRemainingQuantity(), batchResponse.getMedicine().getMeasuringUnit()));
        lblSoLuot1.setForeground(Color.BLACK);
        lblSoLuot1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlHeader.add(lblSoLuot1);

        JLabel lblSoLuot2 = new JLabel();
        lblSoLuot2.setText(FormatUtil.formatVND(batchResponse.getSellingPrice()) + " đ");
        lblSoLuot2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlHeader.add(lblSoLuot2);

        JButton btnTrangThai = new JButton();
        btnTrangThai.setText(batchResponse.getBatchStatus().toString());
        btnTrangThai.setBackground(new Color(204, 255, 204));

        BatchStatus status = batchResponse.getBatchStatus();
        switch (status) {
            case BatchStatus.SELLING -> btnTrangThai.setForeground(new Color(0, 100, 0));
            case BatchStatus.CANCELLED -> btnTrangThai.setForeground(new Color(251, 188, 4));
            case BatchStatus.SOLD_OUT -> btnTrangThai.setForeground(new Color(229, 62, 49));
            case BatchStatus.EXPIRED -> btnTrangThai.setForeground(new Color(123, 31, 162));
        }

        if (status == BatchStatus.SELLING) {
            long noOfDaysBetween = ChronoUnit.DAYS.between(LocalDate.now(), batchResponse.getExpirationDate());
            if (noOfDaysBetween <= 30) {
                btnTrangThai.setText("Sắp hết hạn. Không được bán");
                btnTrangThai.setForeground(new Color(255, 51, 51));
            } else if (noOfDaysBetween <= 90) {
                btnTrangThai.setText("Sắp hết hạn");
                btnTrangThai.setForeground(new Color(250, 184, 0));
            }
        }

        btnTrangThai.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnTrangThai.setFocusPainted(false);
        btnTrangThai.setContentAreaFilled(false);
        btnTrangThai.setBorderPainted(false);
        btnTrangThai.setOpaque(false);
        btnTrangThai.setHorizontalAlignment(SwingConstants.CENTER);

        pnlHeader.add(btnTrangThai);

        // content, mở nội dung khi ấn nút show xuống
        JPanel pnlContent = new JPanel();
        pnlContent.setVisible(false);

        btn.addActionListener(e -> {
            boolean value = pnlContent.isVisible();
            if (!value && pnlContent.getComponentCount() == 0)
                initPnlContent(pnlContent, batchResponse.getId());

            pnlContent.setVisible(!value);
            btn.setIcon(new FlatSVGIcon(String.format("icon/svg/%s.svg", value ? "down" : "top"), 15, 15));
        });

        pnlMain.add(pnlHeader, BorderLayout.NORTH);
        pnlMain.add(pnlContent, BorderLayout.CENTER);

        return pnlMain;
    }

    private int mapStatusByInt(BatchStatus status) {
        return switch (status) {
            case SELLING -> 1;
            case CANCELLED -> 2;
            case SOLD_OUT -> 3;
            case EXPIRED -> 4;
        };
    }

    // Tạo phần content chứa nội dung chi tiết của batch
    private void initPnlContent(JPanel pnlContent, Long id) {
        BatchAllResponse batchResponse = batchService.getBatchAllFieldById(id);
        pnlContent.setLayout(new BorderLayout());

        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        pnlTitle.setBackground(new Color(217, 217, 217));
        JLabel lblTitle = new JLabel("Chi tiết");
        lblTitle.setForeground(Color.BLACK);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pnlTitle.add(lblTitle);

        JPanel pnlBody = new JPanel();
        pnlBody.setLayout(new MigLayout("wrap 4, fillx, insets 10 30 10 30", "[left][grow,fill]40[left][grow,fill]",
                "[]10[]10[]10[]10[]10[]10[]10[]"));

        Font labelFont = new Font("Segoe UI", Font.BOLD, 12);

        JLabel lblSoLo = new JLabel();
        lblSoLo.setFont(labelFont);
        JTextField txtSoLo = new JTextField();
        txtSoLo.setText(batchResponse.getBatchNumber());
        txtSoLo.setEditable(false);

        JLabel lblMaSP = new JLabel();
        lblMaSP.setFont(labelFont);
        JTextField txtMaSP = new JTextField();
        txtMaSP.setEditable(false);
        txtMaSP.setText(batchResponse.getMedicine().getMedicineName());

        pnlBody.add(lblSoLo);
        pnlBody.add(txtSoLo);
        pnlBody.add(lblMaSP);
        pnlBody.add(txtMaSP);

        JLabel lblNgaySX = new JLabel();
        lblNgaySX.setFont(labelFont);
        JTextField txtNgaySX = new JTextField();
        txtNgaySX.setText(FormatUtil.formatDate(batchResponse.getManufacturingDate()));

        JLabel lblNCC = new JLabel();
        lblNCC.setFont(labelFont);
        JComboBox<SupplierMiniResponse> cmbNCC = new JComboBox<>();

        supplierService.getAllSupplier().forEach(ncc -> cmbNCC.addItem(ncc));

        cmbNCC.setSelectedItem(batchResponse.getSupplier());

        pnlBody.add(lblNgaySX);
        pnlBody.add(txtNgaySX);
        pnlBody.add(lblNCC);
        pnlBody.add(cmbNCC);

        JLabel lblHanSuDung = new JLabel();
        lblHanSuDung.setFont(labelFont);
        JTextField txtHanSuDung = new JTextField();
        txtHanSuDung.setText(FormatUtil.formatDate(batchResponse.getExpirationDate()));

        JPanel pnl1 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        JLabel lblSoLuongNhap = new JLabel();
        lblSoLuongNhap.setFont(labelFont);
        JTextField txtSoLuongNhap = new JTextField();
        txtSoLuongNhap.setText(String.valueOf(batchResponse.getImportQuantity()));
        txtSoLuongNhap.setPreferredSize(new Dimension(170, 30));

        JTextField txtDonViTinh1 = new JTextField();
        txtDonViTinh1.setEditable(false);
        txtDonViTinh1.setText(batchResponse.getMedicine().getMeasuringUnit());
        txtDonViTinh1.setPreferredSize(new Dimension(80, 30));

        pnl1.setBackground(Color.WHITE);
        pnl1.add(txtSoLuongNhap);
        pnl1.add(txtDonViTinh1);

        pnlBody.add(lblHanSuDung);
        pnlBody.add(txtHanSuDung);
        pnlBody.add(lblSoLuongNhap);
        pnlBody.add(pnl1);

        JLabel lblNgayNhap = new JLabel();
        lblNgayNhap.setFont(labelFont);
        JTextField txtNgayNhap = new JTextField();
        txtNgayNhap.setEditable(false);
        txtNgayNhap.setText(FormatUtil.formatDate(batchResponse.getImportDate()));

        JLabel lblSoLuongCon = new JLabel();
        lblSoLuongCon.setFont(labelFont);
        JPanel pnl2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        JTextField txtSoLuongCon = new JTextField();
        txtSoLuongCon.setText(String.valueOf(batchResponse.getRemainingQuantity()));
        txtSoLuongCon.setPreferredSize(txtSoLuongNhap.getPreferredSize());

        JTextField txtDonViTinh2 = new JTextField();
        txtDonViTinh2.setEditable(false);
        txtDonViTinh2.setText(batchResponse.getMedicine().getMeasuringUnit());
        txtDonViTinh2.setPreferredSize(new Dimension(80, 30));

        pnl2.setBackground(Color.WHITE);
        pnl2.add(txtSoLuongCon);
        pnl2.add(txtDonViTinh2);

        pnlBody.add(lblNgayNhap);
        pnlBody.add(txtNgayNhap);
        pnlBody.add(lblSoLuongCon);
        pnlBody.add(pnl2);

        JLabel lblNhanVien = new JLabel();
        lblNhanVien.setFont(labelFont);
        JTextField txtNhanVien = new JTextField();
        txtNhanVien.setEditable(false);
        txtNhanVien.setText(batchResponse.getEmployee().getFullName());

        JLabel lblGiaNhap = new JLabel();
        lblGiaNhap.setFont(labelFont);
        JTextField txtGiaNhap = new JTextField();
        txtGiaNhap.setText(String.format("%.0f", batchResponse.getImportPrice()));

        pnlBody.add(lblNhanVien);
        pnlBody.add(txtNhanVien);
        pnlBody.add(lblGiaNhap);
        pnlBody.add(txtGiaNhap);

        JLabel lblTrangThai = new JLabel();
        lblTrangThai.setFont(labelFont);

        Translator lang = Translator.getInstance();
        int status = mapStatusByInt(batchResponse.getBatchStatus());
        JComboBox<String> cboTrangThai;

        if (status >= 3) {
            String statusText = "";
            if (status == 3)
                statusText = lang.getString("batch.status.sold_out"); // "Đã bán hết"
            else if (status == 4)
                statusText = lang.getString("batch.status.expired"); // "Đã hết hạn"
            else
                statusText = "Không xác định";

            String[] singleOption = {statusText};
            cboTrangThai = new JComboBox<>(singleOption);

            cboTrangThai.setSelectedIndex(0);

            cboTrangThai.setEnabled(false);

        } else {
            String[] statusOptions = {lang.getString("batch.status.active"),
                    lang.getString("batch.status.cancelled")};

            cboTrangThai = new JComboBox<>(statusOptions);

            int index = status - 1;
            if (index >= 0 && index < statusOptions.length)
                cboTrangThai.setSelectedIndex(index);
        }

        JLabel lblGiaBan = new JLabel();
        lblGiaBan.setFont(labelFont);
        JTextField txtGiaBan = new JTextField();
        txtGiaBan.setText(String.format("%.0f", batchResponse.getSellingPrice()));

        pnlBody.add(lblTrangThai);
        pnlBody.add(cboTrangThai);
        pnlBody.add(lblGiaBan);
        pnlBody.add(txtGiaBan);

        JLabel lblThanhTien = new JLabel();
        lblThanhTien.setFont(labelFont);
        JTextField txtThanhTien = new JTextField();
        txtThanhTien.setEditable(false);
        txtThanhTien.setText(FormatUtil.formatVND(batchResponse.getTotalAmount()) + " VND");

        if (!ClientSecurityContext.hasPermission("BATCH_EDIT")) {
            txtGiaNhap.setText("");
            txtGiaNhap.setEditable(false);
            txtGiaBan.setEditable(false);
            txtThanhTien.setText("");
        }

        pnlBody.add(new JLabel());
        pnlBody.add(new JLabel());
        pnlBody.add(lblThanhTien);
        pnlBody.add(txtThanhTien);

        JButton btn = new JButton("Save");
        btn.setPreferredSize(new Dimension(80, 30));
        btn.setMinimumSize(new Dimension(80, 30));
        btn.setMaximumSize(new Dimension(80, 30));
        btn.setBackground(Color.BLUE);
        btn.setForeground(Color.WHITE);
        btn.setFont(labelFont);
        btn.setEnabled(ClientSecurityContext.hasPermission("BATCH_EDIT"));
        pnlBody.add(btn, "span 4, align right, pushx");

        lblSoLo.setText(lang.getString("batch.lbl.batch"));
        lblMaSP.setText(lang.getString("batch.lbl.product"));
        lblNgaySX.setText(lang.getString("batch.lbl.mfg_date"));
        lblNCC.setText(lang.getString("batch.lbl.supplier"));
        lblHanSuDung.setText(lang.getString("batch.lbl.exp_date"));
        lblSoLuongNhap.setText(lang.getString("batch.lbl.qty_in"));
        lblNgayNhap.setText(lang.getString("batch.lbl.entry_date"));
        lblSoLuongCon.setText(lang.getString("batch.lbl.qty_left"));
        lblNhanVien.setText(lang.getString("batch.lbl.employee"));
        lblGiaNhap.setText(lang.getString("batch.lbl.cost_price"));
        lblTrangThai.setText(lang.getString("batch.lbl.status"));

        lblGiaBan.setText(lang.getString("batch.lbl.sale_price"));
        lblThanhTien.setText(lang.getString("batch.lbl.total"));

        btn.addActionListener(e -> {
            updateBatch(batchResponse, txtNgaySX, txtHanSuDung, cboTrangThai, cmbNCC, txtSoLuongNhap, txtSoLuongCon,
                    txtGiaNhap, txtGiaBan);
        });

        txtGiaNhap.addActionListener(e -> {
            txtGiaBan.selectAll();
            txtGiaBan.requestFocus();
        });

        txtGiaNhap.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                txtThanhTien.setText(String
                        .valueOf(FormatUtil.formatVND(
                                batchService.calculateMoney(txtSoLuongNhap.getText(), txtGiaNhap.getText())))
                        + " VND");

                suggestGiaBan();
            }

            private void suggestGiaBan() {
                try {
                    MedicineResponse medicine = batchResponse.getMedicine();
                    if (medicine == null)
                        return;

                    MedicineType medicineType = medicine.getMedicineType();
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

            @Override
            public void insertUpdate(DocumentEvent e) {
                txtThanhTien.setText(String
                        .valueOf(FormatUtil.formatVND(
                                batchService.calculateMoney(txtSoLuongNhap.getText(), txtGiaNhap.getText())))
                        + " VND");

                suggestGiaBan();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        txtSoLuongNhap.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                txtThanhTien.setText(String
                        .valueOf(FormatUtil.formatVND(
                                batchService.calculateMoney(txtSoLuongNhap.getText(), txtGiaNhap.getText())))
                        + " VND");
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                txtThanhTien.setText(String
                        .valueOf(FormatUtil.formatVND(
                                batchService.calculateMoney(txtSoLuongNhap.getText(), txtGiaNhap.getText())))
                        + " VND");
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        pnlBody.setBackground(Color.WHITE);
        pnlContent.add(pnlTitle, BorderLayout.NORTH);
        pnlContent.add(pnlBody, BorderLayout.CENTER);

    }

    private void updateBatch(BatchAllResponse batch, JTextField txtNgaySX, JTextField txtHanSuDung,
                                JComboBox<String> cboTrangThai, JComboBox<SupplierMiniResponse> cmbNCC, JTextField txtSoLuongNhap,
                                JTextField txtSoLuongCon, JTextField txtGiaNhap, JTextField txtGiaBan) {

        if (cboTrangThai.getItemCount() == 1) {
            JOptionPane.showMessageDialog(this, "Lô thuốc này không thể cập nhật vì đã bán hết hoặc đã hết hạn.");
            return;
        }

        try {
            LocalDate ngaySX = FormatUtil.convertStringToDate(txtNgaySX.getText()); // dd/MM/yyyy
            LocalDate hanSD = FormatUtil.convertStringToDate(txtHanSuDung.getText()); // dd/MM/yyyy

            batchService.checkDate(ngaySX, hanSD);

            long months = ChronoUnit.MONTHS.between(LocalDate.now(), hanSD);
            if (months <= 18) {
                int confirm = JOptionPane.showConfirmDialog(this, "Hạn sử dụng của lô thuốc này chỉ còn: " + months
                        + " tháng.\n Bạn có chắc chắn muốn nhập lô ?");
                if (confirm != JOptionPane.YES_OPTION)
                    return;
            }

            BatchUpdateRequest request = new BatchUpdateRequest();

            request.setManufacturingDate(ngaySX);
            request.setExpirationDate(hanSD);
            request.setSupplierId(((SupplierMiniResponse) cmbNCC.getSelectedItem()).getId());

            try {
                request.setImportQuantity(Integer.parseInt(txtSoLuongNhap.getText().trim()));
                request.setRemainingQuantity(Integer.parseInt(txtSoLuongCon.getText().trim()));
                request.setImportPrice(Double.parseDouble(txtGiaNhap.getText().trim()));
                request.setSellingPrice(Double.parseDouble(txtGiaBan.getText().trim()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Các trường số lượng và giá phải là số hợp lệ!");
            }

            batchService.checkQuantityAndPrice(request.getImportPrice(), request.getSellingPrice(), request.getRemainingQuantity(), request.getImportQuantity());

            request.setBatchStatus(convertIntToBatchStatus(cboTrangThai.getSelectedIndex() + 1));

            batchService.updateBatch(request);

            JOptionPane.showMessageDialog(this, "Chỉnh sửa thông tin lô thuốc thành công!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            loadData(batchService.getAllBatchByPage(pagination, option));

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private BatchStatus convertIntToBatchStatus(int i) {
        return switch (i) {
            case 1 -> BatchStatus.SELLING;
            case 2 -> BatchStatus.CANCELLED;
            case 3 -> BatchStatus.SOLD_OUT;
            case 4 -> BatchStatus.EXPIRED;
            default -> throw new IllegalStateException("Unexpected value: " + i);
        };
    }

    private void initMainBody() {
        pnlMain.setLayout(new BorderLayout());

        // Tạo dòng tiêu đề như table
        pnlMainHeader = new JPanel();
        pnlMainHeader.setLayout(new MigLayout("wrap, fillx, insets 10",
                "[fill][fill][fill][fill][fill][fill][fill][fill][fill]", "[]"));
        pnlMainHeader.setBackground(new Color(238, 238, 238));

        JLabel lblIcon = new JLabel();
        pnlMainHeader.add(lblIcon);

        lblHeaderSoLo = new JLabel();
        pnlMainHeader.add(lblHeaderSoLo);

        lblHeaderProd = new JLabel();
        pnlMainHeader.add(lblHeaderProd);

        lblHeaderNgayNhap = new JLabel();
        pnlMainHeader.add(lblHeaderNgayNhap);

        lblHanSuDung = new JLabel();
        pnlMainHeader.add(lblHanSuDung);

        lblSoLuongNhap = new JLabel();
        pnlMainHeader.add(lblSoLuongNhap);

        lblSoLuongCon = new JLabel();
        pnlMainHeader.add(lblSoLuongCon);

        lblGiaBan = new JLabel();
        pnlMainHeader.add(lblGiaBan);

        JLabel lblTrangThai = new JLabel();
        pnlMainHeader.add(lblTrangThai);

        JLabel lbl[] = {lblHeaderSoLo, lblHeaderProd, lblHeaderNgayNhap, lblHanSuDung, lblSoLuongNhap, lblSoLuongCon,
                lblGiaBan, lblTrangThai};

        for (JLabel label : lbl) {
            label.setForeground(new Color(119, 119, 119));
            label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        }

        // phần main ở dưới, tạo giống từng row của table
        pnlMainBody = new JPanel();
        pnlMainBody.setBackground(Color.WHITE);
        pnlMainBody.setLayout(new MigLayout("wrap, fillx, aligny top", "[fill]", ""));

        pnlPage = new JPanel();
        pnlPage.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 0));

        pnlMain.add(pnlMainHeader, BorderLayout.NORTH);
        pnlMain.add(new JScrollPane(pnlMainBody), BorderLayout.CENTER);
        pnlMain.add(pnlPage, BorderLayout.SOUTH);

        JScrollPane scroll = (JScrollPane) pnlMainBody.getParent().getParent();
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE,
                "background:$Table.background;track:$Table.background;trackArc:999");
        scroll.getVerticalScrollBar().setUnitIncrement(30);

    }

    private void initComponents() {

        jPanel1 = new JPanel();
        lblTimKiem = new JLabel();
        jLabel4 = new JLabel();
        txtSearchSoLo = new JTextField();
        txtSearchMaSP = new JTextField();
        cmbOption = new JComboBox<>();
        jLabel5 = new JLabel();
        lblTrangThai = new JLabel();
        radDangLuuHanh = new JRadioButton();
        radStatusAll = new JRadioButton();
        radDaBanHet = new JRadioButton();
        radDaHuy = new JRadioButton();
        radDaHetHan = new JRadioButton();
        jLabel6 = new JLabel();
        lblThoiGian = new JLabel();
        lblNgayNhap = new JLabel();
        radTimeAll = new JRadioButton();
        radToday = new JRadioButton();
        radWeek = new JRadioButton();
        radMonth = new JRadioButton();
        radDiff = new JRadioButton();
        txtTimeEnd = new JTextField();
        txtTimeStart = new JTextField();
        jLabel2 = new JLabel();
        lblHanSuDung = new JLabel();
        radHetHan7Ngay = new JRadioButton();
        radHetHan30Ngay = new JRadioButton();
        radHetHan3Thang = new JRadioButton();
        jPanel2 = new JPanel();
        jPanel3 = new JPanel();
        lblQuanLyLoHang = new JLabel();
        btnExport = new JButton();
        btnAdd = new JButton();
        btnImport = new JButton();
        txtTotalRecord = new JTextField();
        jLabel1 = new JLabel();
        pnlMain = new JPanel();

        setPreferredSize(new Dimension(1075, 768));
        setLayout(new BorderLayout());

        jPanel1.setBackground(new Color(225, 225, 225));
        jPanel1.setPreferredSize(new Dimension(370, 700));

        lblTimKiem.setFont(new Font("Segoe UI", 1, 18)); // NOI18N
        lblTimKiem.setForeground(new Color(51, 51, 255));
        lblTimKiem.setText("Tìm kiếm");

        jLabel4.setIcon(new FlatSVGIcon("icon/svg/filter-1.svg", 20, 20)); // NOI18N
        jLabel4.setPreferredSize(new Dimension(35, 35));

        jLabel5.setIcon(new FlatSVGIcon("icon/svg/filter-1.svg", 20, 20)); // NOI18N
        jLabel5.setPreferredSize(new Dimension(35, 35));

        lblTrangThai.setFont(new Font("Segoe UI", 1, 18)); // NOI18N
        lblTrangThai.setForeground(new Color(51, 51, 255));
        lblTrangThai.setText("Lọc theo trạng thái");

        ButtonGroup group1 = new ButtonGroup();
        group1.add(radStatusAll);
        group1.add(radDangLuuHanh);
        group1.add(radDaBanHet);
        group1.add(radDaHuy);
        group1.add(radDaHetHan);

        jLabel6.setIcon(new FlatSVGIcon("icon/svg/filter-1.svg", 20, 20)); // NOI18N
        jLabel6.setPreferredSize(new Dimension(35, 35));

        lblThoiGian.setFont(new Font("Segoe UI", 1, 18)); // NOI18N
        lblThoiGian.setForeground(new Color(51, 51, 255));

        lblNgayNhap.setFont(new Font("Segoe UI", 1, 12)); // NOI18N

        ButtonGroup group2 = new ButtonGroup();
        group2.add(radTimeAll);
        group2.add(radToday);
        group2.add(radMonth);
        group2.add(radWeek);
        group2.add(radDiff);
        group2.add(radHetHan30Ngay);
        group2.add(radHetHan7Ngay);
        group2.add(radHetHan3Thang);

        radTimeAll.setSelected(true);
        radStatusAll.setSelected(true);

        jLabel2.setFont(new Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setText("...");

        lblHanSuDung.setFont(new Font("Segoe UI", 1, 12)); // NOI18N

        txtTimeStart.setEditable(false);
        txtTimeEnd.setEditable(false);

        dateFrom = new JDateChooser();
        dateTo = new JDateChooser();

        dateFrom.setDateFormatString("dd/MM/yyyy");
        dateTo.setDateFormatString("dd/MM/yyyy");

        dateFrom.setBounds(0, 0, 150, 30);
        dateTo.setBounds(0, 0, 150, 30);

        txtTimeStart.add(dateFrom);
        txtTimeEnd.add(dateTo);

        dateFrom.setVisible(false);
        dateTo.setVisible(false);

        txtTimeStart.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_ICON,
                new FlatSVGIcon("icon/svg/calendar.svg", 0.4f));

        txtTimeEnd.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_ICON,
                new FlatSVGIcon("icon/svg/calendar.svg", 0.4f));

        txtSearchSoLo.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_ICON,
                new FlatSVGIcon("icon/svg/search.svg", 0.4f));

        txtSearchMaSP.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_ICON,
                new FlatSVGIcon("icon/svg/search.svg", 0.4f));

        cmbOption.setModel(new DefaultComboBoxModel<>(
                new String[]{"Sắp xếp theo ngày nhập", "Sắp xếp theo hạn sử dụng"}));

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup().addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(txtTimeStart, GroupLayout.PREFERRED_SIZE, 156,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18).addComponent(jLabel2)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
                                                GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(
                                                txtTimeEnd, GroupLayout.PREFERRED_SIZE, 150,
                                                GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createSequentialGroup().addGroup(jPanel1Layout
                                                .createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                .addComponent(txtSearchSoLo)
                                                .addGroup(jPanel1Layout.createSequentialGroup()
                                                        .addComponent(jLabel5, GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(lblTimKiem, GroupLayout.PREFERRED_SIZE, 149,
                                                                GroupLayout.PREFERRED_SIZE))
                                                .addComponent(txtSearchMaSP)
                                                .addGroup(jPanel1Layout.createSequentialGroup()
                                                        .addComponent(jLabel4, GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(lblTrangThai, GroupLayout.PREFERRED_SIZE, 175,
                                                                GroupLayout.PREFERRED_SIZE))
                                                .addComponent(radDangLuuHanh).addComponent(radDaBanHet).addComponent(radDaHuy)
                                                .addGroup(jPanel1Layout.createSequentialGroup()
                                                        .addComponent(jLabel6, GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(lblThoiGian, GroupLayout.PREFERRED_SIZE, 175,
                                                                GroupLayout.PREFERRED_SIZE))
                                                .addComponent(lblNgayNhap, GroupLayout.PREFERRED_SIZE, 106,
                                                        GroupLayout.PREFERRED_SIZE)
                                                .addComponent(radDiff)
                                                .addComponent(
                                                        lblHanSuDung, GroupLayout.PREFERRED_SIZE, 106,
                                                        GroupLayout.PREFERRED_SIZE)
                                                .addComponent(radHetHan7Ngay).addComponent(radHetHan3Thang)
                                                .addComponent(radHetHan30Ngay)
                                                .addComponent(cmbOption, 0, GroupLayout.DEFAULT_SIZE,
                                                        Short.MAX_VALUE)
                                                .addGroup(GroupLayout.Alignment.TRAILING,
                                                        jPanel1Layout.createSequentialGroup().addGroup(jPanel1Layout
                                                                        .createParallelGroup(GroupLayout.Alignment.TRAILING,
                                                                                false)
                                                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                                                .addComponent(radStatusAll)
                                                                                .addPreferredGap(
                                                                                        LayoutStyle.ComponentPlacement.RELATED,
                                                                                        GroupLayout.DEFAULT_SIZE,
                                                                                        Short.MAX_VALUE)
                                                                                .addComponent(radDaHetHan))
                                                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                                                .addGroup(jPanel1Layout.createParallelGroup(
                                                                                                GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(radTimeAll).addComponent(radWeek))
                                                                                .addGap(111, 111, 111)
                                                                                .addGroup(jPanel1Layout.createParallelGroup(
                                                                                                GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(radMonth)
                                                                                        .addComponent(radToday))))
                                                                .addGap(73, 73, 73)))
                                        .addGap(0, 8, Short.MAX_VALUE)))
                        .addContainerGap()));
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(lblTimKiem, GroupLayout.PREFERRED_SIZE, 31,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel5, GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbOption, GroupLayout.PREFERRED_SIZE, 35,
                                GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(txtSearchSoLo, GroupLayout.PREFERRED_SIZE, 35,
                                GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtSearchMaSP, GroupLayout.PREFERRED_SIZE, 35,
                                GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel4, GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblTrangThai, GroupLayout.PREFERRED_SIZE, 31,
                                        GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(radStatusAll).addComponent(radDaHetHan))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(radDangLuuHanh).addGap(18, 18, 18).addComponent(radDaBanHet).addGap(18, 18, 18)
                        .addComponent(radDaHuy).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel6, GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblThoiGian, GroupLayout.PREFERRED_SIZE, 31,
                                        GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblNgayNhap, GroupLayout.PREFERRED_SIZE, 24,
                                GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(radTimeAll).addComponent(radToday))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(radWeek).addComponent(radMonth))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(radDiff)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(txtTimeStart, GroupLayout.PREFERRED_SIZE, 30,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel2).addComponent(txtTimeEnd, GroupLayout.PREFERRED_SIZE,
                                        30, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblHanSuDung, GroupLayout.PREFERRED_SIZE, 24,
                                GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radHetHan7Ngay)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(radHetHan30Ngay)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(radHetHan3Thang)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        add(jPanel1, BorderLayout.LINE_END);

        jPanel2.setBackground(new Color(204, 204, 255));
        jPanel2.setLayout(new BorderLayout());

        jPanel3.setBackground(new Color(255, 255, 255));
        jPanel3.setPreferredSize(new Dimension(196, 50));

        lblQuanLyLoHang.setFont(new Font("Segoe UI", 1, 18)); // NOI18N
        lblQuanLyLoHang.setForeground(new Color(51, 51, 255));

        btnExport.setBackground(new Color(0, 0, 255));
        btnExport.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
        btnExport.setForeground(new Color(255, 255, 255));
        btnExport.setText("Export");
        btnExport.setPreferredSize(new Dimension(90, 30));
        btnExport.setIcon(new FlatSVGIcon("icon/svg/export.svg", 0.30f));

        btnAdd.setBackground(new Color(0, 0, 255));
        btnAdd.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
        btnAdd.setForeground(new Color(255, 255, 255));
        btnAdd.setPreferredSize(new Dimension(90, 30));
        btnAdd.setIcon(new FlatSVGIcon("icon/svg/add.svg", 0.30f));

        btnImport.setBackground(new Color(0, 0, 255));
        btnImport.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
        btnImport.setForeground(new Color(255, 255, 255));
        btnImport.setText("Import");
        btnImport.setPreferredSize(new Dimension(90, 30));
        btnImport.setIcon(new FlatSVGIcon("icon/svg/edit.svg", 0.35f));

        jLabel1.setFont(new Font("Segoe UI", 1, 12)); // NOI18N
        jLabel1.setText("Total record");

        txtTotalRecord.setEditable(false);

        GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup().addGap(15, 15, 15)
                        .addComponent(lblQuanLyLoHang, GroupLayout.PREFERRED_SIZE, 250,
                                GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 146, Short.MAX_VALUE)
                        .addComponent(btnAdd, GroupLayout.PREFERRED_SIZE, 150,
                                GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnImport, GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnExport, GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, 78,
                                GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtTotalRecord, GroupLayout.PREFERRED_SIZE, 89,
                                GroupLayout.PREFERRED_SIZE)
                        .addContainerGap()));
        jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup().addContainerGap()
                        .addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(btnAdd, GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnExport, GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnImport, GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblQuanLyLoHang)
                                .addComponent(txtTotalRecord, GroupLayout.PREFERRED_SIZE, 35,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel1))
                        .addContainerGap(9, Short.MAX_VALUE)));

        jPanel2.add(jPanel3, BorderLayout.PAGE_START);

        GroupLayout pnlMainLayout = new GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(pnlMainLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGap(0, 196, Short.MAX_VALUE));
        pnlMainLayout.setVerticalGroup(pnlMainLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGap(0, 664, Short.MAX_VALUE));

        // Tạo phần body quản lý danh sách voucher
        initMainBody();

        jPanel2.add(pnlMain, BorderLayout.CENTER);

        add(jPanel2, BorderLayout.CENTER);
    }

    private JButton btnAdd;
    private JButton btnExport;
    private JButton btnImport;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel4;
    private JLabel jLabel5;
    private JLabel jLabel6;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JPanel jPanel3;
    private JLabel lblHanSuDung;
    private JLabel lblNgayNhap;
    private JLabel lblQuanLyLoHang;
    private JLabel lblThoiGian;
    private JLabel lblTimKiem;
    private JLabel lblTrangThai;
    private JPanel pnlMain;
    private JRadioButton radDaBanHet;
    private JRadioButton radDaHetHan;
    private JRadioButton radDaHuy;
    private JRadioButton radDangLuuHanh;
    private JRadioButton radDiff;
    private JRadioButton radHetHan30Ngay;
    private JRadioButton radHetHan3Thang;
    private JRadioButton radHetHan7Ngay;
    private JRadioButton radMonth;
    private JRadioButton radStatusAll;
    private JRadioButton radTimeAll;
    private JRadioButton radToday;
    private JRadioButton radWeek;
    private JTextField txtSearchMaSP;
    private JTextField txtSearchSoLo;
    private JTextField txtTimeEnd;
    private JTextField txtTimeStart;
    private JTextField txtTotalRecord;
    private JComboBox<String> cmbOption;
}

