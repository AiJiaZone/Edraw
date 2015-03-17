package com.android.edraw.view.drawer;

import android.graphics.Bitmap;
import android.view.SurfaceHolder;

public interface Drawer {
	void draw(Bitmap bitmap, SurfaceHolder holder);
	void notifyDataChanged(String type, Object object);
}
