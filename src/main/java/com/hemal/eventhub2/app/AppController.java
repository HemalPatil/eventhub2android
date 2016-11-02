package com.hemal.eventhub2.app;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.hemal.eventhub2.helper.DatabaseHelper;
import com.hemal.eventhub2.helper.network.ConnectionDetector;
import com.hemal.eventhub2.util.LruBitmapCache;

/**
 * Created by Hemal on 09-Oct-16.
 */
public class AppController extends Application
{
	public static final String TAG = AppController.class.getSimpleName();

	private RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;
	private SQLiteDatabase localDB;
	private ConnectionDetector cd;

	private static AppController mInstance;

	@Override
	public void onCreate()
	{
		super.onCreate();
		mInstance = this;
		DatabaseHelper hp = new DatabaseHelper(getApplicationContext());
		localDB = hp.getWritableDatabase();
		cd = new ConnectionDetector(this);
	}

	public static synchronized AppController getInstance()
	{
		return mInstance;
	}

	public synchronized SQLiteDatabase getLocalDB()
	{
		return localDB;
	}

	public synchronized ConnectionDetector getConnectionDetector()
	{
		return cd;
	}

	public RequestQueue getRequestQueue()
	{
		if (mRequestQueue == null)
		{
			mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		}
		return mRequestQueue;
	}

	public ImageLoader getImageLoader()
	{
		getRequestQueue();
		if (mImageLoader == null)
		{
			mImageLoader = new ImageLoader(this.mRequestQueue, new LruBitmapCache());
		}
		return this.mImageLoader;
	}

	public <T> void addToRequestQueue(Request<T> req, String tag)
	{
		// set the default tag if tag is empty
		req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
		getRequestQueue().add(req);
	}

	public void cancelPendingRequests(Object tag)
	{
		if (mRequestQueue != null)
		{
			mRequestQueue.cancelAll(tag);
		}
	}
}