package com.hemal.eventhub2;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.hemal.eventhub2.adapters.CustomEventListAdapter;
import com.hemal.eventhub2.helper.DatabaseHelper;
import com.hemal.eventhub2.model.Event;

/**
 * Created by Hemal on 10-Oct-16.
 */
public class TodayFragment extends Fragment
{
	private List<Event> eventList;
	private ListView listView;
	private CustomEventListAdapter adapter;
	TextView noEvents;
	private SQLiteDatabase localDB;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.today_fragment, container, false);
		listView = (ListView) rootView.findViewById(R.id.todayEventList);
		eventList = new ArrayList<>();
		eventList.clear();
		noEvents = (TextView) rootView.findViewById(R.id.noEventsToday);
		noEvents.setVisibility(View.GONE);

		adapter = new CustomEventListAdapter(getActivity(), eventList, "today");
		listView.setAdapter(adapter);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String currentDateandTime = sdf.format(new Date());
		Log.d("dasdasdasd", currentDateandTime);
		DatabaseHelper hp = new DatabaseHelper(getContext());
		localDB = hp.getReadableDatabase();
		getData(currentDateandTime);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{

				Intent intent = new Intent(getActivity(), LoginActivity.class);
				Event e = (Event) parent.getItemAtPosition(position);
				Log.d("tag", e.getEventName());
				intent.putExtra("id", e.getId());
				startActivity(intent);
			}

			@SuppressWarnings("unused")
			public void onClick(View v)
			{
			}
		});

		return rootView;
	}


	private void getData(final String date) {

		Log.v("inget", "Date " + date) ;
		String night[]={date +" 00:00:00", date + " 23:59:59"};
		Cursor cr = localDB.rawQuery("SELECT * FROM event"/* where date_time>'" + night[0] + "' AND date_time<'" + night[1] + "'"*/, null);

		if(cr!=null && cr.moveToFirst())
		{
			Log.v("fetched", "TodayFragment cursor working");
			do
			{
				Event e = new Event();
				e.setId(Integer.valueOf(cr.getString(cr.getColumnIndex("id"))));
				e.setEventName(cr.getString(cr.getColumnIndex("name")));
				e.setEventVenue(cr.getString(cr.getColumnIndex("venue")));
				String DBdate = cr.getString(cr.getColumnIndex("date_time"));
				e.setEventTime(DBdate);
				eventList.add(e);
				Log.v("eventtoday", e.getEventName());
			}while(cr.moveToNext());
		}
	}
}