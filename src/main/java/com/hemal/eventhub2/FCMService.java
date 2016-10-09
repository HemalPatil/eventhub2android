package com.hemal.eventhub2;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Hemal on 10-Oct-16.
 */
public class FCMService extends FirebaseMessagingService
{
	private static final String TAG = "FCMService";

	@Override
	public void onMessageReceived(RemoteMessage msg)
	{
		Log.v(TAG, msg.getFrom());

		if(msg.getNotification() != null)
		{
			Log.v(TAG, "notification : " + msg.getNotification().getBody());
		}
	}
}
