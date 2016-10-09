package com.hemal.eventhub2.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hemal.eventhub2.R;
import com.hemal.eventhub2.helper.gmailLetter.ColorGenerator;
import com.hemal.eventhub2.helper.gmailLetter.TextDrawable;
import com.hemal.eventhub2.model.Person;

import java.util.List;

/**
 * Created by Hemal on 09-Oct-16.
 */
public class CustomPeopleAdapter extends BaseAdapter {

	private Activity activity;
	private LayoutInflater inflater;
	private List<Person> personItems;

	public CustomPeopleAdapter(Activity activity, List<Person> personItems)
	{
		this.activity=activity;
		this.personItems=personItems;
	}


	@Override
	public int getCount() {
		return personItems.size();
	}


	@Override
	public Object getItem(int location) {
		return personItems.get(location);
	}


	@Override
	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (inflater == null)
			inflater = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null)
			convertView = inflater.inflate(R.layout.person_row, null);

		TextView personName = (TextView) convertView.findViewById(R.id.personName);
		ImageView personImage=(ImageView)convertView.findViewById(R.id.imagePerson);

		Person person=personItems.get(position);
		personName.setText(person.getName());


		ColorGenerator generator = ColorGenerator.MATERIAL;
		int charColor = generator.getRandomColor();
		TextDrawable drawable2 = TextDrawable.builder()
				.buildRound(String.valueOf(person.getName().charAt(0)).toUpperCase(), charColor);
		personImage.setImageDrawable(drawable2);
		return convertView;
	}
}
