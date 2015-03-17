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

class MirrorDrawer implements Drawer {
	int mColor;
	int mAlpha;
	float mRadius;
	View mView;

	static private final int NORMAL = 0x01;
	static private final int MIRROR = 0x02;
	static private final int FILL = 0x04;
	static private final int STROKE = 0x08;
	static private final int MIRROR_FILL = MIRROR | FILL;
	static private final int MIRROR_FILL_STROKE = MIRROR_FILL | STROKE;
	private int mDrawerModel = NORMAL | MIRROR;

	Map<String, Object> mPaintsValues = new HashMap<String, Object>();
	ArrayList<PointerState> mPointers = new ArrayList<PointerState>();
	static Paint mPaint = new Paint();

	public MirrorDrawer(View view) {
		mView = view;
		mPaintsValues.put("color", 0xFFFFFFFF);
		mPaintsValues.put("alpha", 0xFF);
		mPaintsValues.put("radius", (float) 1.0);
		mPaintsValues.put("paint", mPaint);
		mPaintsValues.put("style", mDrawerModel);
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
		mDrawerModel = (Integer) mPaintsValues.get("style");
		// mDrawerModel |= MIRROR;

		new Thread(new Runnable() {
			@Override
			public void run() {
				drawPath(bitmap, holder);
			}
		}).start();

	}

	public void drawPath(Bitmap mBitmap, SurfaceHolder mHolder) {
		synchronized (mPointers) {
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
			float xMidddle = (mView.getX() + mView.getWidth()) / 2;
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
						float temp = mRadius;

						if ((mDrawerModel & STROKE) != 0) {
							mRadius += 10;
							mPaint.setStrokeWidth(mRadius);
						}
						canvas.drawLine(lastX, lastY, x, y, mPaint);
						canvas.drawCircle(lastX, lastY, mRadius / 2, mPaint);
						float delta1 = xMidddle - lastX;
						float delta2 = xMidddle - x;
						int rate1 = delta1 < 0 ? -1 : 1;
						int rate2 = delta2 < 0 ? -1 : 1;

						if ((mDrawerModel & MIRROR) != 0) {
							canvas.drawLine(
									xMidddle + Math.abs(delta1) * rate1, lastY,
									xMidddle + Math.abs(delta2) * rate2, y,
									mPaint);
							canvas.drawCircle(xMidddle + Math.abs(delta1)
									* rate1, lastY, mRadius / 2, mPaint);
						}
						if ((mDrawerModel & FILL) != 0) {
							canvas.drawLine(
									xMidddle + Math.abs(delta1) * rate1, lastY,
									lastX, lastY, mPaint);
						}
						mRadius = temp;
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

}
