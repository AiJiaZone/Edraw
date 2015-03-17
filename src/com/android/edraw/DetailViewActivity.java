package com.android.edraw;

import java.io.File;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public class DetailViewActivity extends Activity {
	private final String SHOW_DETAIL = "com.android.edraw.SHOW_DETAIL";
	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (SHOW_DETAIL.equals(action)) {
				String imageName = intent.getStringExtra("filename");
				Message msg = new Message();
				msg.what = 0;
				msg.obj = imageName;
				mHandler.sendMessage(msg);
			}
		};
	};

	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			String fileName = (String) msg.obj;
			Bitmap bm = BitmapFactory.decodeFile(fileName);
			ImageView imageView = (ImageView) findViewById(R.id.imageView);
			imageView.setImageBitmap(bm);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Black_NoTitleBar_Fullscreen);
		setContentView(R.layout.show_detail);
		
		Intent intent = getIntent();
		String action = intent.getAction();
		if (SHOW_DETAIL.equals(action)) {
			String imageName = intent.getStringExtra("filepath");
			Bitmap bm = BitmapFactory.decodeFile(imageName);
			ImageView imageView = (ImageView) findViewById(R.id.imageView);
			imageView.setImageBitmap(bm);
			registerForContextMenu(imageView);
		}
	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.view_menu, menu);
	};

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		// unregisterReceiver(mBroadcastReceiver);
		super.onDestroy();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int id = item.getItemId();
		String fileName = getIntent().getStringExtra("filepath");
		switch (id) {
		case R.id.graffiti:
			Intent intent = new Intent("com.android.edraw.DRAW");
			intent.putExtra("filename", fileName);
			startActivity(intent);
			break;
		case R.id.delete:
			File file = new File(fileName);
			if (file.exists())
				file.delete();
			finish();
		default:
			break;
		}
		return true;
	}
}
