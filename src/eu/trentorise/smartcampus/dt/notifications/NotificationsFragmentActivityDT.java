package eu.trentorise.smartcampus.dt.notifications;

import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.android.feedback.activity.FeedbackFragmentActivity;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.data.Constants;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;

public class NotificationsFragmentActivityDT extends FeedbackFragmentActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notifications_fragment_dt);
		setUpContent();
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

	@Override
	public String getAppToken() {
		return Constants.APP_TOKEN;
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

}
