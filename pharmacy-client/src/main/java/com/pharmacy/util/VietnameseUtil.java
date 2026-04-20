package com.pharmacy.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class VietnameseUtil {

	public static String removeAccents(String s) {
		if (s == null)
			return null;
		String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		return pattern.matcher(temp).replaceAll("").replace("đ", "d").replace("Đ", "D");
	}

	public static String convertNumberToTextVND(long number) {
		if (number == 0)
			return "Không đồng";

		String[] units = { "", "nghìn", "triệu", "tỷ" };
		StringBuilder result = new StringBuilder();

		int unitIndex = 0;

		while (number > 0) {
			int group = (int) (number % 1000);
			String groupText = readThreeDigits(group);

			if (!groupText.isEmpty()) {
				if (unitIndex > 0) {
					result.insert(0, groupText + " " + units[unitIndex] + " ");
				} else {
					result.insert(0, groupText + " ");
				}
			}

			number /= 1000;
			unitIndex++;
		}

		String text = result.toString().trim();
		text = Character.toUpperCase(text.charAt(0)) + text.substring(1) + " đồng";

		return text.replaceAll("\\s+", " ");
	}

	private static String readThreeDigits(int number) {
		String[] numText = { "không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín" };

		int hundred = number / 100;
		int ten = (number / 10) % 10;
		int unit = number % 10;

		StringBuilder sb = new StringBuilder();

		if (hundred > 0)
			sb.append(numText[hundred]).append(" trăm");

		if (ten > 1)
			sb.append(hundred > 0 ? " " : "").append(numText[ten]).append(" mươi");
		else if (ten == 1)
			sb.append(hundred > 0 ? " " : "").append("mười");
		else if (ten == 0 && unit > 0 && hundred > 0)
			sb.append(" lẻ");

		if (unit > 0) {
			if (ten == 0 || ten == 1) {
				sb.append(" ").append(numText[unit]);
			} else {
				if (unit == 1)
					sb.append(" mốt");
				else if (unit == 5)
					sb.append(" lăm");
				else
					sb.append(" ").append(numText[unit]);
			}
		}

		return sb.toString().trim();
	}

}
