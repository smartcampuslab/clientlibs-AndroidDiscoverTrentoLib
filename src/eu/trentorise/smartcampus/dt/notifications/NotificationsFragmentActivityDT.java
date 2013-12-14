package eu.trentorise.smartcampus.dt.notifications;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.android.feedback.activity.FeedbackFragmentActivity;
import eu.trentorise.smartcampus.dt.DTParamsHelper;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;

public class NotificationsFragmentActivityDT extends FeedbackFragmentActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			startFragment();
		}
		initDataManagement(savedInstanceState);
		setUpContent();
	}

	private void initDataManagement(Bundle savedInstanceState) {
		try {
			DTHelper.init(getApplicationContext());
		} catch (Exception e) {
			Toast.makeText(this, R.string.app_failure_init, Toast.LENGTH_LONG).show();
			return;
		}
	}
	/**
	 * 
	 */
	private void startFragment() {
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		NotificationsFragmentListDT fragment = new NotificationsFragmentListDT();
		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.replace(R.id.fragment_container, fragment);
		// fragmentTransaction.addToBackStack(fragment.getTag());
		fragmentTransaction.commit();
	}

	private void setUpContent() {
		setContentView(R.layout.main);
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

	
}
