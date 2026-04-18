package com.pharmacy.view.menu;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;
import com.pharmacy.util.Translator;
import com.pharmacy.view.menu.mode.LightDarkMode;
import com.pharmacy.view.menu.mode.ToolBarAccentColor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Menu extends JPanel {

	private final String menuItems[][] = {
		    { "menu.item.overview" },
		    { "menu.item.dashboard" },
		    { "menu.item.sell" },
		    { "menu.item.return" },
		    { "menu.item.management" },
		    { "menu.item.product", "menu.item.category", "menu.item.medicine", "menu.item.medical_device", "menu.item.other_product" },
		    { "menu.item.invoice" },
		    { "menu.item.batch", "menu.item.shipment" ,"menu.item.stock_alert" },
		    { "menu.item.supplier" },
		    { "menu.item.customer" },
		    { "menu.item.staff" },
		    { "menu.item.voucher" },
		    { "menu.item.other" },
		    { "menu.item.helper" },
		    { "menu.item.contact" },
		    { "menu.item.profile", "menu.item.personal_info", "menu.item.activity_log", "menu.item.notification" },
		    { "menu.item.setting" },
		    { "menu.item.logout" }
		};

	public boolean isMenuFull() {
		return menuFull;
	}

	public void setMenuFull(boolean menuFull) {
		this.menuFull = menuFull;
		if (menuFull) {
			header.setText(headerName);
			header.setHorizontalAlignment(getComponentOrientation().isLeftToRight() ? JLabel.LEFT : JLabel.RIGHT);
		} else {
			header.setText("");
			header.setHorizontalAlignment(JLabel.CENTER);
		}
		for (Component com : panelMenu.getComponents()) {
			if (com instanceof MenuItem) {
				((MenuItem) com).setFull(menuFull);
			}
		}
		lightDarkMode.setMenuFull(menuFull);
		toolBarAccentColor.setMenuFull(menuFull);
	}

	private final List<MenuEvent> events = new ArrayList<>();
	private boolean menuFull = true;
	private final String headerName = "  Alamy Pharmacy";

	protected final boolean hideMenuTitleOnMinimum = true;
	protected final int menuTitleLeftInset = 5;
	protected final int menuTitleVgap = 5;
	protected final int menuMaxWidth = 250;
	protected final int menuMinWidth = 60;
	protected final int headerFullHgap = 5;

	public Menu() {
		init();

		Translator.getInstance().addLanguageChangeListener(locale -> {
			SwingUtilities.invokeLater(this::createMenu);
		});
	}

	private void init() {
		setLayout(new MenuLayout());
		putClientProperty(FlatClientProperties.STYLE,
				"" + "border:20,2,2,2;" + "background:$Menu.background;" + "arc:10");
		header = new JLabel(headerName);
		ImageIcon icon = new ImageIcon(getClass().getResource("/images/logo.png"));
		Image image = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
		header.setIcon(new ImageIcon(image));
		header.putClientProperty(FlatClientProperties.STYLE,
				"" + "font:$Menu.header.font;" + "foreground:$Menu.foreground");

		scroll = new JScrollPane();
		panelMenu = new JPanel(new MenuItemLayout(this));
		panelMenu.putClientProperty(FlatClientProperties.STYLE, "" + "border:5,5,5,5;" + "background:$Menu.background");

		scroll.setViewportView(panelMenu);
		scroll.putClientProperty(FlatClientProperties.STYLE, "" + "border:null");
		JScrollBar vscroll = scroll.getVerticalScrollBar();
		vscroll.setUnitIncrement(10);
		vscroll.putClientProperty(FlatClientProperties.STYLE,
				"" + "width:$Menu.scroll.width;" + "trackInsets:$Menu.scroll.trackInsets;"
						+ "thumbInsets:$Menu.scroll.thumbInsets;" + "background:$Menu.ScrollBar.background;"
						+ "thumb:$Menu.ScrollBar.thumb");
		createMenu();
		lightDarkMode = new LightDarkMode();
		toolBarAccentColor = new ToolBarAccentColor(this);
		toolBarAccentColor.setVisible(FlatUIUtils.getUIBoolean("AccentControl.show", false));
		add(header);
		add(scroll);
		add(lightDarkMode);
		add(toolBarAccentColor);
	}
	
	private void createMenu() {
	    Translator lang = Translator.getInstance();
	    // Delete old menu before rebuild
	    panelMenu.removeAll();

	    int index = 0;
	    for (int i = 0; i < menuItems.length; i++) {
	        // menuItems[i] array
	        String[] translatedItems = Arrays.stream(menuItems[i])
	                                         .map(lang::getString)
	                                         .toArray(String[]::new);

	        String menuName = translatedItems[0];
	        if (menuName.startsWith("~") && menuName.endsWith("~")) {
	            panelMenu.add(createTitle(menuName));
	        } else {
	            MenuItem menuItem = new MenuItem(this, translatedItems, index++, events);
	            panelMenu.add(menuItem);
	        }
	    }

	    panelMenu.revalidate();
	    panelMenu.repaint();
	}

	private JLabel createTitle(String title) {
		String menuName = title.substring(1, title.length() - 1);
		JLabel lbTitle = new JLabel(menuName);
		lbTitle.putClientProperty(FlatClientProperties.STYLE,
				"" + "font:$Menu.label.font;" + "foreground:$Menu.title.foreground");
		return lbTitle;
	}

	public void setSelectedMenu(int index, int subIndex) {
		runEvent(index, subIndex);
	}

	protected void setSelected(int index, int subIndex) {
		int size = panelMenu.getComponentCount();
		for (int i = 0; i < size; i++) {
			Component com = panelMenu.getComponent(i);
			if (com instanceof MenuItem) {
				MenuItem item = (MenuItem) com;
				if (item.getMenuIndex() == index) {
					item.setSelectedIndex(subIndex);
				} else {
					item.setSelectedIndex(-1);
				}
			}
		}
	}

	protected void runEvent(int index, int subIndex) {
		MenuAction menuAction = new MenuAction();
		for (MenuEvent event : events) {
			event.menuSelected(index, subIndex, menuAction);
		}
		if (!menuAction.isCancel()) {
			setSelected(index, subIndex);
		}
	}

	public void addMenuEvent(MenuEvent event) {
		events.add(event);
	}

	public void hideMenuItem() {
		for (Component com : panelMenu.getComponents()) {
			if (com instanceof MenuItem) {
				((MenuItem) com).hideMenuItem();
			}
		}
		revalidate();
	}

	public boolean isHideMenuTitleOnMinimum() {
		return hideMenuTitleOnMinimum;
	}

	public int getMenuTitleLeftInset() {
		return menuTitleLeftInset;
	}

	public int getMenuTitleVgap() {
		return menuTitleVgap;
	}

	public int getMenuMaxWidth() {
		return menuMaxWidth;
	}

	public int getMenuMinWidth() {
		return menuMinWidth;
	}

	private JLabel header;
	private JScrollPane scroll;
	private JPanel panelMenu;
	private LightDarkMode lightDarkMode;
	private ToolBarAccentColor toolBarAccentColor;

	private class MenuLayout implements LayoutManager {

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
				Insets insets = parent.getInsets();
				int x = insets.left;
				int y = insets.top;
				int gap = UIScale.scale(5);
				int sheaderFullHgap = UIScale.scale(headerFullHgap);
				int width = parent.getWidth() - (insets.left + insets.right);
				int height = parent.getHeight() - (insets.top + insets.bottom);
				int iconWidth = width;
				int iconHeight = header.getPreferredSize().height;
				int hgap = menuFull ? sheaderFullHgap : 0;
				int accentColorHeight = 0;
				if (toolBarAccentColor.isVisible()) {
					accentColorHeight = toolBarAccentColor.getPreferredSize().height + gap;
				}

				header.setBounds(x + hgap, y, iconWidth - (hgap * 2), iconHeight);
				int ldgap = UIScale.scale(10);
				int ldWidth = width - ldgap * 2;
				int ldHeight = lightDarkMode.getPreferredSize().height;
				int ldx = x + ldgap;
				int ldy = y + height - ldHeight - ldgap - accentColorHeight;

				int menux = x;
				int menuy = y + iconHeight + gap;
				int menuWidth = width;
				int menuHeight = height - (iconHeight + gap) - (ldHeight + ldgap * 2) - (accentColorHeight);
				scroll.setBounds(menux, menuy, menuWidth, menuHeight);

				lightDarkMode.setBounds(ldx, ldy, ldWidth, ldHeight);

				if (toolBarAccentColor.isVisible()) {
					int tbheight = toolBarAccentColor.getPreferredSize().height;
					int tbwidth = Math.min(toolBarAccentColor.getPreferredSize().width, ldWidth);
					int tby = y + height - tbheight - ldgap;
					int tbx = ldx + ((ldWidth - tbwidth) / 2);
					toolBarAccentColor.setBounds(tbx, tby, tbwidth, tbheight);
				}
			}
		}
	}
}
