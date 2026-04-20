package com.pharmacy.view.medicine;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.pharmacy.shared.dto.response.MedicineResponse;
import com.pharmacy.shared.service.MedicineService;
import com.pharmacy.shared.util.Pagination;
import com.pharmacy.shared.util.enums.MedicineType;
import com.pharmacy.util.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MedicineUI extends JPanel {

    private int currentType = 0; // 0: tất cả, 1: thuốc, 2: thực phẩm, 3: dụng cụ

    private int currentFilter = 0; // 0: tất cả, 1-5: theo cmbField

    private MedicineService medicineService;

    private Pagination pagination;

    private static final int pageSize = 20;

    private Timer searchTimer;

    private AddProdDialog addProdDialog;

    private EditProdDialog editProdDialog;

    private static final int WIDTH_CART = 260;
    private static final int HEIGHT_CART = 350;

    private static final int WIDTH_ITEM = 240;
    private static final int HEIGHT_ITEM = 230;

    private static final String activeButtonStyle = "" + "background:$primary;" + "foreground:$white;" + "arc:20;"
            + "margin:5,10,5,10;" + "hoverBackground:$primary;" + "hoverForeground:$white;" + "font:$h5.font";

    private static final String normalButtonStyle = "" + "background:$white;" + "foreground:$black;" + "arc:20;"
            + "margin:5,10,5,10;" + "borderColor:$gray;" + "hoverBackground:#E0E0E0;" + "hoverForeground:$black;"
            + "hoverBorderColor:#007BFF;" + "font:$h5.font;";

    private static final String activePageButtonStyle = "" + "background:$primary;" + "foreground:$white;" + "arc:999;"
            + "margin:5,5,5,5;" + "hoverBackground:$primary;" + "hoverForeground:$white;" + "font:$h6.font;";

    private static final String normalPageButtonStyle = "" + "background:$white;" + "foreground:$black;" + "arc:999;"
            + "margin:5,5,5,5;" + "borderColor:$gray;" + "hoverBackground:#E0E0E0;" + "hoverForeground:$black;"
            + "hoverBorderColor:#007BFF;" + "font:$h6.font;";

    private JButton btnAll;
    private JButton btnTool;
    private JButton btnMedicine;
    private JButton btnFood;
    private JPanel pnlCategory;
    private JPanel pnlProd;
    private JButton btnRefresh;

    private JTextField txtTotalPage;

    private JPanel pnlPage;

    public MedicineUI() {

        medicineService = ClientContext.getService(MedicineService.class);

        initComponents();

        applyPermissions();

        Translator.getInstance().addLanguageChangeListener(locale -> {
            SwingUtilities.invokeLater(this::updateTexts);
        });

        updateTexts();

        initEventForButton();

        initSearchEvent();

        loadRefreshDataPage();

        SwingUtilities.invokeLater(() -> txtSearch.requestFocusInWindow());
    }

    public void applyPermissions() {
        btnAdd.setEnabled(ClientSecurityContext.hasPermission("PRODUCT_ADD"));
        btnExport.setEnabled(ClientSecurityContext.hasPermission("PRODUCT_EXPORT"));
    }

    private void loadRefreshDataPage() {
        int total = medicineService.getTotalMedicine();
        pagination = new Pagination(1, pageSize, total);
        txtTotalPage.setText(String.valueOf(total));

        initPage(medicineService.getAllMedicineByPage(pagination));
    }

    private MedicineType getMedicineType() {
        return switch (currentType) {
            case 0 -> MedicineType.ALL;
            case 1 -> MedicineType.MEDICINE;
            case 2 -> MedicineType.FUNCTIONAL_FOODS;
            case 3 -> MedicineType.MEDICAL_INSTRUMENT;
            default -> throw new IllegalStateException("Unexpected value: " + currentType);
        };
    }

    private void initPage(List<MedicineResponse> list) {
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

        showProduct(list);
    }

    public void showPage(int pageNum) {
        if (pageNum == 0 || pageNum > pagination.getTotalPages())
            pageNum = 1;

        pagination.setPageNumber(pageNum);
        updatePageButtons(pageNum);
        showProduct(medicineService.getMedicineFilteredByTypeAndDeletedAndTotalQty(pagination, getMedicineType(), currentFilter));
    }

    public Pagination getCurrentPage() {
        return pagination;
    }

    public void showFirstPage() {
        showPage(1);
    }

    public void showLastPage() {
        showPage(pagination.getTotalPages());
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

    private void initSearchEvent() {
        // search by barcode
        txtSearch.addActionListener(e -> {
            if (radBarcode.isSelected()) {
                String barcode = txtSearch.getText().trim();
                if (barcode.isEmpty())
                    return;

                try {
                    MedicineResponse medicineResponse = medicineService.getMedicineByBarcode(barcode);
                    showProduct(Arrays.asList(medicineResponse));

                } catch (RuntimeException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage());
                } finally {
                    SwingUtilities.invokeLater(() -> {
                        txtSearch.setText("");
                        txtSearch.requestFocusInWindow();
                    });
                }
            }
        });

        searchTimer = new Timer(100, e -> {
            performSearch();
        });
        searchTimer.setRepeats(false);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {

            private void handleSearch() {
                searchTimer.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                handleSearch();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                handleSearch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
    }

    private void performSearch() {
        if (radProdName.isSelected()) {
            String keyword = txtSearch.getText();
            if (keyword.isEmpty()) {
                showProduct(medicineService.getMedicineFilteredByTypeAndDeletedAndTotalQty(pagination, getMedicineType(), currentFilter));
            } else {
                showProduct(medicineService.getMedicineFilteredAndSearchByMedicineName(getMedicineType(), currentFilter, keyword));
            }
        }
    }

    private void initEventForButton() {

        btnAdd.addActionListener(e -> addProd());

        btnExport.addActionListener(e -> exportExcelProd());

        btnRefresh.addActionListener(e -> refreshDataProd());

        btnAll.addActionListener(e -> {
            currentType = 0;
            applyFilters();
            setActiveBtn(btnAll);
        });

        btnMedicine.addActionListener(e -> {
            currentType = 1;
            applyFilters();
            setActiveBtn(btnMedicine);
        });

        btnFood.addActionListener(e -> {
            currentType = 2;
            applyFilters();
            setActiveBtn(btnFood);
        });

        btnTool.addActionListener(e -> {
            currentType = 3;
            applyFilters();
            setActiveBtn(btnTool);
        });

        cmbField.addActionListener(e -> {
            currentFilter = cmbField.getSelectedIndex();
            applyFilters();
        });

        radBarcode.addActionListener(e -> {
            txtSearch.setText("");
            SwingUtilities.invokeLater(() -> txtSearch.requestFocusInWindow());
        });

        radProdName.addActionListener(e -> {
            txtSearch.setText("");
            SwingUtilities.invokeLater(() -> txtSearch.requestFocusInWindow());
        });

    }

    private void applyFilters() {
        int totalRecords = 0;
        List<MedicineResponse> list;
        if (currentType == 0 && currentFilter == 0) {
            list = medicineService.getAllMedicineByPage(pagination);
            totalRecords = medicineService.getTotalMedicine();
        } else {
            list = medicineService.getMedicineFilteredByTypeAndDeletedAndTotalQty(pagination, getMedicineType(), currentFilter);
            totalRecords = medicineService.getTotalMedicineFiltered(getMedicineType(), currentFilter);
        }

        txtTotalPage.setText(String.valueOf(totalRecords));

        pagination = new Pagination(1, pageSize, totalRecords);

        initPage(list);
    }

    public void showProduct(List<MedicineResponse> list) {
        pnlProd.setVisible(false);
        pnlProd.removeAll();

        list.forEach(item -> {
            pnlProd.add(initCardItem(item, WIDTH_CART, HEIGHT_CART));
        });

        pnlProd.revalidate();
        pnlProd.repaint();
        pnlProd.setVisible(true);
    }

    private void setActiveBtn(JButton activeBtn) {
        btnAll.putClientProperty(FlatClientProperties.STYLE, normalButtonStyle);
        btnFood.putClientProperty(FlatClientProperties.STYLE, normalButtonStyle);
        btnMedicine.putClientProperty(FlatClientProperties.STYLE, normalButtonStyle);
        btnTool.putClientProperty(FlatClientProperties.STYLE, normalButtonStyle);

        activeBtn.putClientProperty(FlatClientProperties.STYLE, activeButtonStyle);
    }

    public void exportExcelProd() {
        List<MedicineResponse> list = medicineService.getAllMedicineToExportCSV(getMedicineType(), currentFilter);
        String[] headers = {"Mã SP", "Tên SP", "Barcode", "Tổng số lượng",
                "DVT", "Loại SP", "Trạng thái"};

        List<Object[]> data = new ArrayList<>();
        for (MedicineResponse p : list) {
            data.add(p.getObjects());
        }

        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

        FileDialog fileDialog = new FileDialog(parentFrame, "Xuất danh sách sản phẩm ra Excel", FileDialog.SAVE);

        fileDialog.setFile("Danh_Sach_San_Pham.xlsx");

        fileDialog.setFilenameFilter((dir, name) -> name.endsWith(".xlsx"));

        fileDialog.setVisible(true);

        String directory = fileDialog.getDirectory();
        String filename = fileDialog.getFile();

        if (filename != null && directory != null) {
            String filePath = directory + filename;

            if (!filePath.toLowerCase().endsWith(".xlsx"))
                filePath += ".xlsx";

            try {
                ExcelExporterUtil.exportDataToExcel(filePath, "DanhSachSanPham", headers, data);
                JOptionPane.showMessageDialog(this, "Xuất file thành công!");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi xuất file!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshDataProd() {
        txtSearch.setText("");
        SwingUtilities.invokeLater(() -> txtSearch.requestFocusInWindow());
        cmbField.setSelectedIndex(0);
        currentType = 0;
        currentFilter = 0;
        radBarcode.setSelected(true);

        loadRefreshDataPage();
        setActiveBtn(btnAll);
    }

    private void addProd() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);

        JPanel glass = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 120));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        glass.setOpaque(false);

        frame.setGlassPane(glass);
        glass.setVisible(true);

        if (addProdDialog == null)
            addProdDialog = new AddProdDialog(frame, true, this);
        else
            addProdDialog.clearData();

        addProdDialog.setLocationRelativeTo(frame);
        addProdDialog.setVisible(true);

        glass.setVisible(false);
    }

    private void editDataForProd(String barcode) {
        MedicineResponse medicineResponse = medicineService.getMedicineByBarcode(barcode);

        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);

        JPanel glass = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 120));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        glass.setOpaque(false);

        frame.setGlassPane(glass);
        glass.setVisible(true);

        editProdDialog = new EditProdDialog(frame, true, medicineResponse, this);
        editProdDialog.setLocationRelativeTo(frame);
        editProdDialog.setVisible(true);

        glass.setVisible(false);
    }

    private void updateTexts() {
        Translator lang = Translator.getInstance();

        jLabel1.setText(lang.getString("prod.title"));
        btnAdd.setText(lang.getString("prod.btn.add"));
        jLabel2.setText(lang.getString("prod.lbl.status"));
        btnAll.setText(lang.getString("prod.btn.all"));
        btnMedicine.setText(lang.getString("prod.btn.medicine"));
        btnTool.setText(lang.getString("prod.btn.tool"));
        btnFood.setText(lang.getString("prod.btn.food"));
    }

    private void initComponents() {

        pnlTop = new JPanel();
        jLabel1 = new JLabel();
        txtSearch = new JTextField();
        btnAdd = new JButton();
        cmbField = new JComboBox<>();
        jLabel2 = new JLabel();
        pnlMain = new JPanel();
        btnExport = new JButton();
        btnRefresh = new JButton();
        radBarcode = new JRadioButton();
        radProdName = new JRadioButton();

        setLayout(new BorderLayout());

        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                Translator.getInstance().getString("prod.text.search"));
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_ICON,
                new FlatSVGIcon("icon/svg/search.svg", 0.4f));

        ButtonGroup group = new ButtonGroup();
        group.add(radBarcode);
        group.add(radProdName);

        radBarcode.setText("Barcode");

        radProdName.setText("Prod name");

        radBarcode.setSelected(true);

        pnlTop.setBackground(new Color(244, 248, 250));

        jLabel1.setBackground(new Color(51, 51, 255));
        jLabel1.setFont(new Font("Segoe UI", 1, 18)); // NOI18N

        btnAdd.setBackground(new Color(51, 153, 255));
        btnAdd.setFont(new Font("Segoe UI", 3, 12)); // NOI18N
        btnAdd.setForeground(new Color(255, 255, 255));
        btnAdd.setText("Thêm sản phẩm");
        btnAdd.setIcon(new FlatSVGIcon("icon/svg/add.svg", 0.30f));

        btnRefresh.setBackground(new Color(51, 153, 255));
        btnRefresh.setFont(new Font("Segoe UI", 3, 12)); // NOI18N
        btnRefresh.setForeground(new Color(255, 255, 255));
        btnRefresh.setText("Refresh");
        btnRefresh.setIcon(new FlatSVGIcon("icon/svg/refresh.svg", 20, 20));

        btnExport.setBackground(new Color(51, 153, 255));
        btnExport.setFont(new Font("Segoe UI", 3, 12)); // NOI18N
        btnExport.setForeground(new Color(255, 255, 255));
        btnExport.setText("Export");
        btnExport.setIcon(new FlatSVGIcon("icon/svg/export.svg", 0.30f));

        cmbField.setModel(new DefaultComboBoxModel<>(new String[]{"Tất cả", "Đang hoạt động",
                "Ngừng kinh doanh", "Còn hàng", "Sắp hết hàng", "Đã hết hàng"}));

        jLabel2.setFont(new Font("Segoe UI", 1, 12)); // NOI18N

        GroupLayout pnlTopLayout = new GroupLayout(pnlTop);
        pnlTop.setLayout(pnlTopLayout);
        pnlTopLayout.setHorizontalGroup(pnlTopLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(pnlTopLayout.createSequentialGroup().addGap(12, 12, 12)
                        .addGroup(pnlTopLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(txtSearch, GroupLayout.PREFERRED_SIZE, 365,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, 203,
                                        GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(pnlTopLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(pnlTopLayout.createSequentialGroup().addComponent(radProdName)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 35,
                                                Short.MAX_VALUE)
                                        .addComponent(jLabel2, GroupLayout.PREFERRED_SIZE, 71,
                                                GroupLayout.PREFERRED_SIZE))
                                .addGroup(pnlTopLayout.createSequentialGroup().addComponent(radBarcode).addGap(0, 0,
                                        Short.MAX_VALUE)))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cmbField, GroupLayout.PREFERRED_SIZE, 170,
                                GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnRefresh, GroupLayout.PREFERRED_SIZE, 140,
                                GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnAdd, GroupLayout.PREFERRED_SIZE, 140,
                                GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(btnExport,
                                GroupLayout.PREFERRED_SIZE, 140, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap()));
        pnlTopLayout.setVerticalGroup(pnlTopLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(pnlTopLayout.createSequentialGroup().addContainerGap()
                        .addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, 27,
                                GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlTopLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(cmbField, GroupLayout.PREFERRED_SIZE, 34,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel2, GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtSearch, GroupLayout.PREFERRED_SIZE, 33,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnRefresh, GroupLayout.PREFERRED_SIZE, 34,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnExport, GroupLayout.PREFERRED_SIZE, 34,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnAdd, GroupLayout.PREFERRED_SIZE, 34,
                                        GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(20, Short.MAX_VALUE))
                .addGroup(pnlTopLayout.createSequentialGroup().addGap(27, 27, 27).addComponent(radBarcode)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(radProdName)
                        .addContainerGap(19, Short.MAX_VALUE)));

        add(pnlTop, BorderLayout.PAGE_START);

        pnlMain.setBackground(new Color(255, 247, 247));

        GroupLayout pnlMainLayout = new GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(pnlMainLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGap(0, 959, Short.MAX_VALUE));
        pnlMainLayout.setVerticalGroup(pnlMainLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGap(0, 386, Short.MAX_VALUE));

        initPnlMain();

        add(pnlMain, BorderLayout.CENTER);
    }

    private void initPnlMain() {
        // pnlMain
        pnlMain.setLayout(new MigLayout("wrap, fill", "[fill]", "[grow 0][fill][grow 0]"));
        pnlCategory = new JPanel();
        pnlMain.add(pnlCategory);
        pnlProd = new JPanel();
        pnlProd.setBackground(new Color(255, 247, 247));
        pnlMain.add(new JScrollPane(pnlProd));

        // pnlCategory
        // add button category
        btnAll = new JButton();
        btnMedicine = new JButton();
        btnTool = new JButton();
        btnFood = new JButton();

        btnAll.setPreferredSize(new Dimension(150, 35));
        btnMedicine.setPreferredSize(btnAll.getPreferredSize());
        btnTool.setPreferredSize(btnAll.getPreferredSize());
        btnFood.setPreferredSize(btnAll.getPreferredSize());

        btnAll.putClientProperty(FlatClientProperties.STYLE, activeButtonStyle);
        btnFood.putClientProperty(FlatClientProperties.STYLE, normalButtonStyle);
        btnMedicine.putClientProperty(FlatClientProperties.STYLE, normalButtonStyle);
        btnTool.putClientProperty(FlatClientProperties.STYLE, normalButtonStyle);

        JPanel pnlTotalPage = new JPanel();
        JLabel lblTotalPage = new JLabel("Total Record:");
        lblTotalPage.setFont(new Font("Segoe UI", 1, 12));
        txtTotalPage = new JTextField();
        txtTotalPage.setPreferredSize(new Dimension(100, 35));
        txtTotalPage.setEditable(false);
        pnlTotalPage.add(lblTotalPage);
        pnlTotalPage.add(txtTotalPage);

        pnlCategory.setLayout(new MigLayout("wrap", "[fill][fill][fill][fill][]", "[grow 0]"));
        pnlCategory.add(btnAll);
        pnlCategory.add(btnMedicine);
        pnlCategory.add(btnFood);
        pnlCategory.add(btnTool);
        pnlCategory.add(pnlTotalPage, "pushx, align right");

        pnlProd.setLayout(new MigLayout("wrap 5, fill",
                "[fill, grow]15[fill, grow]15[fill, grow]15[fill, grow]15[fill, grow]", "[al top]"));

        JScrollPane scroll = (JScrollPane) pnlProd.getParent().getParent();
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE,
                "background:$Table.background;track:$Table.background;trackArc:999");
        scroll.getVerticalScrollBar().setUnitIncrement(30);

        pnlPage = new JPanel();
        pnlPage.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 0));

        pnlMain.add(pnlPage);
    }

    private JPanel initCardItem(MedicineResponse medicineResponse, int width, int height) {
        JPanel pnl = new JPanel();
        pnl.setLayout(new MigLayout("wrap, fill", "[]", "[][][]"));

        JLabel lblImage = new JLabel(HelperImageIcon.scaleIcon(medicineResponse.getAvatarUrl(), WIDTH_ITEM, HEIGHT_ITEM));

        JLabel name = new JLabel("<html><body style='width: 150px'>" + medicineResponse.getMedicineName() + "</body></html>");
        name.putClientProperty(FlatClientProperties.STYLE, "" + "font:$h5.font;");

        JPanel pnlBtn = new JPanel();
        JButton btnView = new JButton();
        btnView.setText("View");
        btnView.setBackground(new Color(36, 145, 255));
        btnView.setForeground(Color.WHITE);
        btnView.setFont(new Font("Roboto", Font.BOLD, 12));
        pnlBtn.add(btnView);

        JLabel lblQty = new JLabel(String.format("Qty: %s Viên", medicineResponse.getTotalQuantity()));
        lblQty.setFont(new Font("Roboto", Font.ITALIC, 13));
        lblQty.setForeground(Color.BLACK);

        pnl.add(lblImage, "al center");
        pnl.add(name, "al center");
        pnl.add(lblQty, "gapright push");
        pnl.add(pnlBtn, "gapleft push");

        pnl.putClientProperty(FlatClientProperties.STYLE, "background:#ffffff;foreground:#1a1a1a;arc:20");
        pnl.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true));

        Dimension cardSize = new Dimension(width, height);
        pnl.setPreferredSize(cardSize);
        pnl.setMinimumSize(cardSize);
        pnl.setMaximumSize(cardSize);

        btnView.addActionListener(e -> editDataForProd(medicineResponse.getBarcode()));

        return pnl;
    }

    private JButton btnAdd;
    private JButton btnExport;
    private JComboBox<String> cmbField;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JPanel pnlMain;
    private JPanel pnlTop;
    private JTextField txtSearch;
    private JRadioButton radBarcode;
    private JRadioButton radProdName;

}
