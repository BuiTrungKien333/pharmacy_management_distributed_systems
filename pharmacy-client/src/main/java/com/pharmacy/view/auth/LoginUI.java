package com.pharmacy.view.auth;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatClientProperties;

import com.pharmacy.app.ClientApp;
import com.pharmacy.shared.service.AuthService;
import com.pharmacy.util.ClientContext;
import raven.modal.Toast;

public class LoginUI extends JPanel {

	private final AuthService authService;

	private MyTextField txtUser;

	private MyPasswordField txtPass;

	private ForgotPassword forgotPassword;

	private Button btnLogin;

	private JButton btnForgot;

	public LoginUI() {
		authService = ClientContext.getService(AuthService.class);
		initComponents();
	}
	
	@Override
	public void addNotify() {
	    super.addNotify();
	    setupDefaultButton();
	}

	private void setupDefaultButton() {
		JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

		if (parentFrame != null) {
			parentFrame.getRootPane().setDefaultButton(btnLogin);
		}
	}

	private void initComponents() {
		setSize(1000, 700);
		this.setLayout(new BorderLayout());

		ImageIcon backgroundIcon = new ImageIcon(getClass().getResource("/images/login.jpg"));
		Image backgroundImage = backgroundIcon.getImage();

		JPanel pnl = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
			}
		};

		pnl.setLayout(null);

		JLabel lblLogin = new JLabel("Welcome to Alamy!");
		lblLogin.setFont(new Font("Arial", Font.BOLD, 30));
		lblLogin.setForeground(new Color(0, 153, 255));
		lblLogin.setBounds(360, 140, 400, 40); // int x, int y, int width, int height
		pnl.add(lblLogin);

		// Username
		JLabel lblUser = new JLabel("Username:");
		lblUser.setBounds(350, 200, 100, 30);
		lblUser.setForeground(Color.BLACK);
		pnl.add(lblUser);

		txtUser = new MyTextField();
		txtUser.setPrefixIcon(new ImageIcon(getClass().getResource("/icon/png/user.png")));
		txtUser.setHint("Username");
		txtUser.setBounds(350, 240, 290, 40);
		pnl.add(txtUser);

		// Password
		JLabel lblPass = new JLabel("Password:");
		lblPass.setBounds(350, 290, 100, 30);
		lblPass.setForeground(Color.BLACK);
		pnl.add(lblPass);

		txtPass = new MyPasswordField();
		txtPass.setPrefixIcon(new ImageIcon(getClass().getResource("/icon/png/pass.png")));
		txtPass.setHint("Password");
		txtPass.setBounds(350, 330, 290, 40);
		txtPass.putClientProperty(FlatClientProperties.STYLE, "" + "showRevealButton:true;" + "showCapsLock:true");
		txtPass.setForeground(Color.decode("#4d4d4d"));
		pnl.add(txtPass);

		// Button Forgot
		btnForgot = new JButton("Quên mật khẩu");
		btnForgot.setForeground(new Color(100, 100, 100));
		btnForgot.setFont(new Font("sansserif", Font.BOLD, 12));
		btnForgot.setContentAreaFilled(false);
		btnForgot.setBorderPainted(false);
		btnForgot.setFocusPainted(false);
		btnForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnForgot.setBounds(530, 380, 120, 25);
		pnl.add(btnForgot);

		// Button Login
		btnLogin = new Button();
		btnLogin.setText("Đăng nhập");
		btnLogin.setBackground(new Color(0, 153, 255));
		btnLogin.setBounds(410, 430, 160, 35);
		pnl.add(btnLogin);

		btnLogin.addActionListener(e -> login());
		btnForgot.addActionListener(e -> forgotPassword());

		this.add(pnl, BorderLayout.CENTER);
	}

	private void forgotPassword() {
		int option = JOptionPane.showConfirmDialog(this, "Xác nhận quên mật khẩu?", "Warning",
				JOptionPane.YES_NO_OPTION);

		if (option == JOptionPane.YES_OPTION) {
			JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
			JPanel glass = new JPanel() {
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					Graphics2D g2 = (Graphics2D) g.create();
					g2.setColor(new Color(0, 0, 0, 100));
					g2.fillRect(0, 0, getWidth(), getHeight());
					g2.dispose();
				}
			};

			glass.setOpaque(false);

			frame.setGlassPane(glass);
			glass.setVisible(true);

			forgotPassword = new ForgotPassword(frame);
			forgotPassword.setVisible(true);

			glass.setVisible(false);
		}
	}

	public void resetLogin() {
		txtUser.setText("");
		txtPass.setText("");
		txtUser.requestFocus();
	}

	public void login() {
		String user = txtUser.getText();
		String pass = new String(txtPass.getPassword());

		JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

		try {
			authService.login(user, pass);

			Arrays.fill(pass.toCharArray(), '\0');
			pass = null;

			if (currentFrame != null)
				Toast.show(currentFrame, Toast.Type.SUCCESS, "Đăng nhập thành công");

			ClientApp.loginSuccess();

		} catch (IllegalArgumentException e) {
			txtUser.requestFocus();

			if (currentFrame != null && currentFrame.isDisplayable())
				Toast.show(currentFrame, Toast.Type.WARNING, e.getMessage());
			else
				JOptionPane.showMessageDialog(null, e.getMessage());
		}
	}
}