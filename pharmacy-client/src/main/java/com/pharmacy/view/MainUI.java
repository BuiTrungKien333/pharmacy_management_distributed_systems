package com.pharmacy.view;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.util.UIScale;
import com.pharmacy.app.ClientApp;
import com.pharmacy.util.ClientSecurityContext;
import com.pharmacy.util.Translator;
import com.pharmacy.view.batch.BatchUI;
import com.pharmacy.view.dashboard.DashBoardUI;
import com.pharmacy.view.invoice.InvoiceUI;
import com.pharmacy.view.medicine.MedicineUI;
import com.pharmacy.view.menu.Menu;
import com.pharmacy.view.menu.MenuAction;
import com.pharmacy.view.refund.RefundUI;
import com.pharmacy.view.report.ReportUI;
import com.pharmacy.view.sell.SellUI;
import com.pharmacy.view.setting.SettingUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MainUI extends JLayeredPane {

    private Menu menu;
    private JPanel pnlBody;
    private JButton btnMenu;

    private MedicineUI medicineUI;
    private SettingUI settingUI;
    private ReportUI reportUI;
    private BatchUI batchUI;
    private SellUI sellUI;
    private RefundUI refundUI;
    private InvoiceUI invoiceUI;

    public MainUI() {
        init();
    }

    private void init() {
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setLayout(new MainFormLayout());

        menu = new Menu();
        pnlBody = new JPanel(new BorderLayout());

        initMenuArrowIcon();

        btnMenu.putClientProperty(FlatClientProperties.STYLE,
                "" + "background:$Menu.button.background;" + "arc:999;" + "focusWidth:0;" + "borderWidth:0");

        btnMenu.addActionListener((ActionEvent e) -> {
            setMenuFull(!menu.isMenuFull());
        });

        initMenuEvent();
        setLayer(btnMenu, JLayeredPane.POPUP_LAYER);
        add(btnMenu);
        add(menu);
        add(pnlBody);
    }

    @Override
    public void applyComponentOrientation(ComponentOrientation o) {
        super.applyComponentOrientation(o);
        initMenuArrowIcon();
    }

    private void initMenuArrowIcon() {
        if (btnMenu == null) {
            btnMenu = new JButton();
        }
        String icon = (getComponentOrientation().isLeftToRight()) ? "menu_left.svg" : "menu_right.svg";
        btnMenu.setIcon(new FlatSVGIcon("icon/svg/" + icon, 0.8f));
    }

    private void initMenuEvent() {
        menu.addMenuEvent((int index, int subIndex, MenuAction action) -> {
            switch (index) {
                case 0 -> ClientApp.showForm(new DashBoardUI());
                case 1 -> {
                    if (sellUI == null)
                        sellUI = new SellUI();

                    ClientApp.showForm(sellUI);
                }
                case 2 -> {
                    if (refundUI == null)
                        refundUI = new RefundUI();

                    ClientApp.showForm(refundUI);
                }
                case 3 -> {
                    if (medicineUI == null)
                        medicineUI = new MedicineUI();
//                    else
//                        medicineUI.refreshDataProd();

                    ClientApp.showForm(medicineUI);
                }
//                case 4 -> {
//                    if (!Auth.hasPermission("INVOICE_VIEW"))
//                        return;
//
//                    if (invoice == null)
//                        invoice = new InvoiceGUI();
//                    else
//                        invoice.refreshData();
//
//                    ClientApp.showForm(invoice);
//                }
//                case 5 -> {
//                    switch (subIndex) {
//                        case 1 -> {
//                            if (!Auth.hasPermission("BATCH_VIEW"))
//                                return;
//
//                            if (batch == null)
//                                batch = new BatchGUI();
//                            else
//                                batch.refreshDataShipment();
//
//                            ClientApp.showForm(batch);
//                        }
//                        case 2 -> ClientApp.showForm(new InventoryGUI());
//                        default -> action.cancel();
//                    }
//                }
//                case 6 -> {
//                    if (!Auth.hasPermission("SUPPLIER_MANAGE"))
//                        return;
//
//                    ClientApp.showForm(new SupplierGUI());
//                }
//                case 7 -> {
//                    if (!Auth.hasPermission("CUSTOMER_MANAGE"))
//                        return;
//
//                    ClientApp.showForm(new CustomerGUI());
//                }
//                case 8 -> {
//                    if (!Auth.hasPermission("EMPLOYEE_MANAGE"))
//                        return;
//
//                    ClientApp.showForm(new EmployeeGUI());
//                }
//                case 9 -> {
//                    if (!Auth.hasPermission("VOUCHER_MANAGE"))
//                        return;
//
//                    ClientApp.showForm(new VoucherGUI());
//                }
//                case 10 -> {
//                    // TODO:
//                }
//                case 11 -> {
//
//                    if (report == null)
//                        report = new ReportGUI();
//                    else
//                        report.clearForm();
//
//                    ClientApp.showForm(report);
//                }
//                case 12 -> ClientApp.showForm(new ProfileGUI());
//                case 13 -> {
//                    if (!Auth.hasPermission("SEND_REPORT"))
//                        return;
//
//                    if (setting == null)
//                        setting = new SettingGUI();
//                    else
//                        setting.loadSettings();
//
//                    ClientApp.showForm(setting);
//                }
                case 14 -> {
                    int confirm = JOptionPane.showConfirmDialog(this,
                            Translator.getInstance().getString("main.text.logout"));

                    if (confirm == JOptionPane.YES_OPTION) {
                        medicineUI = null;
                        settingUI = null;
                        reportUI = null;
                        batchUI = null;
                        sellUI = null;
                        refundUI = null;
                        invoiceUI = null;

                        ClientApp.logoutSuccess();
                        ClientSecurityContext.clear();
                    }
                }
                default -> action.cancel();
            }
        });
    }

    private void setMenuFull(boolean full) {
        String icon;
        if (getComponentOrientation().isLeftToRight())
            icon = (full) ? "menu_left.svg" : "menu_right.svg";
        else
            icon = (full) ? "menu_right.svg" : "menu_left.svg";

        btnMenu.setIcon(new FlatSVGIcon("icon/svg/" + icon, 0.8f));
        menu.setMenuFull(full);
        revalidate();
    }

    public void hideMenu() {
        menu.hideMenuItem();
    }

    public void showForm(Component component) {
        pnlBody.removeAll();

        pnlBody.add(component);
        pnlBody.repaint();
        pnlBody.revalidate();
    }

    public void setSelectedMenu(int index, int subIndex) {
        menu.setSelectedMenu(index, subIndex);
    }

    private class MainFormLayout implements LayoutManager {

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) {
                return new Dimension(5, 5);
            }
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) {
                return new Dimension(0, 0);
            }
        }

        @Override
        public void layoutContainer(Container parent) {
            synchronized (parent.getTreeLock()) {
                boolean ltr = parent.getComponentOrientation().isLeftToRight();
                Insets insets = UIScale.scale(parent.getInsets());
                int x = insets.left;
                int y = insets.top;
                int width = parent.getWidth() - (insets.left + insets.right);
                int height = parent.getHeight() - (insets.top + insets.bottom);
                int menuWidth = UIScale.scale(menu.isMenuFull() ? menu.getMenuMaxWidth() : menu.getMenuMinWidth());
                int menuX = ltr ? x : x + width - menuWidth;
                menu.setBounds(menuX, y, menuWidth, height);
                int btnMenuWidth = btnMenu.getPreferredSize().width;
                int btnMenuHeight = btnMenu.getPreferredSize().height;
                int menubX;
                if (ltr) {
                    menubX = (int) (x + menuWidth - (btnMenuWidth * (menu.isMenuFull() ? 0.5f : 0.3f)));
                } else {
                    menubX = (int) (menuX - (btnMenuWidth * (menu.isMenuFull() ? 0.5f : 0.7f)));
                }
                btnMenu.setBounds(menubX, UIScale.scale(30), btnMenuWidth, btnMenuHeight);
                int gap = UIScale.scale(5);
                int bodyWidth = width - menuWidth - gap;
                int bodyHeight = height;
                int bodyx = ltr ? (x + menuWidth + gap) : x;
                int bodyy = y;
                pnlBody.setBounds(bodyx, bodyy, bodyWidth, bodyHeight);
            }
        }
    }

}
