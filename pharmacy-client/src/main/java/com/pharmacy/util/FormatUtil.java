package com.pharmacy.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class FormatUtil {

	private static final Locale VIETNAM = new Locale("vi", "VN");

	public static String formatVND(double price) {
		BigDecimal x = new BigDecimal(price);
		@SuppressWarnings("deprecation")
		Locale vietnam = new Locale("vi", "VN");
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(vietnam);
		symbols.setGroupingSeparator('.');
		symbols.setDecimalSeparator(',');
		DecimalFormat formatter = new DecimalFormat("#,##0", symbols);
		return formatter.format(x);
	}

	public static String formatDate(LocalDate date) {
		DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		return date.format(df);
	}

	public static String formatDateDDMMYY(LocalDate date) {
		DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yy");
		return date.format(df);
	}

	public static LocalDate convertStringToDate(String date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		LocalDate localDate = null;
		try {
			localDate = LocalDate.parse(date, formatter);
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("Ngày phải có định dạng dd/MM/yyyy");
		}
		return localDate;
	}

	public static String formatDate(LocalDateTime date) {
		DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
		return date.format(df);
	}

}
