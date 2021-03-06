package com.hemal.eventhub2;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.hemal.eventhub2.app.AppController;

public class AboutClubActivity extends AppCompatActivity
{
	private Toolbar toolbar;
	private int clubID;

	private SQLiteDatabase localDB;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_club);

		toolbar = (Toolbar) findViewById(R.id.aboutClubToolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);

		Intent intent = getIntent();
		clubID = intent.getIntExtra("clubID", 0xffff);
		if(clubID == 0xffff)
		{
			// should never happen
			return;
		}

		localDB = AppController.getInstance().getLocalDB();
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
