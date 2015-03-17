package com.android.edraw.view.file;

import java.io.File;

import android.os.Environment;

public interface EdrawEvent {
	public final static String ROOT_DIR = Environment.getExternalStorageDirectory()
			.getPath() + File.separator + "Edraw";
	
	public final static String TMP_DIR = ROOT_DIR + File.separator + ".temp";
	
	enum FILE_EVENT {
		SAVE(0x01), DELETE(0x10);
		int action;
		public int getAction() {
			return action;
		}
		FILE_EVENT(int action) {
			this.action = action;
		}
	}
	enum UI_EVENT {
		UPDATE(0), DRAW(1), INVALIDATE(2), CLEAR(3);
		int action;
		public int getAction() {
			return action;
		}
		UI_EVENT(int action) {
			this.action = action;
		}
	}
	
	enum DRAWER_STYLE {
		NORMAL("Normal"), MIRROR("Mirror");
		String style;
		public String toString() {
			return style;
		}
		DRAWER_STYLE(String style) {
			this.style = style;
		}
	}
}
