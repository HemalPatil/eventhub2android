package com.hemal.eventhub2.helper.network;

/**
 * Created by Hemal on 09-Oct-16.
 */
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;


public final class ServerUtilities
{
	private static final int MAX_ATTEMPTS = 5;
	private static final int BACKOFF_MILLI_SECONDS = 2000;
	private static final Random random = new Random();


	public static boolean registerGoogleSignIn(String name, String email)
	{
		String serverUrl = com.hemal.eventhub2.app.URL.registerGoogleSignIn;
		Map<String, String> params = new HashMap<>();
		params.put("method","google");
		params.put("name", name);
		params.put("email", email);
		return post(serverUrl, params);
	}

	public static boolean registerFCMToken(String email, String token)
	{
		Log.v("serverutilities", "register fcm token " + token);
		String serverUrl = com.hemal.eventhub2.app.URL.registerFCMToken;
		Map<String, String> params = new HashMap<>();
		params.put("email", email);
		params.put("mobile_id", token);
		return post(serverUrl, params);
	}

	private static boolean post(String URL, Map<String, String> params)
	{
		long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
		// As the server might be down, we will retry it MAX_ATTEMPTS number of times
		for (int i = 1; i <= MAX_ATTEMPTS; i++)
		{
			URL url;
			try
			{
				url = new URL(URL);
			}
			catch(MalformedURLException e)
			{
				throw new IllegalArgumentException("invalid url: " + URL);
			}
			StringBuilder bodyBuilder = new StringBuilder();
			Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
			// constructs the POST body using the parameters
			while (iterator.hasNext())
			{
				Entry<String, String> param = iterator.next();
				bodyBuilder.append(param.getKey()).append('=').append(param.getValue());
				if (iterator.hasNext())
				{
					bodyBuilder.append('&');
				}
			}
			String body = bodyBuilder.toString();
			Log.v("FCM", "Posting '" + body + "' to " + url);
			byte[] bytes = body.getBytes();
			HttpURLConnection conn = null;
			try
			{
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				conn.setDoOutput(true);

				OutputStream out = conn.getOutputStream();
				out.write(bytes);
				out.close();
				int status = conn.getResponseCode();
				if(status != HttpURLConnection.HTTP_OK)
				{
					throw new IOException("Post failed with error code : " + status);
				}
				else
				{
					return true;
				}
			}
			catch(IOException e)
			{
				try
				{
					Thread.sleep(BACKOFF_MILLI_SECONDS);
				}
				catch (InterruptedException ie)
				{
					Thread.currentThread().interrupt();
				}
			}
			finally
			{
				if(conn != null)
				{
					conn.disconnect();
				}
			}
		}
		return false;
	}
}