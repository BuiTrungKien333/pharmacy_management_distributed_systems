package com.pharmacy.util;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class CustomQuantitySpinner extends JPanel {

	private int value = 1;

	private final int min = 1;

	private int max = 1000;

	private RoundButton btnMinus;

	private RoundButton btnPlus;

	private RoundTextField txtValue;

	private Color mainColor = new Color(46, 153, 217);

	private Color bgColor = new Color(247, 247, 247);

	private Color borderColor = new Color(200, 200, 200);

	private Font fontBold = new Font("SansSerif", Font.BOLD, 14);

	public CustomQuantitySpinner() {
		setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
		setBackground(new Color(250, 250, 250));

		initUI();
		addEvents();
	}

	public void addActionListener(java.awt.event.ActionListener listener) {
		txtValue.addActionListener(listener);
	}

	private void initUI() {
		btnMinus = new RoundButton("-");

		txtValue = new RoundTextField(value + "", 5);

		btnPlus = new RoundButton("+");

		add(btnMinus);
		add(txtValue);
		add(btnPlus);
	}

	private void addEvents() {
		btnMinus.addActionListener(e -> {
			if (value > min) {
				value--;
				txtValue.setText(String.valueOf(value));
			}
		});

		btnPlus.addActionListener(e -> {
			if (value < max) {
				value++;
				txtValue.setText(String.valueOf(value));
			}
		});

		txtValue.addActionListener(e -> enforceLimitAndReport());

		txtValue.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override
			public void focusLost(java.awt.event.FocusEvent e) {
				enforceLimitAndReport();
			}
		});
	}

	private void enforceLimitAndReport() {
		int current = getValue();

		if (current > max)
			current = max;
		else if (current < min)
			current = min;

		setValue(current);
	}

	public int getValue() {
		try {
			String text = txtValue.getText().trim();
			int val = Integer.parseInt(text);

			if (val < min)
				val = min;

			if (val > max)
				val = max;

			this.value = val;
		} catch (NumberFormatException e) {
		}
		return this.value;
	}

	public void setValue(int newValue) {
		if (newValue >= min && newValue <= max) {
			this.value = newValue;
			txtValue.setText(String.valueOf(value));
		}
	}

	public void setMaxQuantity(int max) {
		if (max < this.min)
			this.max = this.min;
		else
			this.max = max;

		if (this.value > this.max)
			setValue(this.max);
	}

	private class RoundButton extends JButton {
		public RoundButton(String text) {
			super(text);
			setPreferredSize(new Dimension(30, 30)); // Kích thước cố định (Vuông -> Tròn)
			setMargin(new Insets(0, 0, 0, 0));
			setContentAreaFilled(false);
			setFocusPainted(false);
			setBorderPainted(false);
			setFont(fontBold);
			setForeground(mainColor);
			setCursor(new Cursor(Cursor.HAND_CURSOR));
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			if (getModel().isPressed()) {
				g2.setColor(borderColor);
			} else {
				g2.setColor(bgColor);
			}
			g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);

			g2.setColor(borderColor);
			g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);

			g2.dispose();
			super.paintComponent(g);
		}
	}

	private class RoundTextField extends JTextField {
		private Shape shape;

		public RoundTextField(String text, int size) {
			super(text, size);
			setOpaque(false); // Làm trong suốt để vẽ background bo góc
			setHorizontalAlignment(CENTER);
			setFont(fontBold);
			setForeground(mainColor); // Chữ màu xanh dương
			setPreferredSize(new Dimension(50, 30)); // Chiều dài 55, cao 30 (bằng nút)
			setBorder(new EmptyBorder(0, 5, 0, 5)); // Padding nội dung
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			// Vẽ nền bo góc (Bo góc 10px)
			g2.setColor(bgColor);
			g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);

			super.paintComponent(g); // Vẽ text lên trên
			g2.dispose();
		}

		@Override
		protected void paintBorder(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			// Vẽ viền bo góc (Màu xám nhẹ)
			g2.setColor(borderColor);
			g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);

			g2.dispose();
		}

		@Override
		public boolean contains(int x, int y) {
			if (shape == null || !shape.getBounds().equals(getBounds())) {
				shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
			}
			return shape.contains(x, y);
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Demo Custom Spinner");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(400, 200);
			frame.setLayout(new GridBagLayout()); // Căn giữa màn hình
			frame.getContentPane().setBackground(Color.WHITE);

			frame.add(new CustomQuantitySpinner());

			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}
}