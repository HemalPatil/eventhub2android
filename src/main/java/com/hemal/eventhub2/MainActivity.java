package com.hemal.eventhub2;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hemal.eventhub2.adapters.CustomClubListAdapter;
import com.hemal.eventhub2.app.AppController;
import com.hemal.eventhub2.app.Topics;
import com.hemal.eventhub2.app.URL;
import com.hemal.eventhub2.app.UserDetails;
import com.hemal.eventhub2.helper.DatabaseHelper;
import com.hemal.eventhub2.helper.SlidingTabLayout;
import com.hemal.eventhub2.helper.network.ConnectionDetector;
import com.hemal.eventhub2.helper.network.ServerUtilities;
import com.hemal.eventhub2.model.Club;
import com.hemal.eventhub2.model.Event;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener
{
	private final static int ADD_EVENT_CODE = 0xcafe;
	private final static int ADD_CLUB_CODE = 0xbabe;

	private SQLiteDatabase localDB;
	private SlidingTabLayout mTabs;
	private ConnectionDetector cd;
	private ViewPager mPager;
	private Toolbar toolbar;
	private ListView clubsListView;
	private CustomClubListAdapter clubAdapter;
	private ArrayList<Club> allClubs;
	private CustomEventsFragment myEventsFragment;
	private CustomEventsFragment todayFragment;
	private CustomEventsFragment upComingFragment;
	private FloatingActionButton addEventClubButton;
	private boolean clubEventFocus;	// true = focused on clubs layout, false = focused on events layout

    @Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);

		Log.v("appactivities", "Main activity onCreate called");

        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.mainActivityToolbar);
		toolbar.setTitle(R.string.eventString);
        setSupportActionBar(toolbar);

		addEventClubButton = (FloatingActionButton) findViewById(R.id.addEventClubButton);
		addEventClubButton.setVisibility(View.GONE);
		addEventClubButton.setOnClickListener(this);

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
			UserDetails.fcmToken = fcmToken;
			Log.v("signin", "Signed in as : " + email);
			Log.v("fcmtoken", "Main activity " + fcmToken);
		}

		DatabaseHelper DBHelper = new DatabaseHelper(this);
		localDB = DBHelper.getWritableDatabase();
		clubEventFocus = false;

		allClubs = getAllClubs();

		cd = new ConnectionDetector(this);

		Bundle x = new Bundle();
		x.putInt("fragmentLayout", R.layout.myevents_fragment);
		x.putInt("refreshLayout", R.id.myEventsRefreshLayout);
		x.putInt("listView", R.id.myEventList);
		x.putInt("noEvents", R.id.noEventsMy);
		x.putInt("refreshButton", R.id.myEventsRefreshButton);
		x.putString("tag", "myevents");
		myEventsFragment = new MyEventsFragment();
		myEventsFragment.setArguments(x);

		x = new Bundle();
		x.putInt("fragmentLayout", R.layout.today_fragment);
		x.putInt("refreshLayout", R.id.todayRefreshLayout);
		x.putInt("listView", R.id.todayEventList);
		x.putInt("noEvents", R.id.noEventsToday);
		x.putInt("refreshButton", R.id.todayRefreshButton);
		x.putString("tag", "today");
		todayFragment = new TodayFragment();
		todayFragment.setArguments(x);

		x = new Bundle();
		x.putInt("fragmentLayout", R.layout.upcoming_fragment);
		x.putInt("refreshLayout", R.id.upcomingRefreshLayout);
		x.putInt("listView", R.id.upcomingEventList);
		x.putInt("noEvents", R.id.noEventsUpcoming);
		x.putInt("refreshButton", R.id.upcomingRefreshButton);
		x.putString("tag", "upcoming");
		upComingFragment = new UpcomingFragment();
		upComingFragment.setArguments(x);

		mPager = (ViewPager) findViewById(R.id.eventsPager);
		mPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
		mPager.setCurrentItem(1);
		mTabs = (SlidingTabLayout) findViewById(R.id.eventsTabs);
		mTabs.setDistributeEvenly(true);
		mTabs.setCustomTabView(R.layout.custom_tab_view, R.id.tabText);
		mTabs.setSelectedIndicatorColors(getResources().getColor(R.color.colorPrimary));
		mTabs.setBackgroundColor(getResources().getColor(R.color.white));
		mTabs.setViewPager(mPager);

		clubsListView = (ListView) findViewById(R.id.clubsList);
		clubAdapter = new CustomClubListAdapter(this, allClubs);
		clubsListView.setAdapter(clubAdapter);
		clubsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Intent intent = new Intent(MainActivity.this, AboutClubActivity.class);
				Club c = (Club) parent.getItemAtPosition(position);
				intent.putExtra("clubid", c.clubID);
				startActivity(intent);
			}
		});

		if(cd.isConnectedToInternet() && UserDetails.email != null)
		{
			// some times due to race conditions mobile_id (fcmToken) in db of our backend remains blank
			// make sure that this token is present in the db, if not present, send the token from here
			// the fcm token also needs to be updated if the token was refreshed but could not be updated at the backend
			// the app always has the recent token stored in its shared preferences
			testAndSetFCMToken();
			syncDatabase();
			checkIsAdmin();
		}
    }

	private void testAndSetFCMToken()
	{
		if(UserDetails.fcmToken == "default")
		{
			// user's token is not yet added to app's preferences.
			// most probable cause : network slow, hence token hasn't been received yet
			return;
		}
		new registerFCM().execute();
	}

	private void checkIsAdmin()
	{
		StringRequest req = new StringRequest(Request.Method.POST, URL.isAdmin,
				new Response.Listener<String>()
				{
					@Override
					public void onResponse(String response)
					{
						try
						{
							JSONObject jObj = new JSONObject(response);
							UserDetails.isAdmin = jObj.getInt("isadmin") == 1;
							if(UserDetails.isAdmin)
							{
								addEventClubButton.setVisibility(View.VISIBLE);
							}
						}
						catch(JSONException e)
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

					}
				})
		{
			@Override
			protected Map<String, String> getParams()
			{
				// Posting params to is admin url
				Map<String, String> params = new HashMap<>();
				params.put("email", UserDetails.email);
				return params;
			}
		};

		AppController.getInstance().addToRequestQueue(req, "checkAdminRequest");
	}

	public void refreshMyEventsFragment()
	{
		myEventsFragment.addEventsToFragment();
	}

	public void syncDatabase()
	{
		final String latestCreatedOn = getLatestCreatedOnEventTimestamp();
		final ArrayList<Integer> existingClubs = new ArrayList<>();
		for(Club c : allClubs)
		{
			existingClubs.add(c.clubID);
		}

		StringRequest strReq = new StringRequest(Request.Method.POST, URL.syncEvents,
				new Response.Listener<String>()
				{
					@Override
					public void onResponse(String response)
					{
						ArrayList<Integer> newClubs = new ArrayList<>();
						newClubs.clear();
						try
						{
							JSONObject jObj = new JSONObject(response);
							JSONArray jsonArray = jObj.getJSONArray("events");
							int len = jsonArray.length();
							for(int i=0;i<len;i++)
							{
								JSONObject jsonObject=jsonArray.getJSONObject(i);
								int eventClubID = jsonObject.getInt("club_id");
								if(!existingClubs.contains(eventClubID) && !newClubs.contains(eventClubID))
								{
									newClubs.add(eventClubID);
								}
								ContentValues eventValues = new ContentValues();
								eventValues.put("id", jsonObject.getInt("id"));
								eventValues.put("type", jsonObject.getString("type"));
								eventValues.put("subtype", jsonObject.getString("subtype"));
								eventValues.put("name", jsonObject.getString("name"));
								eventValues.put("date_time", jsonObject.getString("date_time"));
								eventValues.put("contact_name_1", jsonObject.getString("contact_name_1"));
								eventValues.put("contact_number_1", jsonObject.getString("contact_number_1"));
								eventValues.put("contact_name_2", jsonObject.getString("contact_name_2"));
								eventValues.put("contact_number_2", jsonObject.getString("contact_number_2"));
								eventValues.put("venue", jsonObject.getString("venue"));
								eventValues.put("alias", jsonObject.getString("alias"));
								eventValues.put("club_id", jsonObject.getInt("club_id"));
								eventValues.put("created_on", jsonObject.getString("created_on"));
								eventValues.put("followed", jsonObject.getInt("followed"));
								eventValues.put("image_downloaded", 0);
								eventValues.put("description", jsonObject.getString("description"));
								long newID = localDB.insert("event", null, eventValues);
								Integer ID = (int)newID;
								Log.v("eventAddedID", ID.toString());
							}

							// If received new events from server, ask the fragments to update themselves
							if(len > 0)
							{
								myEventsFragment.addEventsToFragment();
								todayFragment.addEventsToFragment();
								upComingFragment.addEventsToFragment();
							}
							else
							{
								Toast.makeText(MainActivity.this, R.string.noNewEvents, Toast.LENGTH_SHORT).show();
								if(myEventsFragment.isRefreshing())
								{
									myEventsFragment.setRefreshing(false);
								}
								if(todayFragment.isRefreshing())
								{
									todayFragment.setRefreshing(false);
								}
								if(upComingFragment.isRefreshing())
								{
									upComingFragment.setRefreshing(false);
								}
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
						Toast.makeText(MainActivity.this, R.string.slowInternet, Toast.LENGTH_SHORT).show();
						if(myEventsFragment.isRefreshing())
						{
							myEventsFragment.setRefreshing(false);
						}
						if(todayFragment.isRefreshing())
						{
							todayFragment.setRefreshing(false);
						}
						if(upComingFragment.isRefreshing())
						{
							upComingFragment.setRefreshing(false);
						}
					}
				})
		{
			@Override
			protected Map<String, String> getParams()
			{
				// Posting params to sync event url
				Map<String, String> params = new HashMap<>();
				params.put("latest_created_on", latestCreatedOn);
				params.put("email", UserDetails.email);
				return params;
			}
		};

		AppController.getInstance().addToRequestQueue(strReq, "syncEventsRequest");
	}

	private void refreshClubsList()
	{
		allClubs.clear();
		allClubs.addAll(getAllClubs());
		clubAdapter.notifyDataSetChanged();
	}

	private ArrayList<Club> getAllClubs()
	{
		ArrayList<Club> list = new ArrayList<>();
		Cursor c = localDB.rawQuery("SELECT * FROM club", null);
		while(c.moveToNext())
		{
			Club club = new Club();
			club.clubID = c.getInt(c.getColumnIndex("id"));
			club.name = c.getString(c.getColumnIndex("name"));
			club.alias = c.getString(c.getColumnIndex("alias"));
			club.followed = c.getInt(c.getColumnIndex("followed")) == 1;
			Log.v("newclub", "Existing club : " + club.name);
			list.add(club);
		}
		c.close();

		return list;
	}

	// TODO : add check before adding any club, that club of same name and alias is not already present
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

			StringRequest req = new StringRequest(Request.Method.POST, URL.syncClubs,
					new Response.Listener<String>()
					{
						@Override
						public void onResponse(String response)
						{
							try
							{
								JSONObject jObj = new JSONObject(response);
								JSONArray jArray = jObj.getJSONArray("newclubs");
								int len = jArray.length();
								for(int i=0; i<len;i++)
								{
									JSONObject clubObject = jArray.getJSONObject(i);
									ContentValues clubValues = new ContentValues();
									Club nc = new Club();
									int followed = clubObject.getInt("followed");
									nc.clubID = clubObject.getInt("id");
									nc.name = clubObject.getString("name");
									nc.alias = clubObject.getString("alias");
									nc.followed = followed == 1;
									clubValues.put("id", nc.clubID);
									clubValues.put("name", nc.name);
									clubValues.put("alias", nc.alias);
									clubValues.put("followed", followed);
									allClubs.add(nc);
									localDB.insert("club", null, clubValues);
								}
								clubAdapter.notifyDataSetChanged();
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
					// Posting params to sync club url
					Map<String, String> params = new HashMap<>();
					params.put("newclubsjson", jObj.toString());
					params.put("email", UserDetails.email);
					return params;
				}
			};

			AppController.getInstance().addToRequestQueue(req, "syncClubsRequest");
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
	}

	private String getLatestCreatedOnEventTimestamp()
	{
		String latestCreatedOnEvent = null;

		Cursor c =localDB.rawQuery("SELECT MAX(created_on) as maxdate FROM event", null);
		if(c!=null && c.moveToFirst())
		{
			latestCreatedOnEvent = c.getString(c.getColumnIndex("maxdate"));
		}
		c.close();
		if(latestCreatedOnEvent == null || latestCreatedOnEvent.isEmpty())
		{
			// no events in the database
			latestCreatedOnEvent = "1980-01-01 00:00:00";
		}

		return latestCreatedOnEvent;
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
			clubEventFocus = false;
			findViewById(R.id.clubLayout).setVisibility(View.GONE);
			findViewById(R.id.eventLayout).setVisibility(View.VISIBLE);
			toolbar.setTitle(R.string.eventString);
        }
		else if (id == R.id.nav_clubs)
		{
			clubEventFocus = true;
			findViewById(R.id.eventLayout).setVisibility(View.GONE);
			findViewById(R.id.clubLayout).setVisibility(View.VISIBLE);
			toolbar.setTitle(R.string.clubString);
        }
		else if (id == R.id.nav_signout)
		{
			// TODO : add a sign-out activity
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
			tabs = getResources().getStringArray(R.array.eventTabNames);
		}

		@Override
		public Fragment getItem(int index)
		{
			Log.d("index", String.valueOf(index));
			switch (index)
			{
				case 0:
					return myEventsFragment;
				case 1:
					return todayFragment;
				case 2:
					return upComingFragment;
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

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		if(id == R.id.clubFollow)
		{
			if(!cd.isConnectedToInternet())
			{
				Toast.makeText(MainActivity.this, R.string.noInternet, Toast.LENGTH_SHORT).show();
				return;
			}
			View parentRow = (View) v.getParent().getParent();
			final int position = clubsListView.getPositionForView(parentRow);
			Club c = (Club) clubsListView.getItemAtPosition(position);
			Button x = (Button) v;
			if(v.getTag().toString() == "notfollowed")
			{
				sendClubFollowRequest(c, true, x);
			}
			else
			{
				sendClubFollowRequest(c, false, x);
			}
			myEventsFragment.addEventsToFragment();
		}
		else if(id ==  R.id.addEventClubButton)
		{
			if(clubEventFocus)
			{
				// focus was on clubs layout
				startActivityForResult(new Intent(this, AddClubActivity.class), ADD_CLUB_CODE);
			}
			else
			{
				// focus was on events layout
				startActivityForResult(new Intent(this, AddEventActivity.class), ADD_EVENT_CODE);
			}
		}
	}

	private void sendClubFollowRequest(final Club club, final boolean follow, final Button b)
	{
		String requestUrl;
		if(follow)
		{
			requestUrl = URL.followClub;
		}
		else
		{
			requestUrl = URL.unFollowClub;
		}
		final String clubFollowTopic = Topics.CLUB_FOLLOW + club.clubID + club.alias;
		StringRequest req = new StringRequest(Request.Method.POST, requestUrl,
				new Response.Listener<String>()
				{
					@Override
					public void onResponse(String response)
					{
						try
						{
							JSONObject jObj = new JSONObject(response);
							if(jObj.getInt("success") == 1)
							{
								Cursor cr = localDB.rawQuery("SELECT * FROM event WHERE club_id=" + club.clubID, null);
								FirebaseMessaging fcmInstance = FirebaseMessaging.getInstance();
								if(follow)
								{
									// user was not following, now he should follow
									club.followed = true;
									localDB.execSQL("UPDATE club SET followed=1 WHERE id=" + club.clubID);
									localDB.execSQL("UPDATE event SET followed=1 WHERE club_id=" + club.clubID);
									b.setBackgroundResource(R.drawable.followed_button);
									b.setTextColor(getResources().getColor(R.color.white));
									b.setText(R.string.followed);
									b.setTag("followed");
									fcmInstance.subscribeToTopic(clubFollowTopic);
								}
								else
								{
									// user was following, now he should unfollow
									club.followed = false;
									localDB.execSQL("UPDATE club SET followed=0 WHERE id=" + club.clubID);
									localDB.execSQL("UPDATE event SET followed=0 WHERE club_id=" + club.clubID);
									b.setBackgroundResource(R.drawable.not_followed_button);
									b.setTextColor(getResources().getColor(R.color.black));
									b.setText(R.string.follow);
									b.setTag("notfollowed");
									fcmInstance.unsubscribeFromTopic(clubFollowTopic);
								}
								// make the user follow or unfollow from notifications of all events by this club
								while(cr.moveToNext())
								{
									String eventFollowTopic = Topics.EVENT_FOLLOW + cr.getInt(cr.getColumnIndex("id")) + cr.getString(cr.getColumnIndex("alias"));
									if(follow)
									{
										fcmInstance.subscribeToTopic(eventFollowTopic);
									}
									else
									{
										fcmInstance.unsubscribeFromTopic(eventFollowTopic);
									}
								}
								myEventsFragment.addEventsToFragment();
							}
						}
						catch(JSONException e)
						{
							Toast.makeText(MainActivity.this, R.string.sentDataError, Toast.LENGTH_SHORT).show();
						}
					}
				},
				new Response.ErrorListener()
				{
					@Override
					public void onErrorResponse(VolleyError error)
					{
						Toast.makeText(MainActivity.this, R.string.slowInternet, Toast.LENGTH_SHORT).show();
					}
				})
		{
			@Override
			protected Map<String, String> getParams()
			{
				// Posting params to register url
				Map<String, String> params = new HashMap<>();
				params.put("clubid", club.clubID.toString());
				params.put("email", UserDetails.email);
				return params;
			}
		};

		AppController.getInstance().addToRequestQueue(req, "followClubRequest");
	}

	public static class MyEventsFragment extends CustomEventsFragment
	{
		public MyEventsFragment()
		{
			super();
		}

		@Override
		protected ArrayList<Event> getEvents()
		{
			ArrayList<Event> list = new ArrayList<>();
			Cursor cr = fragmentDB.rawQuery("SELECT * FROM event WHERE followed=1 ORDER BY event.date_time", null);
			addEventsFromCursor(cr, list);
			return list;
		}
	}

	public static class TodayFragment extends CustomEventsFragment
	{
		public TodayFragment()
		{
			super();
		}

		@Override
		protected ArrayList<Event> getEvents()
		{
			ArrayList<Event> list = new ArrayList<>();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String currentDate = sdf.format(new Date());
			String night[]={currentDate +" 00:00:00", currentDate + " 23:59:59"};
			//Cursor cr = fragmentDB.rawQuery("SELECT * FROM event ORDER BY date_time", null);
			Cursor cr = fragmentDB.rawQuery("SELECT * FROM event WHERE date_time>'" + night[0] + "' AND date_time<'" + night[1] + "' ORDER BY date_time", null);
			addEventsFromCursor(cr, list);
			return list;
		}
	}

	public static class UpcomingFragment extends CustomEventsFragment
	{
		public UpcomingFragment()
		{
			super();
		}

		@Override
		protected ArrayList<Event> getEvents()
		{
			ArrayList<Event> list = new ArrayList<>();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String currentDate = sdf.format(new Date());
			//Cursor cr = fragmentDB.rawQuery("SELECT * FROM event ORDER BY date_time", null);
			Cursor cr = fragmentDB.rawQuery("SELECT * FROM event WHERE date_time>'" + currentDate + " 23:59:59" + "' ORDER BY date_time", null);
			addEventsFromCursor(cr, list);
			return list;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == ADD_CLUB_CODE && resultCode == RESULT_OK)
		{
			refreshClubsList();
		}
	}
}