package com.android.edraw.view.drawer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.view.SurfaceHolder;
import android.view.View;

import com.android.edraw.view.PointerState;

class NormalDrawer implements Drawer {
	int mColor;
	int mAlpha;
	float mRadius;
	View mView;
	Map<String, Object> mPaintsValues = new HashMap<String, Object>();
	ArrayList<PointerState> mPointers = new ArrayList<PointerState>();
	Paint mPaint = new Paint();

	public NormalDrawer(View view) {
		mView = view;
		mPaintsValues.put("color", 0xFFFFFFFF);
		mPaintsValues.put("alpha", 0xFF);
		mPaintsValues.put("radius", (float) 1.0);
		mPaintsValues.put("paint", mPaint);
	}

	@Override
	public void notifyDataChanged(String type, Object object) {
		// TODO Auto-generated method stub
		mPaintsValues.put(type, object);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void draw(final Bitmap bitmap, final SurfaceHolder holder) {
		// TODO Auto-generated method stub
		mColor = (Integer) mPaintsValues.get("color");
		mAlpha = (Integer) mPaintsValues.get("alpha");
		mRadius = (Float) mPaintsValues.get("radius");
		mPaint = (Paint) mPaintsValues.get("paint");
		mPointers = (ArrayList<PointerState>) mPaintsValues.get("pointstate");
		new Thread(new Runnable() {
			@Override
			public void run() {
				drawPath(bitmap, holder);
			}
		}).start();

	}

	public void drawPath(Bitmap mBitmap, SurfaceHolder mHolder) {
		if (mHolder == null)
			return;
		Canvas surfaceCanvas = mHolder.lockCanvas();
		if (surfaceCanvas == null) {
			return;
		}
		Canvas canvas = new Canvas(mBitmap);
		mPaint.setColor(mColor);
		mPaint.setXfermode(new PorterDuffXfermode(Mode.SRC));
		mPaint.setStyle(Style.FILL);
		mPaint.setStrokeWidth(mRadius);
		mPaint.setAntiAlias(true);
		Iterator<PointerState> iter = mPointers.iterator();
		while (iter.hasNext()) {
			PointerState pState = (PointerState) iter.next();
			float lastX = -1, lastY = -1;
			boolean haveLast = false;
			int N = pState.mTraceCount;
			for (int i = 0; i < N; i++) {
				float x = pState.mTraceX[i];
				float y = pState.mTraceY[i];
				if (Float.isNaN(x)) {
					haveLast = false;
					continue;
				}
				if (haveLast) {
					canvas.drawLine(lastX, lastY, x, y, mPaint);
					canvas.drawCircle(lastX, lastY, mRadius / 2, mPaint);
				}
				lastX = x;
				lastY = y;
				haveLast = true;
			}
		}

		Paint paint = new Paint();
		paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
		surfaceCanvas.drawPaint(paint);
		mPaint.setAlpha(mAlpha);
		surfaceCanvas.drawBitmap(mBitmap, mView.getMatrix(), mPaint);
		mHolder.unlockCanvasAndPost(surfaceCanvas);
	}

}
