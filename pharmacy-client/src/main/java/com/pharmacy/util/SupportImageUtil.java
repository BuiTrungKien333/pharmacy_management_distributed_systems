package com.pharmacy.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
public class SupportImageUtil {

	public static void cleanupTempFiles() {
		try {
			File rootDir = new File(PathUtil.getAppPath());
			File tempDir = new File(rootDir, "temp_images");
			if (tempDir.exists() && tempDir.isDirectory()) {
				File[] tempFiles = tempDir.listFiles();
				if (tempFiles != null) {
					for (File file : tempFiles) {
						if (file.isFile() && file.getName().startsWith("temp_")) {
							boolean deleted = file.delete();
							if (deleted)
								log.info("[Util] Deleted temp file: {}", file.getName());
							else
								log.debug("[Util] Failed to delete temp file: {}", file.getName());
						}
					}
				}
			}
		} catch (Exception ex) {
			log.debug("[Util] Error cleaning up temp files: {}", ex.getMessage());
			ex.printStackTrace();
		}
	}

	public static void copyFile(File source, File destination) throws IOException {
		try (InputStream in = new FileInputStream(source); OutputStream out = new FileOutputStream(destination)) {

			byte[] buffer = new byte[1024];
			int length;
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}
		}
	}

	public static String getFileExtension(String fileName) {
		int lastDot = fileName.lastIndexOf('.');
		if (lastDot > 0)
			return fileName.substring(lastDot + 1).toLowerCase();

		return "png";
	}

}
