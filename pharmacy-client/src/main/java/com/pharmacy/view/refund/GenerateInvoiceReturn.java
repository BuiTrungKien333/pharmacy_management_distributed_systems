package com.pharmacy.view.refund;

import com.formdev.flatlaf.FlatClientProperties;
import com.pharmacy.shared.dto.response.InvoiceDetailResponse;
import com.pharmacy.shared.dto.response.InvoiceRefundResponse;
import com.pharmacy.util.FormatUtil;
import com.pharmacy.util.QRGenerator;
import com.pharmacy.util.VietnameseUtil;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class GenerateInvoiceReturn extends JDialog {

	private JPanel pnlInvoice;
	private JPanel pnlHeader;
	private JPanel pnlBody;
	private JPanel pnlFooter;
	private JPanel pnlBodyCenter;
	private JPanel pnlBodyPayment;
	private JPanel pnlBodyInfo;
	private JLabel lblValueMaHD;
	private JLabel lblValueNgayLap;
	private JLabel lblValueSDT;
	private JLabel lblValueTenKH;
	private JLabel lblValueNhanVien;
	private JPanel pnlBodyCenterBody;
	private JLabel lblMoney;
	private JLabel lblDesc;
	private JLabel lblQRCode;

	private static final int WIDTH = 460;

	private static final int HEIGHT = 800;

	public GenerateInvoiceReturn(Frame parent, boolean modal) {
		super(parent, "Hóa đơn trả hàng", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		pnlInvoice = new JPanel(new BorderLayout());
		pnlInvoice.setBackground(Color.WHITE);

		pnlHeader = new JPanel();
		pnlBody = new JPanel();
		pnlFooter = new JPanel();

		pnlHeader.setBackground(Color.WHITE);
		pnlBody.setBackground(Color.WHITE);
		pnlFooter.setBackground(Color.WHITE);

		initComponentHeader();
		initComponentBody();
		initComponentFooter();

		pnlInvoice.add(pnlHeader, BorderLayout.NORTH);
		pnlInvoice.add(pnlBody, BorderLayout.CENTER);
		pnlInvoice.add(pnlFooter, BorderLayout.SOUTH);

		JScrollPane scrollPane = new JScrollPane(pnlInvoice, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setBorder(null);
		scrollPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		scrollPane.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE,
				"width:5;" + "track:#FFFFFF;" + "thumb:#F2F2F2;" + "trackArc:999;" + "thumbArc:999;");

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		pack();

	}

	public void setInitData(InvoiceRefundResponse invoiceReturn, List<InvoiceDetailResponse> listChosen) {
		lblValueMaHD.setText(invoiceReturn.getReturnInvoiceCode());
		lblValueNgayLap.setText(FormatUtil.formatDate(invoiceReturn.getCreatedDate()));
		lblValueNhanVien.setText(invoiceReturn.getEmployee().getDisplayName());
		lblMoney.setText(FormatUtil.formatVND(invoiceReturn.getRefundAmount()));

		String text = "<html><p style='text-align:right;'>"
				+ VietnameseUtil.convertNumberToTextVND((long) invoiceReturn.getRefundAmount()) + "</p></html>";

		lblDesc.setText(text);

		String sdt = invoiceReturn.getCustomer().getPhoneNumber();
		lblValueSDT.setText(sdt.substring(0, 4) + "***" + sdt.substring(7));
		lblValueTenKH.setText(invoiceReturn.getCustomer().getFullName());

		showChiTietHoaDon(listChosen);

		try {
			BufferedImage img = QRGenerator.generateQRCodeImage(invoiceReturn.getReturnInvoiceCode(), 120, 120);
			lblQRCode.setIcon(new ImageIcon(img));
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.getMessage());
		}
	}

	private void showChiTietHoaDon(List<InvoiceDetailResponse> list) {
		pnlBodyCenterBody.setVisible(false);
		pnlBodyCenterBody.removeAll();

		list.forEach(item -> pnlBodyCenterBody.add(initProdDetail(item)));

		pnlBodyCenterBody.revalidate();
		pnlBodyCenterBody.repaint();
		pnlBodyCenterBody.setVisible(true);

	}

	private void initComponentHeader() {
		pnlHeader.setLayout(new MigLayout("fillx, wrap", "[grow]", "[]5[]5[]10[]"));

		JLabel lblTitle = new JLabel("NHÀ THUỐC ALAMI");
		lblTitle.setFont(new Font("Roboto", Font.BOLD, 21));

		JLabel lblAddress = new JLabel("12 Nguyễn Văn Bảo, Phường 5, Gò Vấp, TP.HCM");
		lblAddress.setFont(new Font("Roboto", Font.PLAIN, 13));

		JLabel lblPhone = new JLabel("Điện thoại: 18008989");
		lblPhone.setFont(new Font("Roboto", Font.PLAIN, 13));

		JSeparator line = new JSeparator();
		line.setForeground(new Color(180, 180, 180));

		JLabel lblTitleInvoice = new JLabel("HÓA ĐƠN TRẢ HÀNG");
		lblTitleInvoice.setFont(new Font("Roboto", Font.BOLD, 17));

		pnlHeader.add(lblTitle, "al center, gapbottom 3");
		pnlHeader.add(lblAddress, "al center");
		pnlHeader.add(lblPhone, "al center, gapbottom 8");
		pnlHeader.add(line, "growx, gapbottom 8");
		pnlHeader.add(lblTitleInvoice, "al center");
	}

	private void initComponentBody() {
		pnlBody.setLayout(new BorderLayout());

		pnlBodyInfo = new JPanel();
		initPnlBodyInfo();

		pnlBodyCenter = new JPanel(new BorderLayout());
		JPanel pnlBodyCenterHeader = new JPanel();
		pnlBodyCenterHeader.setBackground(Color.WHITE);
		pnlBodyCenterHeader.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		pnlBodyCenterHeader.setLayout(new MigLayout("wrap, fillx", "[fill][fill][fill][fill][fill]", "[]"));

		JLabel lblTitle1 = new JLabel("Tên thuốc");
		JLabel lblTitle2 = new JLabel("ĐVT");
		JLabel lblTitle3 = new JLabel("SL");
		JLabel lblTitle4 = new JLabel("Đơn giá");
		JLabel lblTitle5 = new JLabel("Tổng tiền");

		pnlBodyCenterHeader.add(lblTitle1);
		pnlBodyCenterHeader.add(lblTitle2);
		pnlBodyCenterHeader.add(lblTitle3);
		pnlBodyCenterHeader.add(lblTitle4);
		pnlBodyCenterHeader.add(lblTitle5);

		pnlBodyCenterBody = new JPanel();
		pnlBodyCenterBody.setBackground(Color.WHITE);
		pnlBodyCenterBody.setLayout(new MigLayout("wrap, fillx, aligny top", "[fill]", ""));

		pnlBodyCenter.add(pnlBodyCenterHeader, BorderLayout.NORTH);
		pnlBodyCenter.add(pnlBodyCenterBody, BorderLayout.CENTER);

		pnlBodyPayment = new JPanel();
		initPnlBodyPayment();

		pnlBody.add(pnlBodyInfo, BorderLayout.NORTH);
		pnlBody.add(pnlBodyCenter, BorderLayout.CENTER);
		pnlBody.add(pnlBodyPayment, BorderLayout.SOUTH);
	}

	private void initPnlBodyInfo() {
		pnlBodyInfo.setLayout(new MigLayout("fillx, wrap", "[][30][]", "[]10[]10[]10[]10[]"));
		pnlBodyInfo.setBackground(Color.WHITE);

		JLabel lblMaHD = new JLabel("Mã HD:");
		lblValueMaHD = new JLabel();
		lblValueMaHD.setFont(new Font("Roboto", Font.PLAIN, 13));
		pnlBodyInfo.add(lblMaHD);
		pnlBodyInfo.add(lblValueMaHD, "span");

		JLabel lblNgayLap = new JLabel("Ngày lập:");
		lblValueNgayLap = new JLabel();
		lblValueNgayLap.setFont(lblValueMaHD.getFont());
		pnlBodyInfo.add(lblNgayLap);
		pnlBodyInfo.add(lblValueNgayLap, "span");

		JLabel lblSDT = new JLabel("SĐT khách hàng:");
		lblValueSDT = new JLabel();
		lblValueSDT.setFont(lblValueMaHD.getFont());
		pnlBodyInfo.add(lblSDT);
		pnlBodyInfo.add(lblValueSDT, "span");

		JLabel lblTenKH = new JLabel("Tên khách hàng:");
		lblValueTenKH = new JLabel();
		lblValueTenKH.setFont(lblValueMaHD.getFont());
		pnlBodyInfo.add(lblTenKH);
		pnlBodyInfo.add(lblValueTenKH, "span");

		JLabel lblNhanVien = new JLabel("Nhân viên:");
		lblValueNhanVien = new JLabel();
		lblValueNhanVien.setFont(lblValueMaHD.getFont());
		pnlBodyInfo.add(lblNhanVien);
		pnlBodyInfo.add(lblValueNhanVien, "span");

	}

	private void initPnlBodyPayment() {
		pnlBodyPayment.setLayout(new MigLayout("fillx, insets 10", "[grow][right]", "[]15[]20[]"));

		JLabel lblTotal = new JLabel("TIỀN HOÀN");
		lblMoney = new JLabel();

		pnlBodyPayment.add(lblTotal);
		pnlBodyPayment.add(lblMoney, "align right, wrap");

		lblTotal.setFont(new Font("Roboto", Font.BOLD, 14));
		lblMoney.setFont(new Font("Roboto", Font.BOLD, 14));

		lblDesc = new JLabel();
		lblDesc.setHorizontalAlignment(SwingConstants.RIGHT);
		pnlBodyPayment.add(lblDesc, "span 2, growx, wrap");

		JLabel lblInf = new JLabel("Quét mã QR để xem thông tin hóa đơn");
		lblQRCode = new JLabel();
		pnlBodyPayment.add(lblInf);
		pnlBodyPayment.add(lblQRCode, "align right");

		pnlBodyPayment.setBackground(Color.WHITE);
	}

	private JPanel initProdDetail(InvoiceDetailResponse refund) {
		JPanel pnl = new JPanel();
		pnl.setLayout(new MigLayout("wrap 5, fillx", "[38%][fill][fill][fill][fill]", "[]"));

		JLabel lblTitle1 = new JLabel(refund.getMedicine().getMedicineName());
		JLabel lblTitle2 = new JLabel(refund.getMedicine().getMeasuringUnit());
		JLabel lblTitle3 = new JLabel(String.valueOf(refund.getQuantity()));
		JLabel lblTitle4 = new JLabel(FormatUtil.formatVND(refund.getUnitPrice()));
		JLabel lblTitle5 = new JLabel(FormatUtil.formatVND(refund.getTotalAmount()));

		pnl.add(lblTitle1);
		pnl.add(lblTitle2);
		pnl.add(lblTitle3);
		pnl.add(lblTitle4);
		pnl.add(lblTitle5);

		JLabel lbls[] = { lblTitle1, lblTitle2, lblTitle3, lblTitle4, lblTitle5 };
		for (JLabel lbl : lbls) {
			lbl.setFont(new Font("Roboto", Font.PLAIN, 13));
		}

		pnl.setBackground(Color.WHITE);
		pnl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

		return pnl;
	}

	private void initComponentFooter() {
		pnlFooter.setLayout(new MigLayout("fillx, wrap", "[]", "[]5[]15[]5[]50"));

		JLabel lblLuuY = new JLabel();
		lblLuuY.setText("LƯU Ý:");

		JLabel lblContentLuuY = new JLabel(
				"<html>Nếu gặp vấn đề về việc hoàn trả sản phẩm, bạn có thể liên hệ bộ phận chăm sóc khách hàng 18008989 để được hỗ trợ. </html>");
		lblContentLuuY.setPreferredSize(new Dimension(410, lblContentLuuY.getPreferredSize().height));

		JLabel lbl1 = new JLabel("<html>Quý khách vui lòng kiểm tra và giữ lại hóa đơn để được hỗ trợ tốt nhất</html>");
		lbl1.setPreferredSize(new Dimension(410, lblContentLuuY.getPreferredSize().height));
		lbl1.setFont(new Font("Roboto", Font.ITALIC, 12));

		JLabel lbl2 = new JLabel("Cảm ơn quý khách");
		lbl2.setFont(new Font("Roboto", Font.BOLD | Font.ITALIC, 13));

		pnlFooter.add(lblLuuY);
		pnlFooter.add(lblContentLuuY);
		pnlFooter.add(lbl1, "al center");
		pnlFooter.add(lbl2, "al center");
	}

}
