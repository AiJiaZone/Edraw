package com.android.edraw.view.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;

import com.android.edraw.view.file.EdrawEvent.FILE_EVENT;

public final class FileOperator extends AsyncTask<Object, Void, Void> {

	public static void handle(FileOperationCallbacks callback,
			Object... objects) {
		new FileOperator(callback).execute(objects);
	}

	private final FileOperationCallbacks mCallback;

	public FileOperator(FileOperationCallbacks callback) {
		mCallback = callback;
	}

	@Override
	protected Void doInBackground(Object... objects) {
		if (objects == null) {
			return null;
		}
		int length = objects.length;
		String filePath = (String) objects[0];
		if (length == 2) {
			FILE_EVENT operation = (FILE_EVENT) objects[1];
			if (EdrawEvent.FILE_EVENT.DELETE.equals(operation)) {
				delete(filePath);
			}
			return null;
		}
		if (length == 3) {
			Bitmap bitmap = (Bitmap) objects[1];
			FILE_EVENT operation = (FILE_EVENT) objects[2];
			if (EdrawEvent.FILE_EVENT.SAVE.equals(operation)) {
				save(filePath, bitmap);
			}
			return null;
		}

		return null;
	}

	void save(String fileName, Bitmap bitmap) {
		try {
			FileOutputStream fStream = new FileOutputStream(fileName);
			bitmap.compress(CompressFormat.PNG, 100, fStream);
			fStream.close();
			if (mCallback != null)
				mCallback.onSaved();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void delete(String filePath) {
		try {
			File dir = new File(filePath);
			if (!dir.exists())
				return;
			File[] files = dir.listFiles();
			for (File file : files) {
				file.delete();
			}
			if (mCallback != null)
				mCallback.onDeleted();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
