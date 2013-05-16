/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.dt;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.maps.MapView;

import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.feedback.activity.FeedbackFragmentActivity;
import eu.trentorise.smartcampus.dt.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.dt.custom.TabListener;
import eu.trentorise.smartcampus.dt.custom.data.Constants;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
import eu.trentorise.smartcampus.dt.custom.map.MapManager;
import eu.trentorise.smartcampus.dt.fragments.events.AllEventsFragment;
import eu.trentorise.smartcampus.dt.fragments.events.EventDetailsFragment;
import eu.trentorise.smartcampus.dt.fragments.home.HomeFragment;
import eu.trentorise.smartcampus.dt.fragments.pois.AllPoisFragment;
import eu.trentorise.smartcampus.dt.fragments.pois.PoiDetailsFragment;
import eu.trentorise.smartcampus.dt.fragments.stories.AllStoriesFragment;
import eu.trentorise.smartcampus.dt.fragments.stories.StoryDetailsFragment;
import eu.trentorise.smartcampus.dt.model.BaseDTObject;
import eu.trentorise.smartcampus.dt.model.EventObject;
import eu.trentorise.smartcampus.dt.model.POIObject;
import eu.trentorise.smartcampus.dt.model.StoryObject;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class DiscoverTrentoActivity extends FeedbackFragmentActivity {

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("tag", getSupportActionBar()
				.getSelectedNavigationIndex());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setUpContent(savedInstanceState != null ? savedInstanceState
				.getInt("tag") : null);

		initDataManagement(savedInstanceState);
		MapManager.setMapView(new MapView(this, getResources().getString(
				R.string.maps_api_key)));

	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this); // Add this method.
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this); // Add this method.
	}

	@Override
	protected void onResume() {
		if (DTHelper.getLocationHelper() != null)
			DTHelper.getLocationHelper().start();
		super.onResume();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		} else
			return super.onOptionsItemSelected(item);

	}

	@Override
	protected void onPause() {
		if (DTHelper.getLocationHelper() != null)
			DTHelper.getLocationHelper().stop();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		DTHelper.destroy();
		super.onDestroy();
	}

	private void initDataManagement(Bundle savedInstanceState) {
		try {
			DTHelper.init(getApplicationContext());
			String token = DTHelper.getAccessProvider()
					.getAuthToken(this, null);
			if (token != null) {
				initData(token);
			}
		} catch (Exception e) {
			Toast.makeText(this, R.string.app_failure_init, Toast.LENGTH_LONG)
					.show();
			return;
		}
	}

	private boolean initData(String token) {
		try {
			new SCAsyncTask<Void, Void, BaseDTObject>(this,
					new LoadDataProcessor(this)).execute();
		} catch (Exception e1) {
			Toast.makeText(this, R.string.app_failure_init, Toast.LENGTH_LONG)
					.show();
			return false;
		}
		return true;
	}

	private void setUpContent(Integer pos) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setSupportProgressBarIndeterminateVisibility(false);

		setContentView(R.layout.main);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true); // system title
		actionBar.setDisplayShowHomeEnabled(true); // home icon bar
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS); // tabs bar

		// Home
		ActionBar.Tab tab = actionBar.newTab();
		tab.setText(R.string.tab_home);
		tab.setTabListener(new TabListener<HomeFragment>(this, "me",
				HomeFragment.class));
		actionBar.addTab(tab);

		// Points of interest
		tab = actionBar.newTab();
		tab.setText(R.string.tab_places);
		tab.setTabListener(new TabListener<AllPoisFragment>(this, "pois",
				AllPoisFragment.class));
		actionBar.addTab(tab);

		// Events
		tab = actionBar.newTab();
		tab.setText(R.string.tab_events);
		tab.setTabListener(new TabListener<AllEventsFragment>(this, "events",
				AllEventsFragment.class));
		actionBar.addTab(tab);

		// Stories
		tab = getSupportActionBar().newTab();
		tab.setText(R.string.tab_stories);
		tab.setTabListener(new TabListener<AllStoriesFragment>(this, "stories",
				AllStoriesFragment.class));
		actionBar.addTab(tab);

		if (pos != null)
			actionBar.selectTab(actionBar.getTabAt(pos));
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.emptymenu, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			String token = data.getExtras().getString(
					AccountManager.KEY_AUTHTOKEN);
			if (token == null) {
				Toast.makeText(this, R.string.app_failure_security,
						Toast.LENGTH_LONG).show();
				finish();
			} else {
				initData(token);
			}
		} else if (resultCode == RESULT_CANCELED
				&& requestCode == SCAccessProvider.SC_AUTH_ACTIVITY_REQUEST_CODE) {
			DTHelper.endAppFailure(this, R.string.token_required);
		}
	}

	private class LoadDataProcessor extends
			AbstractAsyncTaskProcessor<Void, BaseDTObject> {

		private int syncRequired = 0;
		private SherlockFragmentActivity currentRootActivity = null;

		public LoadDataProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public BaseDTObject performAction(Void... params)
				throws SecurityException, Exception {
			Long entityId = getIntent().getLongExtra(
					getString(R.string.view_intent_arg_entity_id), -1);
			String type = getIntent().getStringExtra(
					getString(R.string.view_intent_arg_entity_type));

			Exception res = null;

			try {
				syncRequired = DTHelper.syncRequired();
			} catch (Exception e) {
				res = e;
			}

			if (entityId > 0 && type != null) {
				if ("event".equals(type))
					return DTHelper.findEventByEntityId(entityId);
				else if ("location".equals(type))
					return DTHelper.findPOIByEntityId(entityId);
				else if ("narrative".equals(type))
					return DTHelper.findStoryByEntityId(entityId);
			} else if (res != null) {
				throw res;
			}
			return null;
		}

		@Override
		public void handleResult(BaseDTObject result) {
			if (syncRequired != DTHelper.SYNC_NOT_REQUIRED) {
				if (syncRequired == DTHelper.SYNC_REQUIRED_FIRST_TIME) {
					Toast.makeText(DiscoverTrentoActivity.this,
							R.string.initial_data_load, Toast.LENGTH_LONG)
							.show();
				}
				setSupportProgressBarIndeterminateVisibility(true);
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							currentRootActivity = DTHelper
									.start(DiscoverTrentoActivity.this);
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if (currentRootActivity != null) {
								currentRootActivity
										.runOnUiThread(new Runnable() {
											@Override
											public void run() {
												currentRootActivity
														.setSupportProgressBarIndeterminateVisibility(false);
											}
										});
							}
						}
					}
				}).start();
			}

			Long entityId = getIntent().getLongExtra(
					getString(R.string.view_intent_arg_entity_id), -1);
			if (entityId > 0) {
				if (result == null) {
					Toast.makeText(DiscoverTrentoActivity.this,
							R.string.app_failure_obj_not_found,
							Toast.LENGTH_LONG).show();
					return;
				}

				SherlockFragment fragment = null;
				String tag = null;
				Bundle args = new Bundle();
				if (result instanceof POIObject) {
					fragment = new PoiDetailsFragment();
					args.putSerializable(PoiDetailsFragment.ARG_POI, result);
					tag = "pois";
				} else if (result instanceof EventObject) {
					fragment = new EventDetailsFragment();
					args.putString(EventDetailsFragment.ARG_EVENT_OBJECT,
							(result.getId()));
					tag = "events";
				} else if (result instanceof StoryObject) {
					fragment = new StoryDetailsFragment();
					args.putSerializable(StoryDetailsFragment.ARG_STORY, result);
					tag = "stories";
					// } else if (result instanceof StoryObject) {
					// fragment = new EventDetailsFragment();
					// args.putSerializable(StoryDetailsFragment.ARG_STORY_OBJECT,
					// result);
					// tag = "stories";
				}
				if (fragment != null) {
					FragmentTransaction fragmentTransaction = getSupportFragmentManager()
							.beginTransaction();
					fragment.setArguments(args);

					fragmentTransaction
							.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					fragmentTransaction.replace(android.R.id.content, fragment,
							tag);
					fragmentTransaction.addToBackStack(fragment.getTag());
					fragmentTransaction.commit();
				}
			}
		}

	}

	@Override
	public void onNewIntent(Intent intent) {
		try {
			DTHelper.getAccessProvider().getAuthToken(this, null);
		} catch (Exception e) {
			Toast.makeText(this, R.string.app_failure_init, Toast.LENGTH_LONG)
					.show();
			return;
		}

	}

	/*
	 * @Override protected void onResume() { super.onResume();
	 * 
	 * try { DTHelper.init(getApplicationContext()); String token =
	 * DTHelper.getAccessProvider().getAuthToken(this, null); if (token != null)
	 * { initData(token); } } catch (Exception e) { Toast.makeText(this,
	 * R.string.app_failure_init, Toast.LENGTH_LONG).show(); return; } }
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	// private BroadcastReceiver mTokenInvalidReceiver = new BroadcastReceiver()
	// {
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// DTHelper.getAccessProvider().invalidateToken(DiscoverTrentoActivity.this,
	// null);
	// initDataManagement(null);
	// }
	// };
	//
	// @Override
	// protected void onResume() {
	// IntentFilter filter = new
	// IntentFilter(SyncStorageService.ACTION_AUTHENTICATION_PROBLEM);
	// registerReceiver(mTokenInvalidReceiver, filter);
	// super.onResume();
	// }
	//
	// @Override
	// protected void onPause() {
	// unregisterReceiver(mTokenInvalidReceiver);
	// super.onPause();
	// }

	@Override
	public String getAppToken() {
		return Constants.APP_TOKEN;
	}

	@Override
	public String getAuthToken() {
		return DTHelper.getAuthToken();
	}
}
