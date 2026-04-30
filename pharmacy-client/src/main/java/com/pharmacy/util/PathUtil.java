package com.pharmacy.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class PathUtil {

	public static String getAppPath() {
		try {
			File path = new File(PathUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI());

			if (path.isFile()) {
				return path.getParent();
			} else {
				File parent = path.getParentFile();
				return (parent != null) ? parent.getParent() : ".";
			}

		} catch (Exception e) {
			log.error("Failed to determine application path. Defaulting to current directory '.'", e);
			return ".";
		}
	}
}