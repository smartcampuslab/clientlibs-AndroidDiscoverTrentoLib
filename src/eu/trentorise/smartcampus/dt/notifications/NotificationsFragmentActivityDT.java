package eu.trentorise.smartcampus.dt.notifications;

import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.android.feedback.activity.FeedbackFragmentActivity;
import eu.trentorise.smartcampus.dt.DTParamsHelper;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;

public class NotificationsFragmentActivityDT extends FeedbackFragmentActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notifications_fragment_dt);
		setUpContent();
		
		initDataManagement(savedInstanceState);
	}

	private void setUpContent() {
		// Action bar
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true); // system title
		actionBar.setHomeButtonEnabled(true);
		// actionBar.setDisplayShowHomeEnabled(true); // home icon bar
		actionBar.setDisplayHomeAsUpEnabled(true); // home as up
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);


	}

	private void initDataManagement(Bundle savedInstanceState) {
		try {
			if (!DTHelper.isInitialized()) {
				DTHelper.init(getApplicationContext());
				DTHelper.getAccessProvider().getAuthToken(this, null);
			}
		} catch (Exception e) {
			Toast.makeText(this, R.string.app_failure_init, Toast.LENGTH_LONG).show();
			return;
		}
	}

	@Override
	public String getAppToken() {
		return DTParamsHelper.getAppToken();
	}

	@Override
	public String getAuthToken() {
		return DTHelper.getAuthToken();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}
		else return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == SCAccessProvider.SC_AUTH_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				String token = data.getExtras().getString(AccountManager.KEY_AUTHTOKEN);
				if (token == null) {
					Toast.makeText(this, R.string.app_failure_security, Toast.LENGTH_LONG).show();
					finish();
				}
			} else if (resultCode == RESULT_CANCELED) {
				DTHelper.endAppFailure(this, R.string.token_required);
			}
		}
	}

}
