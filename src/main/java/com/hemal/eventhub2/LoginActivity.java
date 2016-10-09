package com.hemal.eventhub2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.hemal.eventhub2.helper.network.ConnectionDetector;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener
{
	private static final int RC_SIGN_IN = 9001;

	private GoogleApiClient mGoogleApiClient;
	private ConnectionDetector cd;
	private String name;
	private String email;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		cd = new ConnectionDetector(getApplicationContext());

		// google sign-in
		findViewById(R.id.sign_in_button).setOnClickListener(this);
		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();

		mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

		SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
		signInButton.setSize(SignInButton.SIZE_STANDARD);
		signInButton.setScopes(gso.getScopeArray());
	}

	@Override
	public void onClick(View v)
	{
		switch(v.getId())
		{
			case R.id.sign_in_button:
				if(!cd.isConnectedToInternet())
				{
					showAlert();
				}
				else
				{
					signIn();
				}
				break;
		}
	}

	private void signIn()
	{
		Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
		startActivityForResult(signInIntent, RC_SIGN_IN);
	}

	@Override
	public void onActivityResult(int RequestCode, int ResultCode, Intent i)
	{
		super.onActivityResult(RequestCode, ResultCode, i);

		if(RequestCode == RC_SIGN_IN)
		{
			GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(i);
			handleSignInResult(result);
		}
	}

	private void handleSignInResult(GoogleSignInResult result)
	{
		if(result.isSuccess())
		{
			// sign-in successful
			GoogleSignInAccount account = result.getSignInAccount();
			name = account.getDisplayName();
			email = account.getEmail();
			updateUI();
		}
	}

	private void updateUI()
	{
		SharedPreferences.Editor preferencesEditor = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE).edit();
		preferencesEditor.putString("email", email);
		preferencesEditor.putString("name", name);
		preferencesEditor.commit();

		// TODO : add Google push notifications

		// user information is gathered, take the user to MainActivity
		startActivity(new Intent(this, MainActivity.class));
		finish();
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult)
	{
		showAlert();
	}

	private void showAlert()
	{
		new AlertDialog.Builder(this)
				.setTitle("Sign-in failed")
				.setMessage("Cannot connect to Google services. Check your network connection.")
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton("OK", null)
				.show();
	}
}
