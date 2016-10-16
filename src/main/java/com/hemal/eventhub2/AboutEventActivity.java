package com.hemal.eventhub2;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hemal.eventhub2.app.AppController;
import com.hemal.eventhub2.app.Topics;
import com.hemal.eventhub2.app.URL;
import com.hemal.eventhub2.app.UserDetails;
import com.hemal.eventhub2.helper.DatabaseHelper;
import com.hemal.eventhub2.helper.network.ConnectionDetector;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AboutEventActivity extends AppCompatActivity
{
	private final static int EVENT_FOLLOWED = 0xbeef;
	private final static int EVENT_NOT_FOLLOWED = 0xcafe;
	private Toolbar toolbar;
	private CollapsingToolbarLayout collapsingToolbarLayout;
	private SQLiteDatabase localDB;
	private Integer eventID;
	private String eventName;
	private String eventAlias;
	private String eventVenue;
	private String eventDate;
	private String eventTime;
	private String clubName;
	private String eventDescription;
	private String contactName1;
	private String contactNumber1;
	private String contactName2;
	private String contactNumber2;
	private Date eventDateTime;
	private String eventFollowTopic;
	private boolean followed;
	private boolean imageDownloaded;

	private Button followButton;
	private ConnectionDetector cd;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_event);

		collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.eventCollapsingBar);
		toolbar = (Toolbar) findViewById(R.id.eventToolbar);

		collapsingToolbarLayout.setCollapsedTitleTextColor(getResources().getColor(R.color.white));
		collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.AboutActivityTheme_ActionBarStyle_TitleTextStyle);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);

		Intent intent = getIntent();
		eventID = intent.getIntExtra("eventID", 0xffffff);

		if(eventID == 0xffffff)
		{
			// Should never happen, because event should always be present for corresponding call to this activity
			return;
		}

		DatabaseHelper helper = new DatabaseHelper(this);
		localDB = helper.getWritableDatabase();

		cd = new ConnectionDetector(this);

		Cursor c = localDB.rawQuery("SELECT event.*, club.name AS club_name FROM event LEFT JOIN club ON event.club_id=club.id WHERE event.id=" + eventID, null);
		if(!c.moveToFirst())
		{
			return;
		}
		eventName = c.getString(c.getColumnIndex("name"));
		eventAlias = c.getString(c.getColumnIndex("alias"));
		eventFollowTopic = Topics.EVENT_FOLLOW + eventID + eventAlias;
		followed = c.getInt(c.getColumnIndex("followed")) == 1;
		imageDownloaded = c.getInt(c.getColumnIndex("image_downloaded")) == 1;
		eventVenue = c.getString(c.getColumnIndex("venue"));
		String dateTime = c.getString(c.getColumnIndex("date_time"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat sd1 = new SimpleDateFormat("dd MMM yyyy");
		SimpleDateFormat sd2 = new SimpleDateFormat("HH:mm:ss");
		try
		{
			eventDateTime = sdf.parse(dateTime);
		}
		catch(ParseException e)
		{
			return;
		}
		eventDate = sd1.format(eventDateTime);
		eventTime = sd2.format(eventDateTime);
		clubName = c.getString(c.getColumnIndex("club_name"));
		eventDescription = c.getString(c.getColumnIndex("description"));
		contactName1 = c.getString(c.getColumnIndex("contact_name_1"));
		contactNumber1 = c.getString(c.getColumnIndex("contact_number_1"));
		contactName2 = c.getString(c.getColumnIndex("contact_name_2"));
		contactNumber2 = c.getString(c.getColumnIndex("contact_number_2"));

		followButton = (Button) findViewById(R.id.eventFollow);
		((TextView)findViewById(R.id.eventVenue)).setText(eventVenue);
		collapsingToolbarLayout.setTitle(eventName);
		((TextView)findViewById(R.id.eventDate)).setText(eventDate);
		((TextView)findViewById(R.id.eventTime)).setText(eventTime);
		((TextView)findViewById(R.id.byClubName)).setText(clubName);
		((TextView)findViewById(R.id.eventDescription)).setText(eventDescription);
		((TextView)findViewById(R.id.contact1Name)).setText(contactName1);
		((TextView)findViewById(R.id.contact1Number)).setText(contactNumber1);
		((TextView)findViewById(R.id.contact2Name)).setText(contactName2);
		((TextView)findViewById(R.id.contact2Number)).setText(contactNumber2);

		if(followed)
		{
			followButton.setBackgroundResource(R.drawable.followed_button);
			followButton.setTextColor(getResources().getColor(R.color.white));
			followButton.setText(R.string.followed);
			followButton.setTag("followed");
		}
		else
		{
			followButton.setBackgroundResource(R.drawable.not_followed_button);
			followButton.setTextColor(getResources().getColor(R.color.black));
			followButton.setText(R.string.follow);
			followButton.setTag("notfollowed");
		}
		followButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Log.v("eventfollow", "button listener called");
				if(cd.isConnectedToInternet())
				{
					sendEventFollowRequest();
				}
				else
				{
					Toast.makeText(AboutEventActivity.this, R.string.noInternet, Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private void sendEventFollowRequest()
	{
		String requestUrl;
		if(!followed)
		{
			requestUrl = URL.followEvent;
		}
		else
		{
			requestUrl = URL.unFollowEvent;
		}
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
								if(!followed)
								{
									// user was not following, now he should follow
									followed = true;
									localDB.execSQL("UPDATE event SET followed=1 WHERE id=" + eventID);
									followButton.setBackgroundResource(R.drawable.followed_button);
									followButton.setTextColor(getResources().getColor(R.color.white));
									followButton.setText(R.string.followed);
									followButton.setTag("followed");
									FirebaseMessaging.getInstance().subscribeToTopic(eventFollowTopic);
								}
								else
								{
									// user was following, now he should unfollow
									followed = false;
									localDB.execSQL("UPDATE event SET followed=0 WHERE id=" + eventID);
									followButton.setBackgroundResource(R.drawable.not_followed_button);
									followButton.setTextColor(getResources().getColor(R.color.black));
									followButton.setText(R.string.follow);
									followButton.setTag("notfollowed");
									FirebaseMessaging.getInstance().unsubscribeFromTopic(eventFollowTopic);
								}
							}
						}
						catch(JSONException e)
						{
							Toast.makeText(AboutEventActivity.this, R.string.sentDataError, Toast.LENGTH_SHORT).show();
						}
					}
				},
				new Response.ErrorListener()
				{
					@Override
					public void onErrorResponse(VolleyError error)
					{
						Toast.makeText(AboutEventActivity.this, R.string.slowInternet, Toast.LENGTH_SHORT).show();
					}
				})
		{
			@Override
			protected Map<String, String> getParams()
			{
				// Posting params to register url
				Map<String, String> params = new HashMap<>();
				params.put("eventid", eventID.toString());
				params.put("email", UserDetails.email);
				return params;
			}
		};

		AppController.getInstance().addToRequestQueue(req, "followEventRequest");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId() == android.R.id.home)
		{
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		if(followed)
		{
			setResult(EVENT_FOLLOWED);
		}
		else
		{
			setResult(EVENT_NOT_FOLLOWED);
		}
	}
}
