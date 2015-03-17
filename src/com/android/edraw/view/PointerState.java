package com.android.edraw.view;

import android.view.MotionEvent.PointerCoords;

public class PointerState {
	public float posX;
	public float posY;
	public int color;
	public PointerCoords mCoords = new PointerCoords();
	public float[] mTraceX = new float[32];
	public float[] mTraceY = new float[32];
	public int mTraceCount = 0;
	public boolean mCursDown = false;
	public float mXVelocity = 0;
	public float mYVelocity = 0;
	public int mToolType = -1;

	public void clearTrace() {
		mTraceCount = 0;
	}

	public void addTrace(float x, float y) {
		int traceCapcity = mTraceX.length;
		if (mTraceCount == traceCapcity) {
			traceCapcity *= 2;
			float[] newTraceX = new float[traceCapcity];
			System.arraycopy(mTraceX, 0, newTraceX, 0, mTraceCount);
			mTraceX = newTraceX;

			float[] newTraceY = new float[traceCapcity];
			System.arraycopy(mTraceY, 0, newTraceY, 0, mTraceCount);
			mTraceY = newTraceY;
		}
		mTraceX[mTraceCount] = x;
		mTraceY[mTraceCount] = y;
		mTraceCount++;
	}
}