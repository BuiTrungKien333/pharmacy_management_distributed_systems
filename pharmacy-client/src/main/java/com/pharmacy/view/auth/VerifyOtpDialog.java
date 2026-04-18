package com.pharmacy.view.auth;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


import com.pharmacy.shared.service.AuthService;
import com.pharmacy.util.ClientContext;

public class VerifyOtpDialog extends JDialog {

	private JLabel lblTitle;
	private JTextField txtOtp;
	private JButton btnVerify;
	private JButton btnCancel;

	private String empId;
	private ForgotPassword forgotPassword;

	private AuthService authService;

	public VerifyOtpDialog(ForgotPassword forgotPassword, String empId) {
		super(forgotPassword, "Xác minh OTP", true);
		this.forgotPassword = forgotPassword;
		this.empId = empId;
		this.authService = ClientContext.getService(AuthService.class);
		initComponents();
	}

	private void initComponents() {
		this.setLayout(new BorderLayout(10, 10));

		JPanel pnlMain = new JPanel();
		Box b = Box.createVerticalBox();

		JPanel pnlTitle = new JPanel();
		lblTitle = new JLabel("NHẬP MÃ OTP");
		lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
		pnlTitle.add(lblTitle);

		txtOtp = new JTextField();
		txtOtp.setPreferredSize(new Dimension(200, 35));

		btnVerify = new JButton("Xác nhận");
		btnVerify.setBackground(new Color(46, 204, 113));
		btnVerify.setPreferredSize(new Dimension(100, 30));

		btnCancel = new JButton("Hủy");
		btnCancel.setBackground(new Color(231, 76, 60));
		btnCancel.setPreferredSize(new Dimension(100, 30));

		b.add(Box.createVerticalStrut(30));
		b.add(pnlTitle);
		b.add(Box.createVerticalStrut(30));
		b.add(txtOtp);
		b.add(Box.createVerticalStrut(40));

		JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
		pnlBtn.add(btnVerify);
		pnlBtn.add(btnCancel);
		b.add(pnlBtn);

		pnlMain.add(b);

		this.add(pnlMain, BorderLayout.CENTER);
		this.setSize(400, 300);
		this.setLocationRelativeTo(null);

		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				forgotPassword.dispose();
			}
		});

		btnVerify.addActionListener(e -> verifyOtp());

		btnCancel.addActionListener(e -> {
			dispose();
			forgotPassword.setVisible(true);
		});
	}

	private void verifyOtp() {
		String otpInput = txtOtp.getText().trim();

		if (!otpInput.matches("^[0-9]{6}$")) {
			JOptionPane.showMessageDialog(this, "Mã OTP phải gồm 6 chữ số.");
			txtOtp.requestFocus();
			return;
		}

		try {
			authService.verifyOTP(empId, otpInput);

			JOptionPane.showMessageDialog(this, "Xác minh thành công. Vui lòng đổi mật khẩu.");
			forgotPassword.dispose();
			this.dispose();

			// Mở form đổi mật khẩu
			ChangePasswordDialog changePwd = new ChangePasswordDialog(empId);
			changePwd.setVisible(true);
			
		} catch (RuntimeException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

}
