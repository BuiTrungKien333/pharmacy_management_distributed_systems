package com.pharmacy.view.auth;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import com.formdev.flatlaf.FlatClientProperties;
import com.pharmacy.shared.service.AuthService;
import com.pharmacy.util.ClientContext;

public class ChangePasswordDialog extends JDialog {

	private JLabel lblTitle;
	private JLabel lblMaNV;
	private JLabel lblEmail;
	private JPasswordField txtPass;
	private JPasswordField txtNewPass;
	private JButton btnConfirm;
	private JPanel pnl;

	private AuthService authService;
	private String empId;

	public ChangePasswordDialog(String empId) {
		this.empId = empId;
		this.authService = ClientContext.getService(AuthService.class);
		initComponents();
	}

	private void initComponents() {
		this.setTitle("^-^");
		this.setLayout(new BorderLayout(10, 10));

		pnl = new JPanel();
		Box b = Box.createVerticalBox();

		JPanel pnlTitle = new JPanel();
		lblTitle = new JLabel("ĐỔI MẬT KHẨU", JLabel.CENTER);
		lblTitle.setFont(new Font("Arial", Font.BOLD, 26));
		lblTitle.setPreferredSize(new Dimension(300, 35));
		pnlTitle.add(lblTitle);

		JPanel pnlMa = new JPanel(new FlowLayout(FlowLayout.LEFT));
		lblMaNV = new JLabel("New password");
		pnlMa.add(lblMaNV);

		JPanel pnlEmail = new JPanel(new FlowLayout(FlowLayout.LEFT));
		lblEmail = new JLabel("Confirm new password");
		pnlEmail.add(lblEmail);

		txtPass = new JPasswordField();
		txtPass.setPreferredSize(new Dimension(200, 35));
		txtPass.putClientProperty(FlatClientProperties.STYLE, "" + "showRevealButton:true;" + "showCapsLock:true");

		txtNewPass = new JPasswordField();
		txtNewPass.setPreferredSize(new Dimension(200, 35));
		txtNewPass.putClientProperty(FlatClientProperties.STYLE, "" + "showRevealButton:true;" + "showCapsLock:true");

		JPanel pnlFoot = new JPanel();
		btnConfirm = new JButton();
		btnConfirm.setText("Xác nhận");
		btnConfirm.setPreferredSize(new Dimension(150, 35));
		btnConfirm.setBackground(new Color(0, 153, 255));
		pnlFoot.add(btnConfirm);

		b.add(Box.createVerticalStrut(40));
		b.add(pnlTitle);

		b.add(Box.createVerticalStrut(30));
		b.add(pnlMa);

		b.add(Box.createVerticalStrut(10));
		b.add(txtPass);

		b.add(Box.createVerticalStrut(20));
		b.add(pnlEmail);

		b.add(Box.createVerticalStrut(10));
		b.add(txtNewPass);

		b.add(Box.createVerticalStrut(40));
		b.add(pnlFoot);

		btnConfirm.addActionListener(e -> change());

		pnl.add(b);

		this.add(pnl, BorderLayout.CENTER);
		this.setSize(450, 480);
		this.setLocationRelativeTo(null);
	}

	private void change() {
		String pass = new String(txtPass.getPassword());
		String confirmPass = new String(txtNewPass.getPassword());

		if (pass.isBlank() || !pass.equals(confirmPass)) {
			JOptionPane.showMessageDialog(this, "Mật khẩu không hợp lệ.");
			txtPass.requestFocus();
			return;
		}

		authService.changePassword(empId, pass);

		JOptionPane.showMessageDialog(this, "Mật khẩu đã được thay đổi.");
		this.dispose();
	}

}
