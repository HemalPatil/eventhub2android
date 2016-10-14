package com.hemal.eventhub2;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.hemal.eventhub2.helper.DatabaseHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AboutEventActivity extends AppCompatActivity
{
	private Toolbar toolbar;
	private CollapsingToolbarLayout collapsingToolbarLayout;
	private SQLiteDatabase localDB;
	private Integer eventID;
	private String eventName;
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
	private boolean followed;
	private boolean imageDownloaded;

	private Button followButton;

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

		Cursor c = localDB.rawQuery("SELECT event.*, club.name AS club_name FROM event LEFT JOIN club ON event.club_id=club.id WHERE event.id=" + eventID, null);
		if(!c.moveToFirst())
		{
			return;
		}
		eventName = c.getString(c.getColumnIndex("name"));
		followed = c.getInt(c.getColumnIndex("followed")) == 1;
		imageDownloaded = c.getInt(c.getColumnIndex("image_downloaded")) == 1;
		eventVenue = c.getString(c.getColumnIndex("venue"));
		String dateTime = c.getString(c.getColumnIndex("date_time"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat sd1 = new SimpleDateFormat("yyyy-MM-dd");
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
}
