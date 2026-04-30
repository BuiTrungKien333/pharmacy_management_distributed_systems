package com.pharmacy.util;

import java.awt.Image;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelperImageIcon {

	private static final String DEFAULT_IMAGE = "/images/prod/default.png";

	private static final Map<String, ImageIcon> cache = new HashMap<>();

	private static String getExternalImageDir() {
		return PathUtil.getAppPath() + File.separator + "images" + File.separator + "prod" + File.separator;
	}

	public static ImageIcon scaleIcon(String fileName, int width, int height) {
		String key = fileName + "_" + width + "x" + height;
		if (cache.containsKey(key))
			return cache.get(key);

		ImageIcon icon = loadImage(fileName);
		if (icon == null)
			icon = loadDefaultImage();

		if (icon != null) {
			try {
				Image image = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
				ImageIcon scaled = new ImageIcon(image);
				cache.put(key, scaled);
				return scaled;
			} catch (Exception e) {
				log.error("Error scaling image: {} to size {}x{}", fileName, width, height, e);
			}
		}

		return null;
	}

	private static ImageIcon loadImage(String fileNameOrPath) {
		if (fileNameOrPath == null || fileNameOrPath.trim().isEmpty())
			return null;

		try {
			File file = new File(fileNameOrPath);
			if (file.exists() && file.isAbsolute())
				return new ImageIcon(fileNameOrPath);

			File externalFile = new File(getExternalImageDir() + fileNameOrPath);
			if (externalFile.exists())
				return new ImageIcon(externalFile.getAbsolutePath());

			String resourcePath = "/images/prod/" + fileNameOrPath;
			java.net.URL resource = HelperImageIcon.class.getResource(resourcePath);
			if (resource != null)
				return new ImageIcon(resource);

			if (fileNameOrPath.startsWith("/")) {
				resource = HelperImageIcon.class.getResource(fileNameOrPath);
				if (resource != null)
					return new ImageIcon(resource);
			}

		} catch (Exception e) {
			log.error("Error loading image from path/name: {}", fileNameOrPath, e);
		}

		return null;
	}

	private static ImageIcon loadDefaultImage() {
		try {
			java.net.URL defaultResource = HelperImageIcon.class.getResource(DEFAULT_IMAGE);
			if (defaultResource != null)
				return new ImageIcon(defaultResource);
		} catch (Exception e) {
			log.error("Error loading default image: {}", DEFAULT_IMAGE, e);
		}
		return null;
	}

	public static void clearCacheForImage(String fileName) {
		if (fileName == null)
			return;

		cache.keySet().removeIf(key -> key.startsWith(fileName + "_"));
	}
}