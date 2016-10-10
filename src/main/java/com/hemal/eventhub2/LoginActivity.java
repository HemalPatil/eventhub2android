package com.hemal.eventhub2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.hemal.eventhub2.app.UserDetails;
import com.hemal.eventhub2.helper.network.ConnectionDetector;
import com.hemal.eventhub2.helper.network.ServerUtilities;

import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener
{
	private static final int RC_SIGN_IN = 9001;
	private static final String GOOGLE_SIGNIN_FAILED = "Cannot connect to Google services. Check your network connection.";
	private static final String BACKEND_REGISTER_FAILED = "Cannot connect to Event Hub server.";
	private static final String NO_INTERNET = "There is no Internet connection.";

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
		final String id = getResources().getString(R.string.server_client_id);
		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().requestIdToken(id).build();

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
					showAlert(NO_INTERNET);
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

			UserDetails.email = email;
			UserDetails.name = name;

			// TODO : register the google sign-in of the user
			if(!registerGoogleSignIn(name, email))
			{
				showAlert(BACKEND_REGISTER_FAILED);
				return;
			}

			// add the user details to the shared preferences of the app
			SharedPreferences.Editor preferencesEditor = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE).edit();
			preferencesEditor.putString("email", email);
			preferencesEditor.putString("name", name);
			preferencesEditor.commit();

			// TODO : add Google push notifications
			FirebaseAuth fcmAuth = FirebaseAuth.getInstance();
			Log.v("accounttoken", "lolz" + account.getIdToken() + "lolz");
			AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
			fcmAuth.signInWithCredential(credential)
					.addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
					{
						@Override
						public void onComplete(@NonNull Task<AuthResult> task)
						{
							Log.v("fcmsignin", "signInWithCredential:onComplete:" + task.isSuccessful());

							// If sign in fails, display a message to the user. If sign in succeeds
							// the auth state listener will be notified and logic to handle the
							// signed in user can be handled in the listener.
							if (task.isSuccessful())
							{
								Log.v("fcmsignin", "signInWithCredential successful");
								// the generated/refreshed token is handled by onTokenRefresh()
								// of FCMInstanceIdService. So we are decoupling the FCM token
								// registration process and google sign-in registration process
								// in Event Hub 2.0
							} else
							{
								Log.v("fcmsignin", "signInWithCredential failed", task.getException());
								LoginActivity.this.showAlert(GOOGLE_SIGNIN_FAILED);
							}
						}
					});

			// user information is gathered, take the user to MainActivity
			startActivity(new Intent(this, MainActivity.class));
			finish();
		}
		else
		{
			showAlert(GOOGLE_SIGNIN_FAILED);
		}
	}

	private boolean registerGoogleSignIn(final String name, final String email)
	{
		try
		{
			return new register().execute().get();
		}
		catch(ExecutionException e)
		{
			showAlert(BACKEND_REGISTER_FAILED);
		}
		catch(InterruptedException e)
		{
			showAlert(BACKEND_REGISTER_FAILED);
		}
		return false;
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult)
	{
		showAlert(GOOGLE_SIGNIN_FAILED);
	}

	private void showAlert(final String msg)
	{
		new AlertDialog.Builder(this)
				.setTitle("Sign-in failed")
				.setMessage(msg)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton("OK", null)
				.show();
	}

	private class register extends AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(Void... params)
		{
			if(!ServerUtilities.registerGoogleSignIn(name, email))
			{
				return false;
			}
			return ServerUtilities.registerFCMToken(email, UserDetails.fcmtoken);
		}
	}
}
