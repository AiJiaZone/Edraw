package com.android.edraw.view;

import java.io.File;
import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.VelocityTracker;
import android.view.View;

import com.android.edraw.DrawActivity;
import com.android.edraw.DrawActivity.OnActionButtonClickListener;
import com.android.edraw.view.drawer.Drawer;
import com.android.edraw.view.drawer.DrawerFactory;
import com.android.edraw.view.file.EdrawEvent;
import com.android.edraw.view.file.EdrawEvent.DRAWER_STYLE;
import com.android.edraw.view.file.EdrawEvent.UI_EVENT;
import com.android.edraw.view.file.FileOperationCallbacks;
import com.android.edraw.view.file.FileOperator;
import com.android.utils.EdrawService;

public class DrawerSurfaceView extends SurfaceView {

	private final String TAG = "DrawerSurfaceView";
	ArrayList<PointerState> mPointers = new ArrayList<PointerState>(); // Touch pointer path
	private SurfaceHolder mHolder;
	DrawActivity mContext = null;

	private VelocityTracker mVelocity;
	private boolean mCurDown = false;

	private Bitmap mBitmap = null;
	Paint mPaint = new Paint();
	private int mPaintColor = Color.WHITE;
	private int mAlpha = 255;
	private float mStroke = 0;
	private float mRadius = 1;

	private DRAWER_STYLE mDrawerStyle = EdrawEvent.DRAWER_STYLE.NORMAL;
	private int mFillStyle = 0x01 | 0x02;

	/*
	 * Use mActionCounts to prevent drawing too frequently
	 */
	private AtomicInteger mActionCounts = new AtomicInteger(0);
	/**
	 * The real drawer to draw picture
	 * */
	Drawer mDrawer = null;

	public DrawerSurfaceView(Context context) {
		super(context);
	}

	public DrawerSurfaceView(Context context, AttributeSet attr) {
		super(context, attr);
		mHolder = getHolder();
		mHolder.addCallback(mCallback);

		this.setOnTouchListener(mTouchListener);
		mVelocity = VelocityTracker.obtain();
		mContext = (DrawActivity) context;
		mContext.setActionButtonClickListener(mOnActionButtonClickListener);

		// Build the drawer
		mDrawer = DrawerFactory.getDrawer(this, mDrawerStyle);

		Intent intent = mContext.getIntent();
		if (intent.hasExtra("filename")) {
			String filePath = intent.getStringExtra("filename");
			mFileName = filePath;
		}
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == UI_EVENT.DRAW.getAction()) {
				drawPicture();
			} else if (msg.what == UI_EVENT.UPDATE.getAction()) {
				updatePainter();
			} else if (msg.what == UI_EVENT.INVALIDATE.getAction()) {
				// invalidateSurface();
				new Thread(new InvalidateRunnable()).start();
			} else if (msg.what == UI_EVENT.CLEAR.getAction()) {
				new Thread(new ClearSurfaceRunnable()).start();
			}
		};
	};

	public void setColor(int color) {
		mPaintColor = color;
		sendUpdateMessage(UI_EVENT.UPDATE);
	}

	public void setStroke(float stroke) {
		mStroke = stroke;
		sendUpdateMessage(UI_EVENT.UPDATE);
	}

	public void setRadius(float radius) {
		mRadius = radius;
		sendUpdateMessage(UI_EVENT.UPDATE);
	}

	public void setAlpha(int alpha) {
		mAlpha = alpha;
		sendUpdateMessage(UI_EVENT.UPDATE);
	}

	public void setDrawerType(String type) {
		if (EdrawEvent.DRAWER_STYLE.NORMAL.toString().equals(type)) {
			mDrawerStyle = EdrawEvent.DRAWER_STYLE.NORMAL;
		} else if (EdrawEvent.DRAWER_STYLE.MIRROR.toString().equals(type)) {
			mDrawerStyle = EdrawEvent.DRAWER_STYLE.MIRROR;
		}
		sendUpdateMessage(UI_EVENT.UPDATE);
	}

	public void setDrawerStyle(int style) {
		mFillStyle = style;
		sendUpdateMessage(UI_EVENT.UPDATE);
	}

	private void updatePainter() {
		mPaint.setAlpha(mAlpha);
		mPaint.setColor(mPaintColor);
		mPaint.setStrokeWidth(mRadius);
		mDrawer = DrawerFactory.getDrawer(this, mDrawerStyle);
		if (EdrawEvent.DRAWER_STYLE.MIRROR == mDrawerStyle) {
			mDrawer.notifyDataChanged("style", mFillStyle);
		}
		mDrawer.notifyDataChanged("color", mPaintColor);
		mDrawer.notifyDataChanged("alpha", mAlpha);
		mDrawer.notifyDataChanged("radius", mRadius);
		mDrawer.notifyDataChanged("paint", mPaint);
	}

	private void sendUpdateMessage(UI_EVENT event) {
		Message msg = new Message();
		msg.what = event.getAction();
		mHandler.sendMessage(msg);
	}

	private final void drawPicture() {
		mDrawer.draw(mBitmap, mHolder);
	}

	private final void invalidateSurface() {
		Canvas canvas = mHolder.lockCanvas();
		mPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
		canvas.drawPaint(mPaint);
		mPaint.setXfermode(new PorterDuffXfermode(Mode.SRC));
		canvas.drawBitmap(mBitmap, getMatrix(), mPaint);
		mHolder.unlockCanvasAndPost(canvas);
	}

	private final void clearSurface() {
		Canvas canvas = mHolder.lockCanvas();
		mPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
		canvas.drawBitmap(mBitmap, getMatrix(), mPaint);
		mHolder.unlockCanvasAndPost(canvas);
	}

	class InvalidateRunnable implements Runnable {
		@Override
		public void run() {
			invalidateSurface();
		}
	}

	class ClearSurfaceRunnable implements Runnable {

		@Override
		public void run() {
			synchronized (mActionCounts) {
				try {
					while (mActionCounts.get() > 0) {
						mActionCounts.wait();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			mSaveToNewFile = true;
			mRedoStacks.removeAllElements();
			mUndoStacks.removeAllElements();
			removeCachedPicture();
			mBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
					Config.ARGB_8888);
			clearSurface();
		}
	}

	OnTouchListener mTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// Do not use thread to update the data. It may cause unexpected results
			addPointerEvent(event);
			sendUpdateMessage(UI_EVENT.DRAW);

			if (event.getAction() == MotionEvent.ACTION_DOWN && !isFocused()) {
				requestFocus();
			}
			if (event.getAction() == MotionEvent.ACTION_UP) {
				savePic2CacheDir();
			}

			mDrawCallBack.onDrawCallBack(event);

			return true; // Should return true
		}
	};

	void addPointerEvent(MotionEvent event) {
		int action = event.getAction();
		int NP = mPointers.size();
		if (action == MotionEvent.ACTION_DOWN
				|| (action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN) {
			int index = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			final int id = event.getPointerId(index);
			if (action == MotionEvent.ACTION_DOWN) {
				mCurDown = true;
				// Clear the point when the pointer has up
				for (int i = 0; i < NP; i++) {
					PointerState ps = mPointers.get(i);
					ps.clearTrace();
				}
			}
			while (NP <= id) {
				PointerState pState = new PointerState();
				mPointers.add(pState);
				NP++;
			}

			final PointerState pi = mPointers.get(id);
			pi.mCursDown = true;
		}

		mVelocity.addMovement(event);
		mVelocity.computeCurrentVelocity(1);
		final int NI = event.getPointerCount(); // Get the number of pointers of data in the event
		final int N = event.getHistorySize(); // Get the history points in this event
		PointerCoords tempCoords = new PointerCoords();
		// Add all the pointers' value to data list
		for (int historyPos = 0; historyPos < N; historyPos++) {
			for (int i = 0; i < NI; i++) {
				final int id = event.getPointerId(i);
				final PointerState ps = mCurDown ? mPointers.get(id) : null;
				final PointerCoords coords = ps != null ? ps.mCoords
						: tempCoords;
				event.getHistoricalPointerCoords(i, historyPos, coords);
				if (ps != null) {
					ps.addTrace(coords.x, coords.y);
				}
			}
		}

		for (int i = 0; i < NI; i++) {
			final int id = event.getPointerId(i);
			final PointerState pState = mCurDown ? mPointers.get(id) : null;
			final PointerCoords coords = pState != null ? pState.mCoords
					: tempCoords;
			event.getPointerCoords(i, coords);
			if (pState != null) {
				pState.addTrace(coords.x, coords.y);
				pState.mXVelocity = mVelocity.getXVelocity(id); // Get X velocity
				pState.mYVelocity = mVelocity.getYVelocity(id); // Get Y velocity
				pState.mToolType = event.getToolType(i); // Get tool type
			}
		}

		if (action == MotionEvent.ACTION_UP
				|| action == MotionEvent.ACTION_CANCEL
				|| (action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP) {
			final int index = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			final int id = event.getPointerId(index);
			final PointerState pState = mPointers.get(id);
			pState.mCursDown = false;
			if (action == MotionEvent.ACTION_UP
					|| action == MotionEvent.ACTION_CANCEL) {
				mCurDown = false;
			} else {
				mCurDown = false;
				pState.addTrace(Float.NaN, Float.NaN); // When pointer is up, the touch event is end
			}
		}

		synchronized (mPointers) {
			mDrawer.notifyDataChanged("pointstate", mPointers);
		}
	}

	SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			if (mFileName == null) {
				// Create a bitmap to save the drawn image
				mBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
						Config.ARGB_8888);
			} else {
				mBitmap = BitmapFactory.decodeFile(mFileName).copy(
						Config.ARGB_8888, true);
				savePic2CacheDir();
			}
			sendUpdateMessage(UI_EVENT.INVALIDATE);
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			removeCachedPicture();
		}

	};

	/**
	 * This is called in FileOperation.java When Save or delete is called, this will execute.
	 * */
	FileOperationCallbacks mFileOperationCallbacks = new FileOperationCallbacks() {

		@Override
		public void onSaved() {
			mActionCounts.decrementAndGet();
			synchronized (mActionCounts) {
				if (mActionCounts.get() == 0)
					mActionCounts.notifyAll();
			}
			Log.v(TAG, "onSaved mActionCounts" + mActionCounts.get());
		}

		@Override
		public void onDeleted() {
			mActionCounts.set(0);
		}
	};

	/**
	 * Used to hide or show color picker panel
	 * 
	 * @see DrawActivity
	 * */
	public interface OnDrawCallBack {
		void onDrawCallBack(MotionEvent event);
	}

	private OnDrawCallBack mDrawCallBack;

	public void setOnDrawCallBack(OnDrawCallBack onDrawCallBack) {
		mDrawCallBack = onDrawCallBack;
	}

	private void savePic2CacheDir() {
		File file = new File(EdrawEvent.TMP_DIR);
		if (!file.exists()) {
			file.mkdirs();
		}
		String fileName = EdrawService.buildFileName(file);
		try {
			String filePath = EdrawService.buildFileName(EdrawEvent.TMP_DIR,
					fileName);
			mActionCounts.incrementAndGet();
			Log.v(TAG, "onSaved mActionCounts" + mActionCounts.get());
			synchronized (mBitmap) {
				FileOperator.handle(mFileOperationCallbacks, filePath, mBitmap,
						EdrawEvent.FILE_EVENT.SAVE);
			}
			mUndoStacks.add(fileName);
			mRedoStacks.removeAllElements();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void removeCachedPicture() {
		FileOperator.handle(mFileOperationCallbacks, EdrawEvent.TMP_DIR,
				EdrawEvent.FILE_EVENT.DELETE);
	}

	/**
	 * To mark whether need to save the picture to a new file
	 * */
	private boolean mSaveToNewFile = true;

	/**
	 * Used to create the real file
	 * */
	String mFileName = null;
	/**
	 * Store the middle state of drawn file
	 * */
	String mCurrentDrawnFile = null;
	/**
	 * Store the picture that have been drawn
	 * */
	Stack<String> mUndoStacks = new Stack<String>();
	/**
	 * Store the picture that have popped from {@link mUndoStacks}
	 * */
	Stack<String> mRedoStacks = new Stack<String>();

	private OnActionButtonClickListener mOnActionButtonClickListener = new OnActionButtonClickListener() {

		/**
		 * (1) When {@link mUndoStacks} is empty, two cases may happen: we have draw nothing or we
		 * have stepped back to the original state. (2) When {@link mRedoSatacks} is empty, which
		 * means we have not pressed undo button before.So we must pop mUndoStacks twice to get the
		 * state before the last draw.(3)There is a middle state which is the drawn picture.When
		 * action switch from re-do to undo or undo to re-do, the head file in stacks are the same
		 * with the middle state.So use the {@link mCurrentDrawnFile} to store the middle state.But
		 * if mBitmap is created from surface, it need to be clear.
		 * 
		 * */
		@Override
		public void onUndo() {
			new Thread(new Runnable() {

				@Override
				public void run() {
					synchronized (mActionCounts) {
						if (mActionCounts.get() > 0) {
							// mActionCounts.wait();
							return;
						}
					}

					if (mUndoStacks.isEmpty()) {
						mBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
								Config.ARGB_8888);
						// Must clear current drawn file name
						mCurrentDrawnFile = null;
					} else {
						String fileName = null;
						if (mRedoStacks.isEmpty()) {
							fileName = mUndoStacks.pop();
							mRedoStacks.push(fileName);
							mCurrentDrawnFile = null;
						}
						if (mUndoStacks.isEmpty()) {
							mBitmap = Bitmap.createBitmap(getWidth(),
									getHeight(), Config.ARGB_8888);
							mCurrentDrawnFile = null;
						} else {
							fileName = mUndoStacks.pop();
							mRedoStacks.push(fileName);

							if (mCurrentDrawnFile != null
									&& mCurrentDrawnFile.equals(fileName)
									&& !mUndoStacks.isEmpty()) {
								fileName = mUndoStacks.pop();
								mRedoStacks.push(fileName);
							}
							String filePath = EdrawService.buildFileName(
									EdrawEvent.TMP_DIR, fileName);
							File tmpFile = new File(filePath);
							// mBitmap.recycle();
							if (!tmpFile.exists()) {
								mBitmap = Bitmap.createBitmap(getWidth(),
										getHeight(), Config.ARGB_8888);
								mCurrentDrawnFile = null;
							} else {
								mBitmap = BitmapFactory.decodeFile(filePath)
										.copy(Config.ARGB_8888, true);

								// Save the current drawn file
								mCurrentDrawnFile = fileName;
							}
						}
					}
					sendUpdateMessage(UI_EVENT.INVALIDATE);
				}
			}).start();

		}

		@Override
		public void onRedo() {
			if (mActionCounts.get() > 0)
				return;
			if (mRedoStacks.isEmpty())
				return;
			String fileName = mRedoStacks.pop();
			mUndoStacks.push(fileName);
			if (mCurrentDrawnFile != null && mCurrentDrawnFile.equals(fileName)
					&& !mRedoStacks.isEmpty()) {
				fileName = mRedoStacks.pop();
				mUndoStacks.push(fileName);
			}
			String filePath = EdrawService.buildFileName(EdrawEvent.TMP_DIR,
					fileName);
			File tmpFile = new File(filePath);
			if (!tmpFile.exists()) {
				// mBitmap = Bitmap.createBitmap(getWidth(),getHeight(),Config.ARGB_8888);
				return;
			} else {
				mBitmap = BitmapFactory.decodeFile(filePath).copy(
						Config.ARGB_8888, true);
				mCurrentDrawnFile = fileName;
			}

			sendUpdateMessage(UI_EVENT.INVALIDATE);
		}

		@Override
		public void onSave() {
			String state = Environment.getExternalStorageState();
			if (!Environment.MEDIA_MOUNTED.equals(state)) {
				return;
			}
			File edraw = new File(EdrawEvent.ROOT_DIR);
			if (!edraw.exists()) {
				edraw.mkdir();
			}

			if (mSaveToNewFile == true) {
				mFileName = EdrawService.buildFileName(edraw);
				mSaveToNewFile = false;
			}
			try {
				String fileName = EdrawService.buildFileName(
						EdrawEvent.ROOT_DIR, mFileName);
				FileOperator.handle(mFileOperationCallbacks, fileName, mBitmap,
						EdrawEvent.FILE_EVENT.SAVE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onClear() {
			// int color = getSolidColor();
			// setColor(color | 0xFF000000);

			sendUpdateMessage(UI_EVENT.CLEAR);
		}
	};

	private final void sleep(final long nanoSecond) {
		try {
			Thread.sleep(nanoSecond);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
