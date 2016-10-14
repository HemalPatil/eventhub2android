package com.hemal.eventhub2;

import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class AboutEventActivity extends AppCompatActivity
{
	private Toolbar toolbar;
	private CollapsingToolbarLayout collapsingToolbarLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_event);

		collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.eventCollapsingBar);
		toolbar = (Toolbar) findViewById(R.id.eventToolbar);

		collapsingToolbarLayout.setCollapsedTitleTextColor(getResources().getColor(R.color.white));

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
