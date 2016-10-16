package com.hemal.eventhub2;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hemal.eventhub2.app.Topics;
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

		// All users should be subscribed to this topic
		FirebaseMessaging.getInstance().subscribeToTopic(Topics.ALL_EVENTS);

		// All users must be subscribed to this topic so that if a club is added, the user is notified about it
		// and we handle the logic of adding it to the local database
		FirebaseMessaging.getInstance().subscribeToTopic(Topics.ADD_CLUB);
	}
}
