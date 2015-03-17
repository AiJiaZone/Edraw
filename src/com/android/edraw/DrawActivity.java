package com.android.edraw;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.edraw.view.DrawerSurfaceView;
import com.android.edraw.view.DrawerSurfaceView.OnDrawCallBack;
import com.android.edraw.view.file.EdrawEvent;
import com.android.utils.EdrawUtils;

public class DrawActivity extends Activity {
	private final String TAG = "DrawActivity";
	private DrawerSurfaceView mSurface;
	private View mInflater;

	private int mColor = Color.WHITE;
	private SeekBar mRedBar;
	private SeekBar mGreenBar;
	private SeekBar mBlueBar;
	private SeekBar mAplhaBar;
	private SeekBar mHardBar;
	private SeekBar mRadiusBar;

	private boolean mPanelToggle = false;

	private ArrayList<String> mDrawerStyle = new ArrayList<String>();
	private ArrayAdapter<String> mDrawerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Black_NoTitleBar_Fullscreen);
		setContentView(R.layout.draw_activity);
		initialView();
	}

	private void initialView() {
		mSurface = (DrawerSurfaceView) findViewById(R.id.canvas);
		mInflater = findViewById(R.id.painter_settings);
		mInflater.setFocusable(true);
		mInflater.setOnClickListener(mOnClickListener);
		mSurface.setOnDrawCallBack(mOnDrawCallBack);

		mRedBar = (SeekBar) findViewById(R.id.red);
		mBlueBar = (SeekBar) findViewById(R.id.blue);
		mGreenBar = (SeekBar) findViewById(R.id.green);
		mAplhaBar = (SeekBar) findViewById(R.id.alpha);
		mHardBar = (SeekBar) findViewById(R.id.hard);
		mRadiusBar = (SeekBar) findViewById(R.id.radius);
		mRedBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
		mGreenBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
		mBlueBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
		mAplhaBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
		mHardBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
		mRadiusBar.setOnSeekBarChangeListener(mSeekBarChangeListener);

		// Add buttons
		Button saveButton = (Button) findViewById(R.id.save);
		Button clearButton = (Button) findViewById(R.id.clear);
		Button undoButton = (Button) findViewById(R.id.undo);
		Button redoButton = (Button) findViewById(R.id.redo);
		saveButton.setOnClickListener(mOnClickListener);
		clearButton.setOnClickListener(mOnClickListener);
		undoButton.setOnClickListener(mOnClickListener);
		redoButton.setOnClickListener(mOnClickListener);

		TextView textView = (TextView) findViewById(R.id.color_ind);
		textView.setOnClickListener(mOnClickListener);

		Spinner drawerStyle = (Spinner) findViewById(R.id.drawer_style);
		mDrawerAdapter = new ArrayAdapter<String>(this,
				android.R.layout.preference_category, mDrawerStyle);
		mDrawerStyle.add(EdrawEvent.DRAWER_STYLE.NORMAL.toString());
		mDrawerStyle.add(EdrawEvent.DRAWER_STYLE.MIRROR.toString());
		mDrawerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		drawerStyle.setAdapter(mDrawerAdapter);
		drawerStyle.setOnItemSelectedListener(mOnDrawerSelectedListener);

		CheckBox fillBox = (CheckBox) findViewById(R.id.filled);
		CheckBox strokeBox = (CheckBox) findViewById(R.id.stroke);
		fillBox.setOnCheckedChangeListener(mCheckedChangeListener);
		strokeBox.setOnCheckedChangeListener(mCheckedChangeListener);
	}

	protected void onResume() {
		super.onResume();
	};

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			View view = findViewById(R.id.checkbox_set);
			view.setVisibility(msg.arg1);
			CheckBox fillBox = (CheckBox) findViewById(R.id.filled);
			CheckBox strokeBox = (CheckBox) findViewById(R.id.stroke);
			fillBox.setChecked(false);
			strokeBox.setChecked(false);
		};
	};
	OnCheckedChangeListener mCheckedChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			int mStyle = 0x02; // Save mirrorDrawer style, @see {@MirrorDrawer}
			int id = buttonView.getId();
			if (R.id.filled == id) {
				if (isChecked) {
					mStyle |= 0x04;
				} else {
					mStyle &= 0x0A;
				}
			} else if (R.id.stroke == id) {
				if (isChecked) {
					mStyle |= 0x08;
				} else {
					mStyle &= 0x06;
				}
			}
			mSurface.setDrawerStyle(mStyle);
		}
	};

	// Adjust setting panel
	OnDrawCallBack mOnDrawCallBack = new OnDrawCallBack() {

		@Override
		public void onDrawCallBack(MotionEvent event) {
			// TODO Auto-generated method stub
			int action = event.getAction();
			if (action == MotionEvent.ACTION_DOWN) {
				if (mPanelToggle == false) {
					hidePainterSettingPanel();
					mPanelToggle = !mPanelToggle;
				}
			} else if (action == MotionEvent.ACTION_CANCEL
					|| action == MotionEvent.ACTION_UP) {
				// mInflater.setPadding(0, 0, 0, 0);
			}
		}
	};

	OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (seekBar.equals(mRedBar) || seekBar.equals(mGreenBar)
					|| seekBar.equals(mBlueBar) || seekBar.equals(mAplhaBar)) {
				int red = 255 - mRedBar.getProgress();
				int green = 255 - mGreenBar.getProgress();
				int blue = 255 - mBlueBar.getProgress();
				int alpha = mAplhaBar.getProgress();
				mColor = Color.argb(alpha, red, green, blue);
				mSurface.setColor(mColor);
				TextView colorValue = (TextView) findViewById(R.id.colorvalue);
				String hex = EdrawUtils.argbColorToHex(alpha, red, green, blue);
				colorValue.setText("\n" + hex);// Show color value in HEX format
				TextView colorHintView = (TextView) findViewById(R.id.color_ind);
				colorHintView.setBackgroundColor(mColor);
			}

			if (seekBar.equals(mHardBar)) {
				int hard = mHardBar.getProgress();
				float stroke = hard / 100;
				mSurface.setStroke(stroke);
			}
			if (seekBar.equals(mRadiusBar)) {
				int r = mRadiusBar.getProgress();
				float radius = r * 100 / 1000; // Set the max width of paint to 20
				mSurface.setRadius(radius);
			}
		}
	};

	OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int id = v.getId();
			switch (id) {
			case R.id.save:
				// TODO: save the picture
				save();
				break;
			case R.id.clear:
				clear();
				break;
			case R.id.undo:
				// TODO: Add undo action here
				undo();
				break;
			case R.id.redo:
				// TODO: Add re-do action here
				redo();
				break;
			case R.id.color_ind:
				mSurface.setColor(mColor);
				break;
			case R.id.painter_settings:
				if (mPanelToggle == false) {
					hidePainterSettingPanel();
				} else {
					showPainterSettingPanel();
				}
				mPanelToggle = !mPanelToggle;
				break;
			default:
				break;
			}
		}
	};

	private void hidePainterSettingPanel() {
		int height = mInflater.getHeight();
		mInflater.setPadding(0, 50 - height, 0, 0);
	}

	private void showPainterSettingPanel() {
		mInflater.setPadding(0, 0, 0, 0);
	}

	private OnItemSelectedListener mOnDrawerSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			// TODO Auto-generated method stub
			String type = mDrawerAdapter.getItem(position).toString();
			mSurface.setDrawerType(type);

			Message msg = new Message();
			msg.what = 0;
			if (EdrawEvent.DRAWER_STYLE.MIRROR.toString().equals(type)) {
				msg.arg1 = View.VISIBLE;
			} else {
				msg.arg1 = View.GONE;
			}
			mHandler.sendMessage(msg);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub

		}
	};
	private OnActionButtonClickListener mActionButtonClickListener;

	public void setActionButtonClickListener(OnActionButtonClickListener l) {
		mActionButtonClickListener = l;
	}

	public interface OnActionButtonClickListener {
		public void onSave();

		public void onClear();

		public void onUndo();

		public void onRedo();
	}

	public void save() {
		mActionButtonClickListener.onSave();
	}

	private void clear() {
		mActionButtonClickListener.onClear();
	}

	private void undo() {
		mActionButtonClickListener.onUndo();
	}

	private void redo() {
		mActionButtonClickListener.onRedo();
	}
}
