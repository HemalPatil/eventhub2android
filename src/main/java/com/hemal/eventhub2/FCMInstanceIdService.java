package com.hemal.eventhub2;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.hemal.eventhub2.app.UserDetails;
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
		Log.v("fcmtokeninstance", token);

		// add the FCM token to the shared preferences of the app
		UserDetails.fcmtoken = token;
		SharedPreferences preferences = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = preferences.edit();
		prefEditor.putString("fcmtoken", token);
		prefEditor.commit();

		if(UserDetails.email != null)
		{
			ServerUtilities.registerFCMToken(UserDetails.email, token);
		}

		// TODO : make the user subscribe to all notifications of all events
	}
}
