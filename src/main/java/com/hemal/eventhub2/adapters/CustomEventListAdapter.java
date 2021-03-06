package com.hemal.eventhub2.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hemal.eventhub2.R;
import com.hemal.eventhub2.helper.gmailLetter.ColorGenerator;
import com.hemal.eventhub2.helper.gmailLetter.TextDrawable;
import com.hemal.eventhub2.model.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Hemal on 09-Oct-16.
 */
public class CustomEventListAdapter extends BaseAdapter
{
	private Activity activity;
	private LayoutInflater inflater;
	private ArrayList<Event> eventItems;
	private String TAG;

	// TODO : remove string tag in production code
	public CustomEventListAdapter(Activity activity, ArrayList<Event> eventItems, final String x)
	{
		this.activity = activity;
		this.eventItems = eventItems;
		this.TAG = x;
		Log.v("customevent", "number of events : " + this.eventItems.size() + " " + TAG);
	}

	@Override
	public int getCount() {
		return eventItems.size();
	}

	@Override
	public Object getItem(int location) {
		return eventItems.get(location);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		Log.v("customevent", "get view called " + TAG);
		if (inflater == null)
		{
			inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		if (convertView == null)
		{
			convertView = inflater.inflate(R.layout.event_row, null);
		}

		TextView eventName = (TextView) convertView.findViewById(R.id.eventName);
		TextView eventTime = (TextView) convertView.findViewById(R.id.eventTime);
		TextView eventVenue = (TextView) convertView.findViewById(R.id.eventVenue);
		ImageView icon=(ImageView)convertView.findViewById(R.id.imageIcon);
		Event event = eventItems.get(position);
		eventName.setText(event.getEventName());
		eventVenue.setText(event.getEventVenue());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat sd1 = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
		try
		{
			eventTime.setText(sd1.format(sdf.parse(event.getEventTime())));
		}
		catch (ParseException e)
		{
			eventTime.setText(event.getEventTime());
		}

		ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
		int charColor = generator.getRandomColor(); //get color for charecter
		TextDrawable drawable = TextDrawable.builder()
				.buildRound(String.valueOf(event.getEventName().charAt(0)).toUpperCase(), charColor);
		icon.setImageDrawable(drawable);

		return convertView;
	}
}
