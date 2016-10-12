package com.hemal.eventhub2;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hemal.eventhub2.LoginActivity;
import com.hemal.eventhub2.MainActivity;
import com.hemal.eventhub2.R;
import com.hemal.eventhub2.adapters.CustomEventListAdapter;
import com.hemal.eventhub2.helper.DatabaseHelper;
import com.hemal.eventhub2.helper.network.ConnectionDetector;
import com.hemal.eventhub2.model.Event;

import java.util.ArrayList;

/**
 * Created by Hemal on 12-Oct-16.
 */
public abstract class CustomEventsFragment extends Fragment
{
	private final String FRAGMENT_TAG;
	private int refreshLayoutID;
	private int listViewID;
	private int noEventsID;
	private int refreshButtonID;
	private int fragmentID;
	private ArrayList<Event> eventList;
	private ListView listView;
	private SwipeRefreshLayout refreshLayout;
	private TextView noEvents;
	private Button refreshButton;
	private CustomEventListAdapter adapter;
	private ConnectionDetector cd;
	protected SQLiteDatabase fragmentDB;

	public CustomEventsFragment(int fragmentID, int refreshLayoutID, int listViewID, int noEventsID, int refreshButtonID, final String FRAGMENT_TAG)
	{
		this.fragmentID = fragmentID;
		this.refreshLayoutID = refreshLayoutID;
		this.listViewID = listViewID;
		this.noEventsID = noEventsID;
		this.refreshButtonID = refreshButtonID;
		this.FRAGMENT_TAG = FRAGMENT_TAG;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance)
	{
		View rootView = inflater.inflate(fragmentID, container, false);
		refreshLayout = (SwipeRefreshLayout) rootView.findViewById(refreshLayoutID);
		listView = (ListView) rootView.findViewById(listViewID);
		noEvents = (TextView) rootView.findViewById(noEventsID);
		refreshButton = (Button) rootView.findViewById(refreshButtonID);

		refreshLayout.setVisibility(View.GONE);
		noEvents.setVisibility(View.VISIBLE);
		refreshButton.setVisibility(View.VISIBLE);

		eventList = new ArrayList<>();
		eventList.clear();

		adapter = new CustomEventListAdapter(getActivity(), eventList, FRAGMENT_TAG);
		listView.setAdapter(adapter);

		cd = new ConnectionDetector(getActivity());
		refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
		{
			@Override
			public void onRefresh()
			{
				if(cd.isConnectedToInternet())
				{
					((MainActivity)getActivity()).syncDatabase();
				}
				else
				{
					Toast.makeText(getActivity(), R.string.noInternet, Toast.LENGTH_SHORT).show();
				}
			}
		});

		DatabaseHelper hp = new DatabaseHelper(getActivity());
		fragmentDB = hp.getReadableDatabase();

		addEventsToFragment();

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				// TODO : replace the LoginActivity class with AboutEventActivity
				Intent intent = new Intent(getActivity(), LoginActivity.class);
				Event e = (Event) parent.getItemAtPosition(position);
				Log.d("tag", e.getEventName());
				intent.putExtra("id", e.getId());
				startActivity(intent);
			}
		});

		return rootView;
	}

	public boolean isRefreshing()
	{
		return refreshLayout.isRefreshing();
	}

	public void setRefreshing(boolean refreshing)
	{
		refreshLayout.setRefreshing(refreshing);
	}

	protected abstract Cursor getCursor();

	public void addEventsToFragment()
	{
		Cursor c = getCursor();

		eventList.clear();
		while(c.moveToNext())
		{
			Event e = new Event();
			e.setId(Integer.valueOf(c.getString(c.getColumnIndex("id"))));
			e.setEventName(c.getString(c.getColumnIndex("name")));
			e.setEventVenue(c.getString(c.getColumnIndex("venue")));
			String DBDate = c.getString(c.getColumnIndex("date_time"));
			e.setEventTime(DBDate);
			eventList.add(e);
			Log.v("event" + FRAGMENT_TAG, e.getEventName());
		}
		adapter.notifyDataSetChanged();

		if(refreshLayout.isRefreshing())
		{
			refreshLayout.setRefreshing(false);
		}
		if(eventList.size() > 0)
		{
			noEvents.setVisibility(View.GONE);
			refreshButton.setVisibility(View.GONE);
			refreshLayout.setVisibility(View.VISIBLE);
		}
		else
		{
			refreshLayout.setVisibility(View.GONE);
			noEvents.setVisibility(View.VISIBLE);
			refreshButton.setVisibility(View.VISIBLE);
		}
	}
}
