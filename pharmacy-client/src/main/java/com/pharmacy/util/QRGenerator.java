package com.pharmacy.util;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

public class QRGenerator {
	
	public static BufferedImage generateQRCodeImage(String text, int width, int height) throws Exception {
	    Map<EncodeHintType, Object> hints = new HashMap<>();
	    hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

	    BitMatrix matrix = new MultiFormatWriter()
	            .encode(text, BarcodeFormat.QR_CODE, width, height, hints);

	    return MatrixToImageWriter.toBufferedImage(matrix);
	}

}
