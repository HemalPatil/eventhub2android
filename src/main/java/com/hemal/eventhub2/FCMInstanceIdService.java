package com.hemal.eventhub2;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.hemal.eventhub2.helper.network.ServerUtilities;

/**
 * Created by Hemal on 10-Oct-16.
 */
public class FCMInstanceIdService extends FirebaseInstanceIdService
{
	@Override
	public void onTokenRefresh()
	{
		String token = FirebaseInstanceId.getInstance().getToken();
		Log.v("fcmtoken", token);

		// TODO : register this generated/refreshed token with backend
		SharedPreferences preferences = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);

		// send the FCM token to the backend
		final String email = preferences.getString("email", "default");
		if(email == "default")
		{
			// not signed-in through google
		}
		else
		{
			if(ServerUtilities.registerFCMToken(email, token))
			{
				// fcm token registered with backend
			}
			else
			{
				// fcm token registration with backend failed
			}
		}

		// add the FCM token to the shared preferences of the app
		SharedPreferences.Editor prefEditor = preferences.edit();
		prefEditor.putString("fcmtoken", token);
		prefEditor.commit();

		// TODO : make the user subscribe to all notifications of all events
	}
}
