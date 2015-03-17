package com.android.edraw.view.drawer;

import com.android.edraw.view.file.EdrawEvent;

import android.view.View;

public class DrawerFactory {
	static private Drawer mDrawer;
	private static View mView;

	public DrawerFactory(View view) {
		mView = view;
	}

	static void createDefaultDrawer() {
		if (mView != null)
			mDrawer = new NormalDrawer(mView);
	}

	static public Drawer getNormalDrawer(View view) {
		return new NormalDrawer(view);
	}

	static public Drawer getMirrorDrawer(View view) {
		return new MirrorDrawer(view);
	}

	static public Drawer getDrawer(View view, String type) {
		if (type == null)
			return getNormalDrawer(view);
		if ("normal".equals(type))
			return getNormalDrawer(view);
		if ("mirror".equals(type))
			return getMirrorDrawer(view);
		return getNormalDrawer(view);
	}
	static public Drawer getDrawer(View view, EdrawEvent.DRAWER_STYLE type) {
		if (type == null)
			return getNormalDrawer(view);
		if (EdrawEvent.DRAWER_STYLE.NORMAL.equals(type))
			return getNormalDrawer(view);
		if (EdrawEvent.DRAWER_STYLE.MIRROR.equals(type))
			return getMirrorDrawer(view);
		return getNormalDrawer(view);
	}
}
