package com.pharmacy.view.dashboard;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.pharmacy.shared.service.DashboardService;
import com.pharmacy.util.ClientContext;
import com.pharmacy.util.FormatUtil;
import com.pharmacy.util.Translator;
import lombok.AllArgsConstructor;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DashBoardUI extends JPanel {

    private DashboardService dashboardService;

    private JPanel pnlContainer;
    private JPanel pnlHeader;
    private JPanel pnlCenter;
    private JPanel pnlFooter;

    private Color color = new Color(247, 250, 255);
    private JPanel pnlCanhBaoMain;
    private JPanel pnlchartPieMain;
    private JPanel pnlLineChartMain;

    private final List<CardItem> cards = new ArrayList<>();
    private JLabel lblTitleLineChart;
    private JLabel lblTitleChartPie;
    private JLabel lblTitleCanhBao;
    private JLabel lblTitleTopSanPham;
    private JLabel lblTitleHoaDon;

    public DashBoardUI() {
        dashboardService = ClientContext.getService(DashboardService.class);
        initComponents();

        updateTexts();
    }

    private void updateTexts() {
        Translator lang = Translator.getInstance();

        for (CardItem c : cards) {
            c.lblTitle.setText(lang.getString(c.keyTitle));
            c.lblSub.setText(lang.getString(c.keySub));
        }

        lblTitleChartPie.setText(lang.getString("dashboard.chart.pie"));
        lblTitleLineChart.setText(lang.getString("dashboard.chart.line"));
        lblTitleCanhBao.setText(lang.getString("dashboard.chart.warning"));
        lblTitleTopSanPham.setText(lang.getString("dashboard.chart.top_prod"));
        lblTitleHoaDon.setText(lang.getString("dashboard.chart.invoice"));
    }

    private void initComponents() {
        this.setLayout(new BorderLayout());

        pnlContainer = new JPanel(new BorderLayout(10, 10));
        pnlContainer.setBackground(color);

        initHeader();

        initCenter();

        initFooter();

        this.add(pnlContainer, BorderLayout.CENTER);
    }

    private void initHeader() {
        pnlHeader = new JPanel();
        pnlHeader.setLayout(new MigLayout("fill, insets 15 15 10 15",
                "[fill, grow, sg 1]25[fill, grow, sg 1]25[fill, grow, sg 1]25[fill, grow, sg 1]", "[fill, grow]"));
        pnlHeader.setBackground(color);

        pnlHeader.add(createCardPanel("icon/svg/money-bag.svg", "dashboard.card.today_revenue",
                FormatUtil.formatVND(dashboardService.getCalculateDailyRevenue()), "dashboard.card.diff_yesterday"));

        pnlHeader.add(createCardPanel("icon/svg/order.svg", "dashboard.card.orders",
                String.valueOf(dashboardService.getCountDailyInvoices()), "dashboard.card.order_type"));

        pnlHeader.add(createCardPanel("icon/svg/chart.svg", "dashboard.card.estimate_profit",
                FormatUtil.formatVND(dashboardService.getCalculateDailyProfit()), "dashboard.card.estimate"));

        pnlHeader.add(createCardPanel("icon/svg/user-check.svg", "dashboard.card.new_customer",
                String.valueOf(dashboardService.getCountDailyNewCustomers()), "dashboard.card.member"));

        pnlContainer.add(pnlHeader, BorderLayout.NORTH);
    }

    private JPanel createCardPanel(String iconPath, String titleKey, String value, String subKey) {
        Translator lang = Translator.getInstance();

        JPanel pnl = new JPanel(new MigLayout("fill, insets 15 0 15 0", "[]", "[][][]"));
        pnl.setBackground(Color.WHITE);
        pnl.putClientProperty(FlatClientProperties.STYLE, "arc: 20; background: #FFFFFF");

        JLabel lblTitle = new JLabel(lang.getString(titleKey));
        lblTitle.setForeground(new Color(77, 75, 75));
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 17));
        lblTitle.setIcon(new FlatSVGIcon(iconPath, 24, 24));
        lblTitle.setIconTextGap(10);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblValue.setForeground(new Color(22, 124, 198));

        JLabel lblSub = new JLabel(lang.getString(subKey));
        if (lang.getString(subKey).contains("%"))
            lblSub.setForeground(new Color(40, 123, 62));

        pnl.add(lblTitle, "wrap, al center");
        pnl.add(lblValue, "wrap, al center");
        pnl.add(lblSub, "al center");

        cards.add(new CardItem(lblTitle, lblValue, lblSub, titleKey, subKey));

        return pnl;
    }

    private void initCenter() {
        pnlCenter = new JPanel(new MigLayout("fill, insets 0 15 5 15", "[fill, grow]25[fill, grow]", "[fill, grow]"));
        pnlCenter.setBackground(color);

        JPanel chartLineChart = new JPanel(new MigLayout("fill, insets 15", "[fill, grow]", "[][fill, grow]"));
        chartLineChart.setBackground(Color.WHITE);
        chartLineChart.putClientProperty(FlatClientProperties.STYLE, "arc: 20; background: #FFFFFF");

        lblTitleLineChart = new JLabel();
        lblTitleLineChart.setFont(new Font("SansSerif", Font.BOLD, 17));

        pnlLineChartMain = new JPanel(new BorderLayout());
        pnlLineChartMain.setBackground(Color.WHITE);
        createLineChart();

        chartLineChart.add(lblTitleLineChart, "wrap, align left");
        chartLineChart.add(pnlLineChartMain, "grow, push");

        JPanel chartPie = new JPanel(new MigLayout("fill, insets 15", "[fill, grow]", "[][fill, grow]"));
        chartPie.setBackground(Color.WHITE);
        chartPie.putClientProperty(FlatClientProperties.STYLE, "arc: 20; background: #FFFFFF");

        lblTitleChartPie = new JLabel();
        lblTitleChartPie.setFont(new Font("SansSerif", Font.BOLD, 17));

        pnlchartPieMain = new JPanel(new BorderLayout());
        pnlchartPieMain.setBackground(Color.WHITE);
        createPieChartPanel();

        chartPie.add(lblTitleChartPie, "wrap, align left");
        chartPie.add(pnlchartPieMain, "grow, push");

        pnlCenter.add(chartLineChart, "w 60%, h 100%");
        pnlCenter.add(chartPie, "w 40%, h 100%");

        pnlContainer.add(pnlCenter, BorderLayout.CENTER);
    }

    private double fakeData(int index) {
        return switch (index) {
            case 0 -> 2500000;
            case 1 -> 4200000;
            case 2 -> 3800000;
            case 3 -> 5100000;
            case 4 -> 2900000;
            case 5 -> 6500000;
            default -> 0;
        };
    }

    private void createLineChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String seriesName = "Doanh thu";

        Map<String, Double> data = dashboardService.getRevenueLast7Days();

//		data.forEach((x, y) -> dataset.addValue(y, seriesName, x));

        int index = 0;
        for (Map.Entry<String, Double> mpEle : data.entrySet()) {
            double value = mpEle.getValue();
            if (value == 0)
                value += fakeData(index);
            dataset.addValue(value, seriesName, mpEle.getKey());
            index++;
        }

        JFreeChart chart = ChartFactory.createLineChart(null, null, null, dataset, PlotOrientation.VERTICAL, false,
                true, false);

        chart.setBackgroundPaint(Color.WHITE);

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);

        plot.setRangeGridlinePaint(new Color(230, 230, 230));
        plot.setDomainGridlinesVisible(false);

        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();

        renderer.setSeriesPaint(0, new Color(22, 124, 198));

        renderer.setSeriesStroke(0, new BasicStroke(3.0f));

        renderer.setSeriesShapesVisible(0, true);
        renderer.setDrawOutlines(true);
        renderer.setUseFillPaint(true);
        renderer.setSeriesFillPaint(0, Color.WHITE);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setNumberFormatOverride(new DecimalFormat("#,###"));
        rangeAxis.setAxisLineVisible(false);
        rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 11));
        rangeAxis.setTickLabelPaint(Color.GRAY);

        plot.getDomainAxis().setAxisLineVisible(false);
        plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 11));
        plot.getDomainAxis().setTickLabelPaint(Color.GRAY);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPopupMenu(null);
        chartPanel.setMouseWheelEnabled(true);

        pnlLineChartMain.add(chartPanel, BorderLayout.CENTER);
    }

    private void createPieChartPanel() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Thuốc", 65); // 65%
        dataset.setValue("Thực phẩm chức năng", 25); // 25%
        dataset.setValue("Dụng cụ y tế", 10); // 10%

        JFreeChart chart = ChartFactory.createPieChart(null, dataset, false, true, false);

        chart.setBorderVisible(false);
        chart.setBackgroundPaint(Color.WHITE);

        PiePlot plot = (PiePlot) chart.getPlot();

        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {2}", new DecimalFormat("#,##0"),
                new DecimalFormat("0.0%")));

        plot.setSectionPaint("Thuốc", new Color(22, 124, 198));
        plot.setSectionPaint("Thực phẩm chức năng", new Color(255, 179, 0));
        plot.setSectionPaint("Dụng cụ y tế", new Color(46, 204, 113));

        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);

        plot.setLabelFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 12));
        plot.setLabelPaint(UIManager.getColor("Label.foreground"));
        plot.setLabelLinkStyle(org.jfree.chart.plot.PieLabelLinkStyle.STANDARD);
        plot.setLabelBackgroundPaint(Color.WHITE);
        plot.setLabelShadowPaint(null);
        plot.setLabelOutlinePaint(null);

        plot.setShadowPaint(null);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(300, 200));
        chartPanel.setMouseWheelEnabled(true);

        chartPanel.setPopupMenu(null);

        chartPanel.setBackground(Color.WHITE);

        pnlchartPieMain.add(chartPanel, BorderLayout.CENTER);
    }

    private void initFooter() {
        pnlFooter = new JPanel(
                new MigLayout("fill, insets 0 15 10 15", "[fill, grow]25[fill, grow]25[fill, grow]", "[fill, grow]"));
        pnlFooter.setBackground(color);

        JPanel pnlCanhBao = new JPanel(new MigLayout("fill, insets 15", "[fill, grow]", "[][fill, grow]"));
        pnlCanhBao.setBackground(Color.WHITE);
        pnlCanhBao.putClientProperty(FlatClientProperties.STYLE, "arc: 20; background: #FFFFFF");

        lblTitleCanhBao = new JLabel();
        lblTitleCanhBao.setFont(new Font("SansSerif", Font.BOLD, 16));

        pnlCanhBaoMain = new JPanel(new BorderLayout());
        pnlCanhBaoMain.setBackground(Color.WHITE);
        initPnlCanhBao();

        pnlCanhBao.add(lblTitleCanhBao, "wrap, align left");
        pnlCanhBao.add(pnlCanhBaoMain, "grow, push");

        JPanel pnlTopSanPham = new JPanel(new MigLayout("fill, insets 15", "[fill, grow]", "[][][fill, grow]"));
        pnlTopSanPham.setBackground(Color.WHITE);
        pnlTopSanPham.putClientProperty(FlatClientProperties.STYLE, "arc: 20; background: #FFFFFF");

        lblTitleTopSanPham = new JLabel();
        lblTitleTopSanPham.setFont(new Font("SansSerif", Font.BOLD, 16));

        JPanel pnlTopSanPhamMain = createTopProductChartPanel();
        pnlTopSanPhamMain.setBackground(Color.WHITE);

        pnlTopSanPham.add(lblTitleTopSanPham, "wrap, align left");
        pnlTopSanPham.add(new JSeparator(), "wrap, growx");
        pnlTopSanPham.add(pnlTopSanPhamMain, "grow, push");

        JPanel pnlHoaDon = new JPanel(new MigLayout("fill, insets 15", "[fill, grow]", "[][][fill, grow]"));
        pnlHoaDon.setBackground(Color.WHITE);
        pnlHoaDon.putClientProperty(FlatClientProperties.STYLE, "arc: 20; background: #FFFFFF");

        lblTitleHoaDon = new JLabel();
        lblTitleHoaDon.setFont(new Font("SansSerif", Font.BOLD, 16));

        JPanel pnlHoaDonMain = createBarChartPanel();
        pnlHoaDonMain.setBackground(Color.WHITE);

        pnlHoaDon.add(lblTitleHoaDon, "wrap, align left");
        pnlHoaDon.add(new JSeparator(), "wrap, growx");
        pnlHoaDon.add(pnlHoaDonMain, "grow, push");

        pnlFooter.add(pnlCanhBao, "w 40%, h 280!");
        pnlFooter.add(pnlTopSanPham, "w 30%, h 280!");
        pnlFooter.add(pnlHoaDon, "w 30%, h 280!");

        pnlContainer.add(pnlFooter, BorderLayout.SOUTH);
    }

    private JPanel createTopProductChartPanel() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String seriesName = "Đã bán";

        dataset.addValue(680, seriesName, "Hapacol 250"); // Top 1
        dataset.addValue(450, seriesName, "Panadol Extra");
        dataset.addValue(340, seriesName, "Khẩu trang 4D");
        dataset.addValue(215, seriesName, "Vitamin C 500mg");
        dataset.addValue(120, seriesName, "Bông băng y tế");

        JFreeChart chart = ChartFactory.createBarChart(null, null, null, dataset, PlotOrientation.HORIZONTAL, false,
                true, false);

        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);

        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(new Color(240, 240, 240));

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);

        // Màu xanh ngọc (Teal)
        renderer.setSeriesPaint(0, new Color(0, 150, 136));

        // Độ rộng của thanh bar
        renderer.setMaximumBarWidth(0.07);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setAxisLineVisible(false);
        domainAxis.setTickMarksVisible(false);
        domainAxis.setTickLabelFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 10));
        domainAxis.setTickLabelPaint(new Color(71, 71, 71));

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setAxisLineVisible(false);
        rangeAxis.setTickLabelFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 11));
        rangeAxis.setTickLabelPaint(Color.GRAY);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPopupMenu(null);
        chartPanel.setMouseWheelEnabled(true);

        return chartPanel;
    }

    private JPanel createBarChartPanel() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String seriesBan = "Hóa đơn bán";
        String seriesTra = "Hóa đơn trả";

        Map<String, Integer> mpBan = dashboardService.getSalesInvoiceCountLast5Days();
        Map<String, Integer> mpTra = dashboardService.getReturnInvoiceCountLast5Days();

        int index = 0;
        for (Map.Entry<String, Integer> entry : mpBan.entrySet()) {
            String dateLabel = entry.getKey();
            int banValue = mpBan.get(dateLabel);
            int traValue = mpTra.get(dateLabel);

            banValue += fakeDataTotalInvoice(index);
            traValue += fakeDataTotalInvoiceReturn(index);

            dataset.addValue(banValue, seriesBan, dateLabel);
            dataset.addValue(traValue, seriesTra, dateLabel);

            index++;
        }

        JFreeChart chart = ChartFactory.createBarChart(null, null, null, dataset, PlotOrientation.VERTICAL, true, true,
                false);

        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(new Color(230, 230, 230));

        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);

        renderer.setSeriesPaint(0, new Color(22, 124, 198));
        renderer.setSeriesPaint(1, new Color(231, 76, 60));

        renderer.setItemMargin(0.04); // Khoảng cách giữa cột Bán và Trả

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setAxisLineVisible(false);
        rangeAxis.setTickLabelFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 12));
        rangeAxis.setTickLabelPaint(new Color(80, 80, 80));

        plot.getDomainAxis().setAxisLineVisible(false);
        plot.getDomainAxis().setTickLabelFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 12));
        plot.getDomainAxis().setTickLabelPaint(new Color(80, 80, 80));

        chart.getLegend().setFrame(org.jfree.chart.block.BlockBorder.NONE);
        chart.getLegend().setItemFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 12));

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPopupMenu(null);
        chartPanel.setMouseWheelEnabled(true);

        return chartPanel;
    }

    private int fakeDataTotalInvoiceReturn(int index) {
        return switch (index) {
            case 0 -> 2;
            case 1 -> 5;
            case 3 -> 8;
            default -> 0;
        };
    }

    private int fakeDataTotalInvoice(int index) {
        return switch (index) {
            case 0 -> 45;
            case 1 -> 52;
            case 2 -> 38;
            case 3 -> 60;
            default -> 0;
        };
    }

    private void initPnlCanhBao() {
        JTabbedPane tabPanel = new JTabbedPane();

        String[] tblHeaderHetHan = new String[] { "Tên thuốc", "Số lô", "Hạn SD", "SL tồn" };
        Object[][] dataHetHan = { { "Panadol Extra", "L1023", "01/12/2025", "150" },
                { "Vitamin C 500mg", "L9921", "30/11/2025", "20" }, { "Berberin", "L8822", "15/10/2025", "05" },
                { "Khẩu trang Y tế", "K1102", "20/11/2025", "0" } };

        DefaultTableModel modelHetHan = new DefaultTableModel(dataHetHan, tblHeaderHetHan) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tblPage1 = new JTable(modelHetHan);
        styleTable(tblPage1);

        String[] tblHeaderSapHetHang = new String[] { "Tên thuốc", "Loại sản phẩm", "SL tồn" };
        Object[][] dataSapHetHang = { { "Dầu gió Trường Sơn", "Dầu xoa", "02" },
                { "Nước muối sinh lý", "Dung dịch", "05" }, { "Salonpas Gel", "Cao dán", "03" },
                { "Băng cá nhân Urgo", "Y tế", "10" } };

        DefaultTableModel modelSapHetHang = new DefaultTableModel(dataSapHetHang, tblHeaderSapHetHang) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tblPage2 = new JTable(modelSapHetHang);
        styleTable(tblPage2);

        tabPanel.addTab("Thuốc hết hạn", new JScrollPane(tblPage1));
        tabPanel.setBackgroundAt(0, new Color(255, 204, 204));
        tabPanel.setForegroundAt(0, new Color(204, 0, 0));

        tabPanel.addTab("Sắp hết hàng", new JScrollPane(tblPage2));
        tabPanel.setBackgroundAt(1, new Color(255, 255, 204));
        tabPanel.setForegroundAt(1, new Color(204, 153, 0));

        pnlCanhBaoMain.add(tabPanel, BorderLayout.CENTER);
    }

    private void styleTable(JTable table) {
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(225, 240, 255));
        header.setForeground(Color.BLACK);
        header.setFont(new Font("SansSerif", Font.BOLD, 13));

        header.setOpaque(true);

        table.setRowHeight(25);
        table.getTableHeader().setReorderingAllowed(false);

        table.setSelectionBackground(new Color(179, 217, 255));
        table.setSelectionForeground(Color.BLACK);
    }

}

@AllArgsConstructor
class CardItem {
    JLabel lblTitle;
    JLabel lblValue;
    JLabel lblSub;
    String keyTitle;
    String keySub;
}
