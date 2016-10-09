package com.hemal.eventhub2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.hemal.eventhub2.adapters.CustomEventListAdapter;
import com.hemal.eventhub2.helper.network.ConnectionDetector;
import com.hemal.eventhub2.model.Event;

/**
 * Created by Hemal on 10-Oct-16.
 */
public class UpComingFragment extends Fragment
{

	ConnectionDetector cd;
	private List<Event> eventList;
	private ProgressDialog pDialog;

	private ListView listView;
	private CustomEventListAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.upcoming_fragment, container, false);
		listView= (ListView) rootView.findViewById(R.id.listUpcoming);
		eventList = new ArrayList<Event>();
		eventList.clear();
		pDialog = new ProgressDialog(getActivity());
		pDialog.setCancelable(false);
		adapter = new CustomEventListAdapter(getActivity(), eventList);
		listView.setAdapter(adapter);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentDateandTime = sdf.format(new Date());

		/*listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Intent intent = new Intent(getActivity(), AboutEventActivity.class);
				Event e = (Event) parent.getItemAtPosition(position);
				Log.d("tag", e.getEventName());
				intent.putExtra("id", e.getId());
				startActivity(intent);
			}

			@SuppressWarnings("unused")
			public void onClick(View v) {
			}


		});*/

		getData(currentDateandTime);
		return rootView;
	}


	private void getData(final String date) {


  /*      String tag_string_req = "req_event1";
        pDialog.setMessage("Getting Event Details ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                URL.geteventfuture, new Response.Listener<String>() {

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
                        "No up coming Events", Toast.LENGTH_LONG).show();
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