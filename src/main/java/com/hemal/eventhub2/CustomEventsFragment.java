package com.hemal.eventhub2;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hemal.eventhub2.adapters.CustomEventListAdapter;
import com.hemal.eventhub2.app.AppController;
import com.hemal.eventhub2.helper.network.ConnectionDetector;
import com.hemal.eventhub2.model.Event;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Hemal on 12-Oct-16.
 */
public abstract class CustomEventsFragment extends Fragment
{
	private static final int ABOUT_EVENT_CODE = 0xc0de;
	private String FRAGMENT_TAG = null;
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

	public CustomEventsFragment()
	{
		super();
	}

	@Override
	public void onCreate(Bundle savedInstance)
	{
		super.onCreate(savedInstance);

		Bundle b = this.getArguments();
		this.fragmentID = b.getInt("fragmentLayout");
		this.refreshLayoutID = b.getInt("refreshLayout");
		this.listViewID = b.getInt("listView");
		this.noEventsID = b.getInt("noEvents");
		this.refreshButtonID = b.getInt("refreshButton");
		this.FRAGMENT_TAG = b.getString("tag");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance)
	{
		View rootView = inflater.inflate(fragmentID, container, false);
		refreshLayout = (SwipeRefreshLayout) rootView.findViewById(refreshLayoutID);
		listView = (ListView) rootView.findViewById(listViewID);
		noEvents = (TextView) rootView.findViewById(noEventsID);
		refreshButton = (Button) rootView.findViewById(refreshButtonID);
		cd = new ConnectionDetector(getActivity());

		refreshLayout.setVisibility(View.GONE);
		noEvents.setVisibility(View.VISIBLE);
		refreshButton.setVisibility(View.VISIBLE);

		eventList = new ArrayList<>();
		eventList.clear();

		adapter = new CustomEventListAdapter(getActivity(), eventList, FRAGMENT_TAG);
		listView.setAdapter(adapter);

		refreshButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				refreshFragment();
			}
		});
		refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
		{
			@Override
			public void onRefresh()
			{
				refreshFragment();
			}
		});

		fragmentDB = AppController.getInstance().getLocalDB();

		addEventsToFragment();

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Intent intent = new Intent(getActivity(), AboutEventActivity.class);
				Event e = (Event) parent.getItemAtPosition(position);
				intent.putExtra("eventID", e.getId());
				startActivityForResult(intent, ABOUT_EVENT_CODE);
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

	protected static void addEventsFromCursor(Cursor cr, ArrayList<Event> list)
	{
		while(cr.moveToNext())
		{
			Event e = new Event();
			e.setId(cr.getInt(cr.getColumnIndex("id")));
			e.setEventName(cr.getString(cr.getColumnIndex("name")));
			e.setEventVenue(cr.getString(cr.getColumnIndex("venue")));
			e.setEventTime(cr.getString(cr.getColumnIndex("date_time")));
			e.followed = cr.getInt(cr.getColumnIndex("followed")) == 1;
			list.add(e);
		}
	}

	protected abstract ArrayList<Event> getEvents();

	public void addEventsToFragment()
	{
		ArrayList<Event> newList = getEvents();
		eventList.clear();
		eventList.addAll(newList);
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

	private void refreshFragment()
	{
		if(cd.isConnectedToInternet())
		{
			Toast.makeText(getActivity(), R.string.refreshing, Toast.LENGTH_SHORT).show();
			((MainActivity)getActivity()).syncDatabase();
		}
		else
		{
			Toast.makeText(getActivity(), R.string.noInternet, Toast.LENGTH_SHORT).show();
			if(refreshLayout.isRefreshing())
			{
				refreshLayout.setRefreshing(false);
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == ABOUT_EVENT_CODE && resultCode == RESULT_OK)
		{
			((MainActivity)getActivity()).refreshMyEventsFragment();
		}
	}
}
