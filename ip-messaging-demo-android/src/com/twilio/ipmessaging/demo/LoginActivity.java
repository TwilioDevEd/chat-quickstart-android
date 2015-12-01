package com.twilio.ipmessaging.demo;

import java.net.URLEncoder;

import com.twilio.example.R;
import com.twilio.ipmessaging.demo.BasicIPMessagingClient.LoginListener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.Random;

public class LoginActivity extends Activity implements LoginListener {
	private static final Logger logger = Logger.getLogger(LoginActivity.class);
	public  boolean DevEnvV2 = false; 
	
	//test with shared instance 
	private static final String AUTH_PHP_SCRIPT = "https://twilio-ip-messaging-token.herokuapp.com/token?device=";

	private static final String DEFAULT_CLIENT_NAME = "TestUser";
	private ProgressDialog progressDialog;
	private Button login;
	private Button logout;
	private String capabilityToken = null;
	private EditText clientNameTextBox;
	private BasicIPMessagingClient chatClient;
	private String endpoint_id = "";
	public static String local_author = DEFAULT_CLIENT_NAME;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		this.clientNameTextBox = (EditText) findViewById(R.id.client_name);
		this.clientNameTextBox.setText(DEFAULT_CLIENT_NAME);
		this.endpoint_id = Secure.getString(this.getApplicationContext().getContentResolver(), Secure.ANDROID_ID);


				String idChosen = clientNameTextBox.getText().toString();
				String endpointIdFull = idChosen + "-" + LoginActivity.this.endpoint_id + "-android-"+ getApplication().getPackageName();

				StringBuilder url = new StringBuilder();
				url.append(AUTH_PHP_SCRIPT);
		        url.append(Secure.getString(this.getContentResolver(),
						Secure.ANDROID_ID));
//				url.append("&identity=");
//				url.append(URLEncoder.encode(idChosen));
//				url.append("&endpointId=" + URLEncoder.encode(endpointIdFull));
//				url.append(DEFAULT_CLIENT_NAME);
//				url.append("&endpoint_id=" + LoginActivity.this.endpoint_id);
				logger.e("url string : " + url.toString());
				new GetCapabilityTokenAsyncTask().execute(url.toString());



		this.logout = (Button) findViewById(R.id.logout);
		this.logout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				chatClient.cleanupTest();
			}
		});

		chatClient = TwilioApplication.get().getBasicClient();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class GetCapabilityTokenAsyncTask extends AsyncTask<String, Void, String> {

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			LoginActivity.this.chatClient.doLogin(capabilityToken, LoginActivity.this);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			LoginActivity.this.progressDialog = ProgressDialog.show(LoginActivity.this, "",
					"Connecting to #general channel", true);
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				capabilityToken = HttpHelper.httpGet(params[0]);
				logger.e("capabilityToken string : " + capabilityToken);
				chatClient.setCapabilityToken(capabilityToken);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return capabilityToken;
		}
	}

	@Override
	public void onLoginStarted() {
		logger.d("Log in started");
	}

	@Override
	public void onLoginFinished() {
		//LoginActivity.this.progressDialog.dismiss();
		Intent intent = new Intent(this, ChannelActivity.class);
		this.startActivity(intent);
	}

	@Override
	public void onLoginError(String errorMessage) {
		LoginActivity.this.progressDialog.dismiss();
		logger.e("Error logging in : " + errorMessage);
		Toast.makeText(getBaseContext(), errorMessage, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onLogoutFinished() {
		// TODO Auto-generated method stub

	}


}
