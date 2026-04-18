package com.pharmacy.view.splashscreen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class SplashScreenUI extends JDialog {

	private JLabel lblStatus;
	private ProgressBarCustom process;

	public SplashScreenUI(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		initComponents();
	}

	private void initComponents() {
		this.setTitle("^-^");

		ImageIcon icon = new ImageIcon(getClass().getResource("/images/splash_screen.png"));
		Image img = icon.getImage();

		JPanel contentPane = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
			}
		};
		contentPane.setLayout(new BorderLayout());
		this.setContentPane(contentPane);

		lblStatus = new JLabel("Starting...");
		lblStatus.setForeground(Color.GRAY);
		lblStatus.setFont(new Font("sansserif", Font.BOLD, 12));
		lblStatus.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 0));

		process = new ProgressBarCustom();
		process.setPreferredSize(new Dimension(100, 7));

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.setOpaque(false);
		bottomPanel.add(lblStatus, BorderLayout.NORTH);
		bottomPanel.add(process, BorderLayout.SOUTH);

		add(bottomPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setUndecorated(true);
		getRootPane().setBorder(null);
		setBackground(new Color(0, 0, 0, 0));

		setSize(650, 400);
		setLocationRelativeTo(null);
	}

	public void updateProgress(String taskName, int progress) {
		SwingUtilities.invokeLater(() -> {
			lblStatus.setText(taskName);
			process.setValue(progress);
		});
	}
}
