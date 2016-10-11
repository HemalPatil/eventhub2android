package com.hemal.eventhub2;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.hemal.eventhub2.app.UserDetails;

/**
 * Created by Hemal on 10-Oct-16.
 */
public class FCMInstanceIdService extends FirebaseInstanceIdService
{
	@Override
	public void onTokenRefresh()
	{
		String token = FirebaseInstanceId.getInstance().getToken();
		Log.v("fcmtokeninstance", token);

		// add the FCM token to the shared preferences of the app
		UserDetails.fcmToken = token;
		SharedPreferences.Editor prefEditor = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE).edit();
		prefEditor.putString("fcmtoken", token);
		prefEditor.commit();

		// TODO : make the user subscribe to all notifications of all events
	}
}
