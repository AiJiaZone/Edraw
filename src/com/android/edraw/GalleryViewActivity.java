package com.android.edraw;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.edraw.view.file.EdrawEvent;
import com.android.utils.EdrawService;

public class GalleryViewActivity extends Activity {
	private final static String TAG = "ViewActivity";
	private final String ACTION_DRAW = "com.android.edraw.DRAW";
	private static List<HashMap<String, Bitmap>> mImageLists = new ArrayList<HashMap<String, Bitmap>>();
	private GridView mGridView = null;

	private Context mContext;

	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			mAdapter.notifyDataSetChanged();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Black_NoTitleBar_Fullscreen);
		setContentView(R.layout.view_activity);
		mGridView = (GridView) findViewById(R.id.image_field);

		mAdapter = new ImageAdapter(mImageLists, this);
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(mItemClickListener);
		mGridView.setOnItemLongClickListener(mOnItemLongClickListener);
		TextView textView = (TextView) findViewById(R.id.no_image_text);
		textView.setOnClickListener(mOnClickListener);
		mAdapter.registerDataSetObserver(mDataSetObserver);
		mContext = this;
	}

	/**
	 * @see Move loading images process to onResume stage to avoid doing too much in onCreateView
	 *      stage
	 * */
	protected void onResume() {
		super.onResume();
		// Loading picture from SD card
		new Thread(mLoadingImageRunnable).start();
	};

	protected void onDestroy() {
		mAdapter.unregisterDataSetObserver(mDataSetObserver);
		super.onDestroy();
	};

	/**
	 * @see When no picture has drawn, add no image field to hint user
	 * */
	OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			int id = view.getId();
			switch (id) {
			case R.id.no_image_text:
				Intent intent = new Intent(ACTION_DRAW);
				startActivity(intent);
				break;
			default:
				break;
			}
		}
	};

	/**
	 * @see Statrt an activity to show full image.
	 * */
	OnItemClickListener mItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,
				long arg3) {
			Log.v(TAG, "arg0 = " + arg0 + " arg1 = " + view + " arg2 = "
					+ position + "arg3 = " + arg3);
			Map<String, Bitmap> imageMap = mImageLists.get(position);
			String path = imageMap.keySet().iterator().next();
			Intent intent = new Intent("com.android.edraw.SHOW_DETAIL");
			intent.putExtra("filepath", path);
			startActivity(intent);
		}
	};

	OnItemLongClickListener mOnItemLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View view,
				final int position, long arg3) {
			Map<String, Bitmap> imageMap = mImageLists.get(position);
			final String path = imageMap.keySet().iterator().next();
			new AlertDialog.Builder(mContext)
					.setMessage(R.string.delete_file)
					.setPositiveButton(R.string.confirm,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									File file = new File(path);
									if (file != null && file.exists()) {
										file.delete();
										mImageLists.remove(position);
										mHandler.sendEmptyMessage(0);
									}
								}
							}).setNegativeButton(R.string.cancel, null).show();
			return false;
		}
	};

	private ImageAdapter mAdapter;
	/**
	 * @see This Observer may be useless
	 * */
	DataSetObserver mDataSetObserver = new DataSetObserver() {
		public void onChanged() {
		};

		public void onInvalidated() {
		};
	};

	/**
	 * @see This thread is used to loading images, after all have been loaded, send message to
	 *      update UI
	 * */
	Runnable mLoadingImageRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			loadImages();
		}
	};

	/**
	 * @see Loading images from sdcard, and put them into arraylist Consider about security, Media
	 *      Mount state and file path should check before using it.
	 * */
	private void loadImages() {
		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			return;
		}
		File edraw = new File(EdrawEvent.ROOT_DIR);
		if (!edraw.exists()) {
			return;
		}
		String[] files = edraw.list(new FilenameFilter() {
			Pattern pattern = Pattern.compile(".*\\.png|jpg|jpeg");

			@Override
			public boolean accept(File arg0, String arg1) {
				// TODO Auto-generated method stub
				return pattern.matcher(arg1).matches();
			}
		});
		Arrays.sort(files, String.CASE_INSENSITIVE_ORDER);
		mImageLists.clear();
		for (String file : files) {
			Log.v(TAG, "file Name = " + file);
			String path = EdrawService.buildFileName(EdrawEvent.ROOT_DIR, file);
			// Decode the image with option field
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = true;
			options.inSampleSize = 4;
			// here the path other then filename is needed.
			Bitmap bitmap = BitmapFactory.decodeFile(path, options);
			HashMap<String, Bitmap> image = new HashMap<String, Bitmap>();
			image.put(path, bitmap);
			mImageLists.add(image);
			mHandler.sendEmptyMessage(0);
		}
	}
}

class ImageAdapter extends BaseAdapter {
	private Context mContext;
	private List<HashMap<String, Bitmap>> images;

	ImageAdapter(List<HashMap<String, Bitmap>> images, Context context) {
		super();
		this.images = images;
		mContext = context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return images.size();
	}

	@Override
	public Object getItem(int index) {
		return images.get(index);
	}

	@Override
	public long getItemId(int index) {
		// TODO Auto-generated method stub
		return index;
	}

	@Override
	public View getView(int index, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.image_item, null);
			viewHolder = new ViewHolder();
			viewHolder.textView = (TextView) convertView
					.findViewById(R.id.text);
			viewHolder.imageView = (ImageView) convertView
					.findViewById(R.id.image);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		try {
			String imagePath = images.get(index).keySet().iterator().next();
			String imageName = imagePath.substring(
					imagePath.lastIndexOf("/") + 1, imagePath.lastIndexOf("."));
			viewHolder.textView.setText(imageName);
			viewHolder.imageView.setImageBitmap(images.get(index).values()
					.iterator().next());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return convertView;
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		TextView textView = (TextView) ((GalleryViewActivity) mContext)
				.findViewById(R.id.no_image_text);
		if (getCount() != 0) {
			textView.setVisibility(View.GONE);
		} else {
			textView.setVisibility(View.VISIBLE);
		}
	}

	class ViewHolder {
		TextView textView;
		ImageView imageView;
	}

}