package com.hemal.eventhub2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class AboutClubActivity extends AppCompatActivity
{
	private Toolbar toolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_club);

		toolbar = (Toolbar) findViewById(R.id.aboutClubToolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
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
