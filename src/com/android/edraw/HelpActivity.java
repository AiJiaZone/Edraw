package com.android.edraw;

import java.sql.Date;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

public class HelpActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help_activity);
		
		TextView textView = (TextView) findViewById(R.id.helpInform);
		
		String inform = buildInformation();
		textView.setText(inform);
	}
	
	String buildInformation() {
		StringBuilder builder = new StringBuilder();
		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo("com.android.edraw", CONTEXT_INCLUDE_CODE);
			long latsupdate = packageInfo.lastUpdateTime;
			long firstInstallTime = packageInfo.firstInstallTime;
			Date date = new Date(latsupdate);
			builder.append(getString(R.string.version));
			builder.append(packageInfo.versionName);
			builder.append(packageInfo.versionCode);
			builder.append("\n");
			builder.append(getString(R.string.last_update));
			builder.append(date);
			builder.append("\n");
			date = new Date(firstInstallTime);
			builder.append(getString(R.string.first_install));
			builder.append(date);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return builder.toString();
	}
}
