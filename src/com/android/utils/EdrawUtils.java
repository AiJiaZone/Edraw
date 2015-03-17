package com.android.utils;

public final class EdrawUtils {
	final public static String argbColorToHex(int alpha, int red, int green,
			int blue) {
		StringBuilder builder = new StringBuilder();
		builder.append(encodeToHex(alpha));
		builder.append(encodeToHex(red));
		builder.append(encodeToHex(green));
		builder.append(encodeToHex(blue));
		return builder.toString();
	}

	final public static String encodeToHex(int color) {
		StringBuilder builder = new StringBuilder();
		if ((color & 0xFF) < 0x10) {
			builder.append("0");
		}
		builder.append(Long.toHexString(color));
		return builder.toString();
	}
	
	final public static String encodeLongToHex(final int color) {
		StringBuilder builder = new StringBuilder();
		String colorStr = Long.toHexString(color);
		int i = 8 - colorStr.length();
		while(i != 0) {
			builder.append("0");
			i--;
		}
		builder.append(Long.toHexString(color));
		return builder.toString();
	}
	final public static int encodeHexStrToInt(String color) {
		return Integer.getInteger(color, 0);
	}
}
