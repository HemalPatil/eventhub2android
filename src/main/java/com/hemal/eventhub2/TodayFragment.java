package com.hemal.eventhub2;

import android.app.ProgressDialog;
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
import com.hemal.eventhub2.helper.network.ConnectionDetector;
import com.hemal.eventhub2.model.Event;

/**
 * Created by Hemal on 10-Oct-16.
 */
public class TodayFragment extends Fragment
{

	ConnectionDetector cd;
	private List<Event> eventList;
	private ProgressDialog pDialog;
	int flag;
	private ListView listView;
	private CustomEventListAdapter adapter;
	TextView eventvisible;
	private SQLiteDatabase DBRead;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {


		View rootView = inflater.inflate(R.layout.today_fragment, container, false);
		listView= (ListView) rootView.findViewById(R.id.todayeventlist);
		eventList = new ArrayList<Event>();
		eventList.clear();
		flag=0;
		eventvisible=(TextView)rootView.findViewById(R.id.eventvisible);
		eventvisible.setVisibility(View.GONE);

		pDialog = new ProgressDialog(getActivity());
		pDialog.setCancelable(false);

		adapter = new CustomEventListAdapter(getActivity(), eventList);
		listView.setAdapter(adapter);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String currentDateandTime = sdf.format(new Date());
		Log.d("dasdasdasd", currentDateandTime);
		DatabaseHelper hp =new DatabaseHelper(getContext());
		DBRead= hp.getReadableDatabase();
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
		Cursor cr = DBRead.rawQuery("SELECT * FROM event"/* where date_time>'" + night[0] + "' AND date_time<'" + night[1] + "'"*/, null);

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
        /*String tag_string_req = "req_event";
        pDialog.setMessage("Getting Event Details ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                URL.todayevents, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("Tag_event", "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    if(jObj==null)
                    {
                        Toast.makeText(getActivity(), "Please check the network", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    JSONArray jsonArray=jObj.getJSONArray("events");
                    if(jsonArray.length()==0) {
                        eventvisible.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                    }
                    for(int i=0;i<jsonArray.length();i++)
                    {
                        JSONObject jsonObject=jsonArray.getJSONObject(i);
                        Event e=new Event();
                        e.setId(Integer.valueOf(jsonObject.getString("id")));
                        e.setEventName(jsonObject.getString("name"));
                        e.setEventVenue(jsonObject.getString("venue"));
                        String date=jsonObject.getString("date");
                        String[] arr=date.split("T");
                        date=arr[0]+"   "+arr[1].substring(0,5);
                        e.setEventTime(date);

                        eventList.add(e);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                adapter.notifyDataSetChanged();
                listView.invalidateViews();

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error", "Registration Error: " + error.getMessage());
                Toast.makeText(getActivity(),
                        "Network is too slow :(", Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("date", date);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);*/
	}

	private void showDialog() {
		if (!pDialog.isShowing())
			pDialog.show();
	}

	private void hideDialog() {
		if (pDialog.isShowing())
			pDialog.dismiss();
	}



}