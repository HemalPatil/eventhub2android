package com.hemal.eventhub2;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.hemal.eventhub2.app.AppController;
import com.hemal.eventhub2.app.URL;
import com.hemal.eventhub2.app.UserDetails;
import com.hemal.eventhub2.helper.network.ConnectionDetector;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddClubActivity extends AppCompatActivity implements View.OnClickListener
{
	private SQLiteDatabase localDB;
	private ConnectionDetector cd;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_club);

		findViewById(R.id.addClubButton).setOnClickListener(this);

		localDB = AppController.getInstance().getLocalDB();
		cd = new ConnectionDetector(this);
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		if(id == R.id.addClubButton)
		{
			if(!cd.isConnectedToInternet())
			{
				Toast.makeText(this, R.string.noInternet, Toast.LENGTH_SHORT).show();
				return;
			}
			final String name = ((EditText)findViewById(R.id.clubNameInput)).getText().toString();
			final String alias = ((EditText)findViewById(R.id.clubAliasInput)).getText().toString();
			StringRequest req = new StringRequest(Request.Method.POST, URL.addClub,
					new Response.Listener<String>()
					{
						@Override
						public void onResponse(String response)
						{
							try
							{
								JSONObject jObj = new JSONObject(response);
								if(jObj.getInt("success") == 1)
								{
									ContentValues clubValues = new ContentValues();
									clubValues.put("id", jObj.getInt("clubid"));
									clubValues.put("name", name);
									clubValues.put("alias", alias);
									clubValues.put("followed", 0);
									localDB.insert("club", null, clubValues);
									Toast.makeText(AddClubActivity.this, R.string.clubAdded, Toast.LENGTH_SHORT).show();
									AddClubActivity.this.setResult(RESULT_OK);
									AddClubActivity.this.finish();
								}
								else
								{
									Toast.makeText(AddClubActivity.this, R.string.notAllowed, Toast.LENGTH_SHORT).show();
								}
							}
							catch(JSONException e)
							{
								e.printStackTrace();
							}
						}
					},
					new Response.ErrorListener()
					{
						@Override
						public void onErrorResponse(VolleyError error)
						{
							Toast.makeText(AddClubActivity.this, R.string.sentDataError, Toast.LENGTH_SHORT).show();
						}
					})
			{
				@Override
				protected Map<String, String> getParams()
				{
					EditText a = new EditText(AddClubActivity.this);
					// Posting params to add club url
					Map<String, String> params = new HashMap<>();
					params.put("name", name);
					params.put("alias", alias);
					params.put("email", UserDetails.email);
					return params;
				}
			};

			AppController.getInstance().addToRequestQueue(req, "addClubRequest");
		}
	}
}
