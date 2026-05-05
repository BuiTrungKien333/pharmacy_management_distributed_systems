package com.pharmacy.view.invoice;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.pharmacy.shared.dto.response.InvoiceDetailResponse;
import com.pharmacy.shared.dto.response.InvoiceRefundResponse;
import com.pharmacy.shared.dto.response.InvoiceResponse;
import com.pharmacy.shared.service.InvoiceService;
import com.pharmacy.shared.util.Pagination;
import com.pharmacy.util.ClientContext;
import com.pharmacy.util.ClientSecurityContext;
import com.pharmacy.util.FormatUtil;
import com.toedter.calendar.JDateChooser;
import raven.modal.Toast;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class InvoiceUI extends javax.swing.JPanel {

    private static final int pageSize = 50;

    private Pagination pagination;

    private static final String activePageButtonStyle = "" + "background:$primary;" + "foreground:$white;" + "arc:999;"
            + "margin:5,5,5,5;" + "hoverBackground:$primary;" + "hoverForeground:$white;" + "font:$h6.font;";

    private static final String normalPageButtonStyle = "" + "background:$white;" + "foreground:$black;" + "arc:999;"
            + "margin:5,5,5,5;" + "borderColor:$gray;" + "hoverBackground:#E0E0E0;" + "hoverForeground:$black;"
            + "hoverBorderColor:#007BFF;" + "font:$h6.font;";

    private boolean useSearchByMaHD = true;

    private final InvoiceService invoiceService;

    private JDateChooser dateFrom;

    private JDateChooser dateTo;

    private int type = 0; // 0: hóa đơn bán, 1: hoá đơn trả

    private int filter = 0; // 0: Tất cả, 1: Đã duyệt, 2: Chờ xử lý

    private int filterDate = 0; // 0: Tất cả, 1: Hôm nay, 2: 7 ngày trước, 3: Tháng này, 4: Từ ngày đến ngày

    private ApproveInvRefundDialog approveInvRefundDialog;

    private ViewInvoiceDialog viewInvoiceDialog;

    public InvoiceUI() {

        this.invoiceService = ClientContext.getService(InvoiceService.class);

        initComponents();

        applyPermissions();

        decorateTable(tblHoaDon);

        decorateColumnTrangThai();

        initEvent();

        loadInitData();
    }

    private void applyPermissions() {
        btnExport.setEnabled(ClientSecurityContext.hasPermission("INVOICE_EXPORT"));
    }

    private void loadInitData() {
        updateStatusComboBox();

        applyFilters();
    }

    private void updateStatusComboBox() {
        type = comboLoaiHD.getSelectedIndex();
        if (type == 0) {
            comboTrangThai.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Đã bán" }));
            comboTrangThai.setEnabled(false);
            filter = 0;
        } else {
            comboTrangThai.setModel(
                    new javax.swing.DefaultComboBoxModel<>(new String[] { "Tất cả", "Đã duyệt", "Chờ xử lý" }));
            comboTrangThai.setEnabled(true);
            filter = 0;
        }
    }

    private void initEvent() {

        comboLoaiHD.addActionListener(e -> {
            updateStatusComboBox();
            applyFilters();
        });

        comboTrangThai.addActionListener(e -> {
            filter = comboTrangThai.getSelectedIndex();
            applyFilters();
        });

        comboKyXuat.addActionListener(e -> filterInvoiceByDate());

        txtTimeStart.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (filterDate != 4)
                    return;

                dateFrom.setVisible(true);
                dateFrom.getCalendarButton().doClick();
            }
        });

        txtTimeEnd.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (filterDate != 4)
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

        btnXemChiTiet.addActionListener(e -> viewInvoice());

//        btnExport.addActionListener(e -> exportInvoiceToExcel());

        txtMaHD.addActionListener(e -> {
            useSearchByMaHD = true;
            txtSoLo.setText("");
            applyFilters();
        });

        txtSoLo.addActionListener(e -> {
            useSearchByMaHD = false;
            txtMaHD.setText("");
            applyFilters();
        });

        btnRefesh.addActionListener(e -> refreshData());
    }

    /*
    private void exportInvoiceToExcel() {
        LocalDate startDate = invoiceService.convertStringToLocalDate(filterDate, txtTimeStart.getText());
        LocalDate endDate = invoiceService.convertStringToLocalDate(filterDate, txtTimeEnd.getText());

        if (filterDate == 4) {
            if (startDate == null || endDate == null)
                return;

            if (startDate.isAfter(endDate)) {
                Toast.show(this, Toast.Type.ERROR, "Ngày bắt đầu không được lớn hơn ngày kết thúc!");
                return;
            }
        }

        String keyword = "";
        boolean isSearching = false;

        if (useSearchByMaHD && !txtMaHD.getText().trim().isEmpty()) {
            keyword = txtMaHD.getText().trim();
            isSearching = true;
        } else if (!useSearchByMaHD && !txtSoLo.getText().trim().isEmpty()) {
            keyword = txtSoLo.getText().trim();
            isSearching = true;
        }

        List<?> rawList;

        if (isSearching) {
            if (useSearchByMaHD)
                rawList = invoiceService.getAllInvoiceToExportAndSearchById(type, filter, filterDate, startDate, endDate,
                        keyword);
            else
                rawList = invoiceBUS.getAllInvoiceToExportAndSearchBySoLo(type, filter, filterDate, startDate, endDate,
                        keyword);
        } else {
            rawList = invoiceBUS.getAllInvoiceToExport(type, filter, filterDate, startDate, endDate);
        }

        List<Object[]> data = new ArrayList<>();

        String[] headers;
        if (type == 0) {
            headers = new String[] { "Mã hóa đơn", "Ngày Lập", "Khách hàng", "Nhân viên bán", "Voucher sử dụng",
                    "Tổng tiền thanh toán", "Trạng thái trả" };

            List<Invoice> salesList = (List<Invoice>) rawList;
            for (Invoice i : salesList) {
                data.add(i.getObjectsToExcel());
            }

        } else {
            headers = new String[] { "Mã hóa đơn trả", "Ngày Lập", "Khách hàng", "Hóa đơn bán", "Nhân viên trả",
                    "Tiền hoàn", "Lý do", "Trạng thái duyệt" };

            List<InvoiceReturn> returnList = (List<InvoiceReturn>) rawList;
            for (InvoiceReturn ir : returnList) {
                data.add(ir.getObjectsToExcel());
            }
        }

        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

        FileDialog fileDialog = new FileDialog(parentFrame, "Xuất danh sách hóa đơn ra Excel", FileDialog.SAVE);

        fileDialog.setFile("Danh_Sach_Hoa_Don.xlsx");

        fileDialog.setFilenameFilter((dir, name) -> name.endsWith(".xlsx"));

        fileDialog.setVisible(true);

        String directory = fileDialog.getDirectory();
        String filename = fileDialog.getFile();

        if (filename != null && directory != null) {
            String filePath = directory + filename;

            if (!filePath.toLowerCase().endsWith(".xlsx"))
                filePath += ".xlsx";

            try {
                ExcelExporterUtil.exportDataToExcel(filePath, "DanhSachHoaDon", headers, data);
                JOptionPane.showMessageDialog(this, "Xuất file thành công!");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi xuất file!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
     */

    public void refreshData() {
        txtMaHD.setText("");
        txtSoLo.setText("");
        comboKyXuat.setSelectedIndex(0);
        comboLoaiHD.setSelectedIndex(0);
        comboTrangThai.setSelectedIndex(0);
        updateStatusComboBox();
        type = 0;
        filter = 0;
        filterDate = 0;
        useSearchByMaHD = true;

        applyFilters();
    }

    private void filterInvoiceByDate() {
        filterDate = comboKyXuat.getSelectedIndex();

        boolean enable = filterDate == 4;

        txtTimeStart.setEnabled(enable);
        txtTimeEnd.setEnabled(enable);

        if (!enable) {
            txtTimeStart.setText("");
            txtTimeEnd.setText("");
            dateFrom.setVisible(false);
            dateTo.setVisible(false);

            applyFilters();
        }
    }

    private void applyFilters() {
        LocalDate startDate = invoiceService.convertStringToLocalDate(filterDate, txtTimeStart.getText());

        LocalDate endDate = invoiceService.convertStringToLocalDate(filterDate, txtTimeEnd.getText());

        if (filterDate == 4) {
            if (startDate == null || endDate == null)
                return;

            if (startDate.isAfter(endDate)) {
                Toast.show(this, Toast.Type.ERROR, "Ngày bắt đầu không được lớn hơn ngày kết thúc!");
                return;
            }
        }

        String keyword = "";
        boolean isSearching = false;

        if (useSearchByMaHD && !txtMaHD.getText().trim().isEmpty()) {
            keyword = txtMaHD.getText().trim();
            isSearching = true;
        } else if (!useSearchByMaHD && !txtSoLo.getText().trim().isEmpty()) {
            keyword = txtSoLo.getText().trim();
            isSearching = true;
        }

        if (pagination == null)
            pagination = new Pagination(1, pageSize, 0);
        pagination.setPageNumber(1);

        fetchAndDisplayData(startDate, endDate, keyword, isSearching);
    }

    private void fetchAndDisplayData(LocalDate startDate, LocalDate endDate, String keyword, boolean isSearching) {
        int totalRecord;
        List<?> rawList;

        if (isSearching) {
            if (useSearchByMaHD)
                rawList = invoiceService.getListInvoiceFilteredAndSearchById(type, filter, filterDate, startDate, endDate,pagination, keyword);
            else
                rawList = invoiceService.getAllInvoiceFilteredAndSearchByBatchNumber(type, filter, filterDate, startDate, endDate, pagination, keyword);
        } else {
            rawList = invoiceService.getListInvoiceFiltered(type, filter, filterDate, startDate, endDate, pagination);
        }

        totalRecord = invoiceService.getTotalRecordFiltered();
        txtTotalRecord.setText(String.valueOf(totalRecord));

        int currentPage = pagination.getPageNumber();
        pagination = new Pagination(currentPage, pageSize, totalRecord);

        calculateTotalRevenue(rawList);

        initPage(rawList);
    }

    private void calculateTotalRevenue(List<?> rawList) {
        double total = 0;
        if (type == 0) {
            total = invoiceService.calculateTotalRevenue((List<InvoiceResponse>) rawList);
            lblTotalRevenue.setText("Total Revenue: ");
        } else {
            total = invoiceService.calculateTotalRefund((List<InvoiceRefundResponse>) rawList);
            lblTotalRevenue.setText("Total Refund: ");
        }
        txtRevenue.setText(FormatUtil.formatVND(total) + " VND");
    }

    private <T> void showTable(List<T> list) {
        DefaultTableModel model = (DefaultTableModel) tblHoaDon.getModel();
        model.setRowCount(0);

        for (T item : list) {
            if (item instanceof InvoiceResponse invoice) {
                String phone = invoice.getCustomer() != null
                        ? invoice.getCustomer().getPhoneNumber()
                        : "Khách vảng lai";
                model.addRow(new Object[] {
                        invoice.getInvoiceCode(),
                        invoice.getCreatedDate(),
                        phone,
                        "Hóa đơn bán",
                        "Đã bán",
                        FormatUtil.formatVND(invoice.getTotalPayableAmount()) + " VND"
                });
            } else if (item instanceof InvoiceRefundResponse invoiceRefund) {
                model.addRow(new Object[] {
                        invoiceRefund.getReturnInvoiceCode(),
                        invoiceRefund.getCreatedDate(),
                        invoiceRefund.getCustomer().getPhoneNumber(),
                        "Hóa đơn trả",
                        invoiceRefund.isApproved() ? "Đã duyệt" : "Chờ xử lý",
                        FormatUtil.formatVND(invoiceRefund.getRefundAmount()) + " VND"
                });
            }
        }
    }

    public void showPage(int pageNum) {
        if (pageNum == 0 || pageNum > pagination.getTotalPages())
            return;

        pagination.setPageNumber(pageNum);

        LocalDate startDate = invoiceService.convertStringToLocalDate(filterDate, txtTimeStart.getText());
        LocalDate endDate = invoiceService.convertStringToLocalDate(filterDate, txtTimeEnd.getText());

        if (filterDate == 4) {
            if (startDate == null || endDate == null)
                return;

            if (startDate.isAfter(endDate)) {
                Toast.show(this, Toast.Type.ERROR, "Ngày bắt đầu không được lớn hơn ngày kết thúc!");
                return;
            }
        }

        String keyword = "";
        boolean isSearching = false;

        if (useSearchByMaHD && !txtMaHD.getText().isEmpty()) {
            keyword = txtMaHD.getText();
            isSearching = true;
        } else if (!useSearchByMaHD && !txtSoLo.getText().isEmpty()) {
            keyword = txtSoLo.getText();
            isSearching = true;
        }

        List<?> rawList;
        if (isSearching) {
            if (useSearchByMaHD)
                rawList = invoiceService.getListInvoiceFilteredAndSearchById(type, filter, filterDate, startDate, endDate,pagination, keyword);
            else
                rawList = invoiceService.getAllInvoiceFilteredAndSearchByBatchNumber(type, filter, filterDate, startDate, endDate, pagination, keyword);
        } else {
            rawList = invoiceService.getListInvoiceFiltered(type, filter, filterDate, startDate, endDate, pagination);
        }

        updatePageButtons(pageNum);

        if (type == 0)
            showTable((List<InvoiceResponse>) rawList);
        else
            showTable((List<InvoiceRefundResponse>) rawList);
    }

    private void initPage(List<?> list) {
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

        if (type == 0)
            showTable((List<InvoiceResponse>) list);
        else
            showTable((List<InvoiceRefundResponse>) list);
    }

    private void viewInvoice() {
        int i = tblHoaDon.getSelectedRow();
        if (i < 0)
            return;

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

        String maHD = tblHoaDon.getValueAt(i, 0).toString();

        if (type == 0) {
            InvoiceResponse invoice = invoiceService.getInvoiceById(maHD);
            List<InvoiceDetailResponse> list = invoiceService.getAllInvoiceDetailByInvCode(maHD);

            if (viewInvoiceDialog == null)
                viewInvoiceDialog = new ViewInvoiceDialog(frame, true);

            viewInvoiceDialog.setInitData(invoice, list);
            viewInvoiceDialog.setLocationRelativeTo(frame);
            viewInvoiceDialog.setVisible(true);
        } else {
            InvoiceRefundResponse invoiceRefundResponse = invoiceService.getInvoiceRefundById(maHD);
            List<InvoiceDetailResponse> list = invoiceService.getAllInvoiceDetailByInvoiceRefundCode(maHD);

            if (approveInvRefundDialog == null)
                approveInvRefundDialog = new ApproveInvRefundDialog(frame, true);

            approveInvRefundDialog.setInitData(invoiceRefundResponse, list);
            approveInvRefundDialog.setLocationRelativeTo(frame);
            approveInvRefundDialog.setVisible(true);
        }

        glass.setVisible(false);
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

    private void decorateColumnTrangThai() {
        tblHoaDon.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {

                JPanel panel = new JPanel(new GridBagLayout());
                panel.setOpaque(true);
                panel.setBackground(new Color(250, 250, 250));

                JButton label = new JButton(value == null ? "" : value.toString()) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        int width = getWidth();
                        int height = getHeight();

                        g2.setColor(getBackground());
                        g2.fillRoundRect(0, 0, width, height, height, height);

                        super.paintComponent(g2);
                        g2.dispose();
                    }
                };

                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                label.setFocusPainted(false);
                label.setContentAreaFilled(false);
                label.setBorderPainted(false);
                label.setOpaque(false);
                label.setForeground(Color.BLACK);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setPreferredSize(new Dimension(150, 25));

                String trangThai = value == null ? "" : value.toString();
                switch (trangThai) {
                    case "Đã bán":
                        label.setBackground(new Color(204, 255, 204));
                        label.setForeground(new Color(0, 100, 0));
                        break;
                    case "Đã duyệt":
                        label.setBackground(new Color(204, 255, 204));
                        label.setForeground(new Color(0, 100, 0));
                        break;
                    case "Từ chối":
                        label.setBackground(new Color(248, 215, 218));
                        label.setForeground(new Color(120, 0, 0));
                        break;
                    case "Chờ xử lý":
                        label.setBackground(new Color(255, 243, 205));
                        label.setForeground(new Color(120, 90, 0));
                        break;
                    default:
                        label.setBackground(Color.WHITE);
                        label.setForeground(Color.BLACK);
                        break;
                }

                if (isSelected)
                    panel.setBackground(new Color(220, 238, 255));

                panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(220, 220, 220)));

                panel.add(label);
                return panel;
            }
        });
    }

    private void decorateTable(JTable tblHoaDon) {

        tblHoaDon.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblHoaDon.setRowHeight(45);
        tblHoaDon.setGridColor(new Color(220, 220, 220));

        tblHoaDon.setSelectionBackground(new Color(220, 238, 255));
        tblHoaDon.setSelectionForeground(Color.BLACK);

        JTableHeader header = tblHoaDon.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setBackground(new Color(165, 218, 201));
        header.setForeground(Color.BLACK);
        header.setPreferredSize(new Dimension(50, 40));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tblHoaDon.setShowHorizontalLines(true);
        tblHoaDon.setShowVerticalLines(true);
        for (int i = 0; i < tblHoaDon.getColumnCount(); i++) {
            tblHoaDon.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    private void initComponents() {
        JpFirst = new javax.swing.JPanel();
        lblTieuDe = new javax.swing.JLabel();
        txtMaHD = new javax.swing.JTextField();
        txtSoLo = new javax.swing.JTextField();
        btnXemChiTiet = new javax.swing.JButton();
        btnRefesh = new javax.swing.JButton();
        JpCenter = new javax.swing.JPanel();
        lblLoaiHD = new javax.swing.JLabel();
        comboLoaiHD = new javax.swing.JComboBox<>();
        lblTuNgay = new javax.swing.JLabel();
        txtTimeStart = new javax.swing.JTextField();
        txtTimeEnd = new javax.swing.JTextField();
        lblDenNgay = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        lblKyXuatHD = new javax.swing.JLabel();
        comboKyXuat = new javax.swing.JComboBox<>();
        lblTrangThai = new javax.swing.JLabel();
        comboTrangThai = new javax.swing.JComboBox<>();
        btnExport = new javax.swing.JButton();
        JpLast = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblHoaDon = new javax.swing.JTable();
        lblTotal = new javax.swing.JLabel();
        txtTotalRecord = new javax.swing.JTextField();
        pnlPage = new javax.swing.JPanel();
        txtRevenue = new javax.swing.JTextField();
        lblTotalRevenue = new javax.swing.JLabel();
        txtRevenue.setEditable(false);
        txtTotalRecord.setEditable(false);

        txtMaHD.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập số hóa đơn hoặc số điện thoại cần tìm");

        txtSoLo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập số lô để tìm hóa đơn");

        setBackground(new java.awt.Color(235, 235, 235));
        setToolTipText("");

        JpFirst.setBackground(new java.awt.Color(255, 255, 255));

        lblTieuDe.setBackground(new java.awt.Color(66, 100, 145));
        lblTieuDe.setFont(new java.awt.Font("Segoe UI", 1, 15));
        lblTieuDe.setForeground(new java.awt.Color(66, 100, 145));
        lblTieuDe.setText("QUẢN LÝ HÓA ĐƠN");

        lblTotal.setFont(new java.awt.Font("Segoe UI", 1, 13));
        lblTotal.setText("Total Record: ");

        lblTotalRevenue.setFont(new java.awt.Font("Segoe UI", 1, 13));
        lblTotalRevenue.setText("Total Revenue: ");

        btnXemChiTiet.setBackground(new java.awt.Color(215, 215, 215));
        btnXemChiTiet.setFont(new java.awt.Font("Segoe UI", 1, 12));
        btnXemChiTiet.setText("Xem chi tiết");

        btnRefesh.setBackground(new java.awt.Color(0, 51, 255));
        btnRefesh.setFont(new java.awt.Font("Segoe UI", 1, 12));
        btnRefesh.setForeground(new java.awt.Color(255, 255, 255));
        btnRefesh.setText("Refesh");

        javax.swing.GroupLayout JpFirstLayout = new javax.swing.GroupLayout(JpFirst);
        JpFirst.setLayout(JpFirstLayout);
        JpFirstLayout.setHorizontalGroup(JpFirstLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(JpFirstLayout.createSequentialGroup().addGap(17, 17, 17)
                        .addGroup(JpFirstLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(JpFirstLayout.createSequentialGroup().addComponent(lblTieuDe)
                                        .addContainerGap(1094, Short.MAX_VALUE))
                                .addGroup(JpFirstLayout.createSequentialGroup()
                                        .addComponent(txtMaHD, javax.swing.GroupLayout.PREFERRED_SIZE, 422,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(62, 62, 62)
                                        .addComponent(txtSoLo, javax.swing.GroupLayout.PREFERRED_SIZE, 409,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(39, 39, 39)
                                        .addComponent(btnXemChiTiet, javax.swing.GroupLayout.PREFERRED_SIZE, 112,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btnRefesh, javax.swing.GroupLayout.PREFERRED_SIZE, 104,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(37, 37, 37)))));
        JpFirstLayout.setVerticalGroup(JpFirstLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(JpFirstLayout.createSequentialGroup().addContainerGap().addComponent(lblTieuDe)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(JpFirstLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtSoLo, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                                .addComponent(txtMaHD, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                                .addComponent(btnRefesh, javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnXemChiTiet, javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(12, 12, 12)));

        JpCenter.setBackground(new java.awt.Color(255, 255, 255));

        lblLoaiHD.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblLoaiHD.setText("Loại hóa đơn");

        comboLoaiHD.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Hóa đơn bán", "Hóa đơn trả" }));

        lblTuNgay.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblTuNgay.setText("Từ ngày");

        txtTimeStart.setEditable(false);
        txtTimeEnd.setEditable(false);

        dateFrom = new JDateChooser();
        dateTo = new JDateChooser();

        dateFrom.setDateFormatString("dd/MM/yyyy");
        dateTo.setDateFormatString("dd/MM/yyyy");

        dateFrom.setBounds(0, 0, 310, 35);
        dateTo.setBounds(0, 0, 310, 35);

        txtTimeStart.add(dateFrom);
        txtTimeEnd.add(dateTo);

        dateFrom.setVisible(false);
        dateTo.setVisible(false);

        txtTimeStart.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_ICON,
                new FlatSVGIcon("icon/svg/calendar.svg", 0.4f));

        txtTimeEnd.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_ICON,
                new FlatSVGIcon("icon/svg/calendar.svg", 0.4f));

        lblDenNgay.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblDenNgay.setText("Đến ngày");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 18));
        jLabel5.setText("...");

        lblKyXuatHD.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblKyXuatHD.setText("Kỳ xuất hóa đơn");

        comboKyXuat.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[] { "Tất cả", "Hôm nay", "7 ngày trước", "Tháng này", "Lựa chọn khác" }));

        lblTrangThai.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblTrangThai.setText("Trạng thái");

        comboTrangThai.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[] { "Tất cả", "Chờ xử lý", "Đã bán", "Đã duyệt",
                        "Từ chối" }));

        btnExport.setBackground(new java.awt.Color(0, 51, 255));
        btnExport.setFont(new java.awt.Font("Segoe UI", 1, 12));
        btnExport.setForeground(new java.awt.Color(255, 255, 255));
        btnExport.setText("Export");
        btnExport.setIcon(new FlatSVGIcon("icon/svg/export.svg", 25, 25));

        javax.swing.GroupLayout JpCenterLayout = new javax.swing.GroupLayout(JpCenter);
        JpCenter.setLayout(JpCenterLayout);
        JpCenterLayout.setHorizontalGroup(JpCenterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(JpCenterLayout.createSequentialGroup().addGap(34, 34, 34).addGroup(JpCenterLayout
                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(JpCenterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(comboKyXuat, 0, 232, Short.MAX_VALUE)
                                        .addComponent(comboLoaiHD, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(lblLoaiHD, javax.swing.GroupLayout.Alignment.LEADING))
                                .addComponent(lblKyXuatHD))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(JpCenterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(JpCenterLayout.createSequentialGroup().addGroup(JpCenterLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(JpCenterLayout.createSequentialGroup()
                                                        .addComponent(txtTimeStart, javax.swing.GroupLayout.PREFERRED_SIZE, 308,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(18, 18, 18).addComponent(jLabel5))
                                                .addComponent(lblTuNgay)).addGap(12, 12, 12)
                                        .addGroup(JpCenterLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(txtTimeEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 308,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(lblDenNgay)))
                                .addComponent(lblTrangThai)
                                .addGroup(JpCenterLayout.createSequentialGroup()
                                        .addComponent(comboTrangThai, javax.swing.GroupLayout.PREFERRED_SIZE, 308,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 94,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(40, 40, 40)));
        JpCenterLayout.setVerticalGroup(JpCenterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                        JpCenterLayout.createSequentialGroup().addGap(17, 17, 17)
                                .addGroup(JpCenterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblLoaiHD).addComponent(lblTuNgay).addComponent(lblDenNgay))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(JpCenterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel5)
                                        .addComponent(txtTimeEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 34,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtTimeStart, javax.swing.GroupLayout.PREFERRED_SIZE, 34,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(comboLoaiHD, javax.swing.GroupLayout.PREFERRED_SIZE, 34,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(JpCenterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblKyXuatHD).addComponent(lblTrangThai))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(JpCenterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(comboKyXuat, javax.swing.GroupLayout.PREFERRED_SIZE, 34,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(comboTrangThai, javax.swing.GroupLayout.PREFERRED_SIZE, 34,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(24, Short.MAX_VALUE)));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14));
        jLabel8.setText("Danh sách hóa đơn");

        tblHoaDon.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {}, new String[] { "Mã hóa đơn",
                "Ngày lập", "Số điện thoại", "Loại hóa đơn", "Trạng thái", "Thành tiền" }) {
            private static final long serialVersionUID = 1L;

            Class[] types = new Class[] { java.lang.String.class, java.lang.String.class, java.lang.String.class,
                    java.lang.Object.class, java.lang.String.class, java.lang.String.class };

            boolean[] canEdit = new boolean[] { false, false, false, false, false, false };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        jScrollPane1.setViewportView(tblHoaDon);

        if (tblHoaDon.getColumnModel().getColumnCount() > 0) {
            for (int i = 0; i < 5; i++) {
                tblHoaDon.getColumnModel().getColumn(i).setResizable(false);
            }
        }

        pnlPage.setBackground(new java.awt.Color(255, 255, 255));
        pnlPage.setPreferredSize(new java.awt.Dimension(0, 50));

        javax.swing.GroupLayout pnlPageLayout = new javax.swing.GroupLayout(pnlPage);
        pnlPage.setLayout(pnlPageLayout);
        pnlPageLayout.setHorizontalGroup(pnlPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 0, Short.MAX_VALUE));
        pnlPageLayout.setVerticalGroup(pnlPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 50, Short.MAX_VALUE));

        javax.swing.GroupLayout JpLastLayout = new javax.swing.GroupLayout(JpLast);
        JpLast.setLayout(JpLastLayout);
        JpLastLayout.setHorizontalGroup(JpLastLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(JpLastLayout.createSequentialGroup().addContainerGap()
                        .addGroup(JpLastLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(JpLastLayout.createSequentialGroup().addComponent(jLabel8)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(lblTotalRevenue)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtRevenue, javax.swing.GroupLayout.PREFERRED_SIZE, 130,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(41, 41, 41).addComponent(lblTotal)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtTotalRecord, javax.swing.GroupLayout.PREFERRED_SIZE, 75,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(22, 22, 22))
                                .addGroup(JpLastLayout.createSequentialGroup()
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1272,
                                                Short.MAX_VALUE)
                                        .addContainerGap())
                                .addComponent(pnlPage, javax.swing.GroupLayout.DEFAULT_SIZE, 1278, Short.MAX_VALUE))));

        JpLastLayout.setVerticalGroup(JpLastLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(JpLastLayout.createSequentialGroup().addContainerGap()
                        .addGroup(JpLastLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtTotalRecord, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblTotalRevenue).addComponent(jLabel8)
                                .addComponent(txtRevenue, javax.swing.GroupLayout.DEFAULT_SIZE, 15,
                                        javax.swing.GroupLayout.DEFAULT_SIZE)
                                .addComponent(lblTotal))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 430,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(pnlPage, javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap()));

        pnlPage.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 0));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup().addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(JpFirst, javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(JpCenter, javax.swing.GroupLayout.Alignment.TRAILING,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE)
                                .addComponent(JpLast, javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup().addContainerGap()
                        .addComponent(JpFirst, javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(JpCenter, javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(JpLast, javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(435, Short.MAX_VALUE)));
    }

    private javax.swing.JPanel JpCenter;
    private javax.swing.JPanel JpFirst;
    private javax.swing.JPanel JpLast;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnRefesh;
    private javax.swing.JButton btnXemChiTiet;
    private javax.swing.JComboBox<String> comboKyXuat;
    private javax.swing.JComboBox<String> comboLoaiHD;
    private javax.swing.JComboBox<String> comboTrangThai;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblDenNgay;
    private javax.swing.JLabel lblKyXuatHD;
    private javax.swing.JLabel lblLoaiHD;
    private javax.swing.JLabel lblTieuDe;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JLabel lblTrangThai;
    private javax.swing.JLabel lblTuNgay;
    private javax.swing.JTable tblHoaDon;
    private javax.swing.JTextField txtTimeEnd;
    private javax.swing.JTextField txtMaHD;
    private javax.swing.JTextField txtSoLo;
    private javax.swing.JTextField txtTimeStart;
    private javax.swing.JTextField txtTotalRecord;
    private javax.swing.JPanel pnlPage;
    private javax.swing.JLabel lblTotalRevenue;
    private javax.swing.JTextField txtRevenue;

}

