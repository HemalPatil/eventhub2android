package com.hemal.eventhub2.helper.network;

/**
 * Created by Hemal on 09-Oct-16.
 */
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionDetector
{
	private Context context;

	public ConnectionDetector(Context context)
	{
		this.context = context;
	}

	public boolean isConnectedToInternet()
	{
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

		return isConnected;
	}
}