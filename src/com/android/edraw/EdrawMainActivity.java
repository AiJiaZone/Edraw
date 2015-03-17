package com.android.edraw;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class EdrawMainActivity extends Activity {
	private final String ACTION_DRAW = "com.android.edraw.DRAW";
	private final String ACTION_VIEW = "com.android.edraw.VIEW";
	private final String ACTION_HELP = "com.android.edraw.HELP";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Black_NoTitleBar_Fullscreen);
		setContentView(R.layout.main);
		initViews();
	}

	private void initViews() {
		Button drawButton = (Button) findViewById(R.id.draw);
		Button viewButton = (Button) findViewById(R.id.view);
		Button helpButton = (Button) findViewById(R.id.help);

		drawButton.setOnClickListener(mDrawPictureListener);
		viewButton.setOnClickListener(mViewPictureListener);
		helpButton.setOnClickListener(mHelpPictureListener);
	}

	OnClickListener mDrawPictureListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			gotoActivity(ACTION_DRAW);
		}
	};

	OnClickListener mViewPictureListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			gotoActivity(ACTION_VIEW);
		}
	};

	OnClickListener mHelpPictureListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			gotoActivity(ACTION_HELP);
		}
	};

	void gotoActivity(String action) {
		Intent intent = new Intent(action);
		startActivity(intent);
	}
}
