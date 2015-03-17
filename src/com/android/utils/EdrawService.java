package com.android.utils;

import java.io.File;

public class EdrawService {

	public static String buildFileName(File edraw) {

		if (!edraw.exists()) {
			edraw.mkdir();
		}
		StringBuilder builder = new StringBuilder();
		File[] files = edraw.listFiles();
		int length = files.length;
		if (length < 1) {
			builder.append("Img000.png");
			return builder.toString();
		}
		int maxNumber = 0;
		String sortNumber = null;
		for (int i = 0; i < length; i++) {
			File lastFile = files[i];
			String lastFileName = lastFile.getName();
			int imgPos = lastFileName.lastIndexOf("Img");
			int subFixPos = lastFileName.lastIndexOf(".");
			if (subFixPos - imgPos <= 3) {
				continue;
			}

			sortNumber = lastFileName.substring(
					lastFileName.lastIndexOf("Img") + 3,
					lastFileName.lastIndexOf("."));
			try {
				int number = Integer.parseInt(sortNumber);
				if (number > maxNumber) {
					maxNumber = number;
				}
			} catch (Exception e) {
				
			}
		}
		builder.append("Img00");

		builder.append(maxNumber + 1);
		builder.append(".png");
		return builder.toString();
	}

	public static String buildFileName(String dirName, String fileName) {
		StringBuilder builder = new StringBuilder();
		builder.append(dirName);
		builder.append(File.separator);
		builder.append(fileName);
		return builder.toString();
	}
}
