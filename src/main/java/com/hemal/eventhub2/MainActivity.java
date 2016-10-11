package com.hemal.eventhub2;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.hemal.eventhub2.app.AppController;
import com.hemal.eventhub2.app.URL;
import com.hemal.eventhub2.app.UserDetails;
import com.hemal.eventhub2.helper.DatabaseHelper;
import com.hemal.eventhub2.helper.SlidingTabLayout;
import com.hemal.eventhub2.helper.network.ConnectionDetector;
import com.hemal.eventhub2.helper.network.ServerUtilities;
import com.hemal.eventhub2.model.Event;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
	private DatabaseHelper DBHelper;
	private SQLiteDatabase localDB;
	private SlidingTabLayout mTabs;
	private ConnectionDetector cd;
	private ViewPager mPager;
	private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);

		Log.v("appactivities", "Main activity onCreate called");

        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setTitle(R.string.eventString);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

		// check if user is logged in already
		SharedPreferences preferences = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
		final String email = preferences.getString("email", "default");
		if(email == "default")
		{
			// User not signed-in
			Log.v("signin", "Not signed in");
			final String signInSkip = preferences.getString("signInSkip", "default");
			if(signInSkip == "default")
			{
				// app opened for first time, take user to login page
				Log.v("appopen", "first time");
				startActivity(new Intent(this, LoginActivity.class));
				finish();
				return;
			}
			/*else if(signInSkip == "skipped")
			{
				// user had opted not to sign-in
			}*/
		}
		else
		{
			// User signed-in already
			UserDetails.email = email;
			final String fcmToken = preferences.getString("fcmtoken", "default");
			Log.v("fcmtoken", "Main activity " + fcmToken);
			UserDetails.fcmToken = fcmToken;
			Log.v("signin", "Signed in as : " + email);
		}

		DBHelper = new DatabaseHelper(this);
		localDB = DBHelper.getWritableDatabase();

		cd = new ConnectionDetector(getApplicationContext());
		if(cd.isConnectedToInternet() && UserDetails.email != null)
		{
			// some times due to race conditions mobile_id (fcmToken) in db of our backend remains blank
			// make sure that this token is present in the db, if not present, send the token from here
			// the fcm token also needs to be updated if the token was refreshed but could not be updated at the backend
			// the app always has the recent token stored in its shared preferences
			testAndSetFCMToken();
			syncEvents();
		}

		// Create and add the fragments to the Events layout
		mPager = (ViewPager) findViewById(R.id.eventsPager);
		mPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
		mPager.setCurrentItem(1);
		mTabs = (SlidingTabLayout) findViewById(R.id.eventsTabs);
		mTabs.setDistributeEvenly(true);
		mTabs.setCustomTabView(R.layout.custom_tab_view, R.id.tabText);
		mTabs.setSelectedIndicatorColors(getResources().getColor(R.color.colorPrimaryDark));
		mTabs.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
		mTabs.setViewPager(mPager);
    }

	private void testAndSetFCMToken()
	{
		new registerFCM().execute();
	}

	private void syncEvents()
	{
		Log.v("syncdb", "syncing database");
		final String latestEvent = getLatestEventTimestamp();
		final String REQUEST_TAG = "syncDBRequest";
		final ArrayList<Integer> existingClubs = getAllClubs();

		StringRequest strReq = new StringRequest(Request.Method.POST, URL.syncevents,
				new Response.Listener<String>()
				{
					@Override
					public void onResponse(String response)
					{
						ArrayList<Integer> newClubs = new ArrayList<>();
						try
						{
							JSONObject jObj = new JSONObject(response);
							if(jObj==null)
							{
								Log.v("jsonerror", "json response is null not standard error");
								return;
							}
							Log.v("success", "volley success finally!");
							JSONArray jsonArray=jObj.getJSONArray("events");
							int len = jsonArray.length();
							for(int i=0;i<len;i++)
							{
								JSONObject jsonObject=jsonArray.getJSONObject(i);
								Event e=new Event();
								e.setId(Integer.valueOf(jsonObject.getString("id")));
								e.setEventName(jsonObject.getString("name"));
								e.setEventVenue(jsonObject.getString("venue"));
								e.setEventTime(jsonObject.getString("date"));
								Log.v("eventworking", "event loop working ");

								String eventname = jsonObject.getString("name");
								String eventvenue = jsonObject.getString("venue");
								String eventtype = jsonObject.getString("type");
								String eventsubtype = jsonObject.getString("subtype");
								String eventclub = jsonObject.getString("club");
								String eventcontact1name = jsonObject.getString("contact_name_1");
								String eventcontact2name = jsonObject.getString("contact_name_2");
								String eventcontact1number = jsonObject.getString("contact_number_1");
								String eventcontact2number = jsonObject.getString("contact_number_2");
								Integer eventIDint = jsonObject.getInt("id");
								String eventalias = jsonObject.getString("alias");
								Integer eventClubID = jsonObject.getInt("clubid");
								if(!existingClubs.contains(eventClubID) && !newClubs.contains(eventClubID))
								{
									newClubs.add(eventClubID);
								}
								//String eventdatestr = jsonObject.getString("date");
								String datetime=jsonObject.getString("date");
								String[] arr=datetime.split("T");
								String eventdatetime = arr[0]+" "+arr[1];
								datetime = jsonObject.getString("created_on");
								arr=datetime.split("T");
								String eventcreatedon = arr[0]+" "+arr[1];

								Log.v("eventdetails", eventname + eventvenue + eventtype + eventsubtype + eventclub + eventcontact1name + eventcontact2name + eventcontact1number + eventcontact2number + eventIDint + " " + eventalias + " " + eventdatetime + " " + eventcreatedon + " " + eventClubID.toString());

								ContentValues eventValues = new ContentValues();
								eventValues.put("id", eventIDint);
								eventValues.put("name", eventname);
								eventValues.put("type", eventtype);
								eventValues.put("subtype", eventsubtype);
								eventValues.put("venue", eventvenue);
								eventValues.put("club_id", eventClubID);
								eventValues.put("date_time", eventdatetime);
								eventValues.put("contact_name_1", eventcontact1name);
								eventValues.put("contact_name_2", eventcontact2name);
								eventValues.put("contact_number_1", eventcontact1number);
								eventValues.put("contact_number_2", eventcontact2number);
								eventValues.put("alias", eventalias);
								eventValues.put("created_on", eventcreatedon);
								long newID = localDB.insert("event", null, eventValues);
								Integer ID = (int)newID;
								Log.v("eventaddedID", ID.toString());
							}
							// No need to request the server for new clubs if there is no new club to be added
							if(newClubs.size() > 0)
							{
								MainActivity.this.syncClubs(newClubs);
							}
						}
						catch (JSONException e)
						{
							e.printStackTrace();
						}
					}
				},
				new Response.ErrorListener()
				{
					@Override
					public void onErrorResponse(VolleyError error)
					{
						Log.v("error", "error response volley");
					}
				})
		{
			@Override
			protected Map<String, String> getParams()
			{
				// Posting params to register url
				Map<String, String> params = new HashMap<>();
				params.put("latest_event", latestEvent);
				return params;
			}
		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, REQUEST_TAG);
	}

	private ArrayList<Integer> getAllClubs()
	{
		ArrayList<Integer> list = new ArrayList<>();
		Cursor c = localDB.rawQuery("SELECT id FROM club", null);
		if(c != null)
		{
			while(c.moveToNext())
			{
				Log.v("newclub", "Existing club : " + c.getInt(c.getColumnIndex("id")));
				list.add(c.getInt(c.getColumnIndex("id")));
			}
		}
		c.close();

		return list;
	}

	private void syncClubs(ArrayList<Integer> newClubs)
	{
		final JSONObject jObj = new JSONObject();
		try
		{
			JSONArray jArray = new JSONArray();
			for(Integer clubID : newClubs)
			{
				jArray.put((int)clubID);
			}
			jObj.put("newclubs", jArray);
			Log.v("newclub", jObj.toString());

			StringRequest req = new StringRequest(Request.Method.POST, URL.syncclubs,
					new Response.Listener<String>()
					{
						@Override
						public void onResponse(String response)
						{
							//Log.v("syncclubs", response);
							try
							{
								JSONObject jObj = new JSONObject(response);
								JSONArray jArray = jObj.getJSONArray("newclubs");
								int len = jArray.length();
								for(int i=0; i<len;i++)
								{
									JSONObject clubObject = jArray.getJSONObject(i);
									ContentValues clubValues = new ContentValues();
									clubValues.put("id", clubObject.getInt("id"));
									clubValues.put("name", clubObject.getString("name"));
									clubValues.put("alias", clubObject.getString("alias"));
									localDB.insert("club", null, clubValues);
								}
							}
							catch (JSONException e)
							{
								e.printStackTrace();
							}
						}
					},
					new Response.ErrorListener()
					{
						@Override
						public void onErrorResponse(VolleyError error)
						{
							Log.v("error", "error response volley");
						}
					})
			{
				@Override
				protected Map<String, String> getParams()
				{
					// Posting params to register url
					Map<String, String> params = new HashMap<>();
					params.put("newclubsjson", jObj.toString());
					return params;
				}
			};

			final String REQUEST_TAG = "syncClubsRequest";
			AppController.getInstance().addToRequestQueue(req, REQUEST_TAG);
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
	}

	private String getLatestEventTimestamp()
	{
		String latestEvent = null;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date today = new Date();

		Cursor c =localDB.rawQuery("SELECT MAX(created_on) as maxdate FROM event", null);
		if(c!=null && c.moveToFirst())
		{
			latestEvent = c.getString(c.getColumnIndex("maxdate"));
		}
		if(latestEvent == null)
		{
			// no event present in the database
			latestEvent = df.format(today);
		}
		else
		{
			// fetch events only after today if latest event is before today
			Date latestEventDate;
			try
			{
				latestEventDate = df.parse(latestEvent);
			}
			catch (ParseException e)
			{
				latestEventDate = today;
			}
			if(latestEventDate.before(today))
			{
				latestEvent = df.format(today);
			}
		}
		c.close();
		// TODO : remove this for production code, only for testing purposes
		latestEvent = "1970-01-01 00:00:00";

		return latestEvent;
	}

    @Override
    public void onBackPressed()
	{
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
		{
            drawer.closeDrawer(GravityCompat.START);
        }
		else
		{
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
	{
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_events)
		{
			findViewById(R.id.clublayout).setVisibility(View.GONE);
			findViewById(R.id.eventlayout).setVisibility(View.VISIBLE);
			toolbar.setTitle(R.string.eventString);
        }
		else if (id == R.id.nav_clubs)
		{
			findViewById(R.id.eventlayout).setVisibility(View.GONE);
			findViewById(R.id.clublayout).setVisibility(View.VISIBLE);
			toolbar.setTitle(R.string.clubString);
        }
		else if (id == R.id.nav_signout)
		{

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

	class MyPagerAdapter extends FragmentPagerAdapter
	{
		String[] tabs;

		public MyPagerAdapter(FragmentManager fm)
		{
			super(fm);
			tabs = getResources().getStringArray(R.array.tabs);
		}

		@Override
		public Fragment getItem(int index)
		{
			Log.d("index", String.valueOf(index));
			switch (index)
			{
				case 0:
					return new ProfileFragment();
				case 1:
					return new TodayFragment();
				case 2:
					return new UpComingFragment();
			}
			return null;
		}

		@Override
		public CharSequence getPageTitle(int position)
		{
			return tabs[position];
		}

		@Override
		public int getCount()
		{
			return 3;
		}
	}

	private class registerFCM extends AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(Void... params)
		{
			return ServerUtilities.registerFCMToken(UserDetails.email, UserDetails.fcmToken);
		}
	}
}
