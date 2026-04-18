package com.pharmacy.view.auth;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import com.pharmacy.shared.service.AuthService;
import com.pharmacy.util.ClientContext;
import raven.modal.Toast;

public class ForgotPassword extends JDialog {

	private JLabel lblTitle;
	private JLabel lblMaNV;
	private JLabel lblEmail;
	private JTextField txtMaNV;
	private JTextField txtEmail;
	private JButton btnConfirm;
	private JPanel pnl;

	private AuthService authService;

	private VerifyOtpDialog verifyOtpDialog;

	public ForgotPassword(JFrame owner) {
		super(owner, true);
		authService = ClientContext.getService(AuthService.class);
		initComponents();
	}

	private void initComponents() {
		this.setTitle("^-^");
		this.setLayout(new BorderLayout(10, 10));

		pnl = new JPanel();
		Box b = Box.createVerticalBox();

		JPanel pnlTitle = new JPanel();
		lblTitle = new JLabel("QUÊN MẬT KHẨU", JLabel.CENTER);
		lblTitle.setFont(new Font("Arial", Font.BOLD, 26));
		lblTitle.setPreferredSize(new Dimension(300, 35));
		pnlTitle.add(lblTitle);

		JPanel pnlMa = new JPanel(new FlowLayout(FlowLayout.LEFT));
		lblMaNV = new JLabel("Mã nhân viên");
		pnlMa.add(lblMaNV);

		JPanel pnlEmail = new JPanel(new FlowLayout(FlowLayout.LEFT));
		lblEmail = new JLabel("Email");
		pnlEmail.add(lblEmail);

		txtMaNV = new JTextField();
		txtMaNV.setPreferredSize(new Dimension(200, 35));

		txtEmail = new JTextField();
		txtEmail.setPreferredSize(new Dimension(200, 35));

		JPanel pnlFoot = new JPanel();
		btnConfirm = new JButton();
		btnConfirm.setText("Xác nhận");
		btnConfirm.setPreferredSize(new Dimension(150, 30));
		btnConfirm.setBackground(new Color(255, 198, 46));
		pnlFoot.add(btnConfirm);

		b.add(Box.createVerticalStrut(40));
		b.add(pnlTitle);

		b.add(Box.createVerticalStrut(30));
		b.add(pnlMa);

		b.add(Box.createVerticalStrut(10));
		b.add(txtMaNV);

		b.add(Box.createVerticalStrut(20));
		b.add(pnlEmail);

		b.add(Box.createVerticalStrut(10));
		b.add(txtEmail);

		b.add(Box.createVerticalStrut(40));
		b.add(pnlFoot);

		btnConfirm.addActionListener(e -> confirm());

		pnl.add(b);

		this.add(pnl, BorderLayout.CENTER);
		this.setSize(450, 480);
		this.setLocationRelativeTo(null);
	}

	private void confirm() {
		String empId = txtMaNV.getText().trim();
		String email = txtEmail.getText().trim();

		if (empId.isEmpty() || email.isEmpty()) {
			Toast.show(this, Toast.Type.WARNING, "Vui lòng điền đầy đủ thông tin.");
			txtMaNV.requestFocus();
			return;
		}

		if (!empId.matches("^ALA01[0-9]{4}$")) {
			Toast.show(this, Toast.Type.ERROR, "Mã nhân viên không đúng định dạng.");
			txtMaNV.requestFocus();
			return;
		}

		if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
			Toast.show(this, Toast.Type.ERROR, "Email không hợp lệ.");
			txtEmail.requestFocus();
			return;
		}

		JDialog loadingDialog = createLoadingDialog();

		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				authService.forgotPassword(empId, email);
				return null;
			}

			@Override
			protected void done() {
				loadingDialog.dispose();

				try {
					get();

					JOptionPane.showMessageDialog(rootPane, "OTP đã được gửi tới email của bạn.");

					verifyOtpDialog = new VerifyOtpDialog(ForgotPassword.this, empId);
					setVisible(false);
					verifyOtpDialog.setVisible(true);

				} catch (java.util.concurrent.ExecutionException e) {
					Throwable cause = e.getCause();

					if (cause instanceof RuntimeException) {
						JOptionPane.showMessageDialog(rootPane, cause.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
						txtMaNV.requestFocus();
					} else {
						JOptionPane.showMessageDialog(rootPane, "Lỗi hệ thống: " + cause.getMessage());
						cause.printStackTrace();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};

		worker.execute();

		loadingDialog.setVisible(true);
	}

	private JDialog createLoadingDialog() {
		JDialog dialog = new JDialog(this, "Đang xử lý...", true);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setSize(300, 80);
		dialog.setLocationRelativeTo(this);
		dialog.setLayout(new BorderLayout());

		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);

		JLabel lblMessage = new JLabel("Đang gửi mã OTP, vui lòng chờ...", JLabel.CENTER);
		lblMessage.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

		dialog.add(lblMessage, BorderLayout.NORTH);
		dialog.add(progressBar, BorderLayout.CENTER);

		return dialog;
	}

}
