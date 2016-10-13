package com.hemal.eventhub2.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.hemal.eventhub2.R;
import com.hemal.eventhub2.model.Club;

import java.util.ArrayList;

/**
 * Created by Hemal on 13-Oct-16.
 */
public class CustomClubListAdapter extends BaseAdapter
{
	private Activity activity;
	private LayoutInflater inflater;
	private ArrayList<Club> clubs;

	public CustomClubListAdapter(Activity activity, ArrayList<Club> clubs)
	{
		this.activity = activity;
		this.clubs = clubs;
	}

	@Override
	public int getCount() {
		return clubs.size();
	}

	@Override
	public Object getItem(int location) {
		return clubs.get(location);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (inflater == null)
		{
			inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		if (convertView == null)
		{
			convertView = inflater.inflate(R.layout.club_row, null);
		}

		TextView clubName = (TextView) convertView.findViewById(R.id.clubName);
		Button clubFollowButton = (Button) convertView.findViewById(R.id.clubFollow);

		Club c = clubs.get(position);
		clubName.setText(c.name);
		if(c.followed)
		{
			clubFollowButton.setBackgroundResource(R.drawable.followed_button);
			clubFollowButton.setTextColor(activity.getResources().getColor(R.color.white));
			clubFollowButton.setText(R.string.followed);
			clubFollowButton.setTag("followed");
		}
		else
		{
			clubFollowButton.setBackgroundResource(R.drawable.not_followed_button);
			clubFollowButton.setTextColor(activity.getResources().getColor(R.color.black));
			clubFollowButton.setText(R.string.follow);
			clubFollowButton.setTag("notfollowed");
		}

		return convertView;
	}
}
