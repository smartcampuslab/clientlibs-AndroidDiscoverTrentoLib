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

import java.util.Map;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.github.espiandev.showcaseview.BaseTutorialActivity;

import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.feedback.activity.FeedbackFragmentActivity;
import eu.trentorise.smartcampus.dt.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.dt.custom.TutorialActivity;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper.Tutorial;
import eu.trentorise.smartcampus.dt.fragments.events.AllEventsFragment;
import eu.trentorise.smartcampus.dt.fragments.events.EventDetailsFragment;
import eu.trentorise.smartcampus.dt.fragments.home.HomeFragment;
import eu.trentorise.smartcampus.dt.fragments.pois.AllPoisFragment;
import eu.trentorise.smartcampus.dt.fragments.pois.PoiDetailsFragment;
import eu.trentorise.smartcampus.dt.fragments.stories.AllStoriesFragment;
import eu.trentorise.smartcampus.dt.fragments.stories.StoryDetailsFragment;
import eu.trentorise.smartcampus.dt.notifications.NotificationsFragmentActivityDT;
import eu.trentorise.smartcampus.dt.notifications.NotificationsFragmentListDT;
import eu.trentorise.smartcampus.dt.notifications.NotificationsSherlockFragmentDT;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;
import eu.trentorise.smartcampus.territoryservice.model.EventObject;
import eu.trentorise.smartcampus.territoryservice.model.POIObject;
import eu.trentorise.smartcampus.territoryservice.model.StoryObject;

public class DiscoverTrentoActivity extends FeedbackFragmentActivity {

	private final static int TUTORIAL_REQUEST_CODE = 1;
	private Tutorial lastShowed;
	private boolean isLoading;

	public static DrawerLayout mDrawerLayout;
	public static ListView mDrawerList;
	public static ActionBarDrawerToggle mDrawerToggle;
	public static String drawerState = "on";
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private String[] mFragmentTitles;

	protected final int mainlayout = android.R.id.content;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("tag", getSupportActionBar().getSelectedNavigationIndex());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			startHomeFragment();
			// firstConfig();

		}
		setUpContent(savedInstanceState != null ? savedInstanceState.getInt("tag") : null);

		initDataManagement(savedInstanceState);

		// DEBUG PURPOSE
		// DTHelper.getTutorialPreferences(this).edit().clear().commit();

		if (DTHelper.isFirstLaunch(this)) {
			openNavDrawerIfNeeded();
			showTourDialog();
			DTHelper.disableFirstLaunch(this);
		}
		Intent intent = getIntent();
		BaseDTObject result = (BaseDTObject) intent
				.getSerializableExtra(NotificationsFragmentListDT.NOTIFICATIONS_PARAM);
		if (result != null) {
			FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
			SherlockFragment fragment = null;
			Bundle args = new Bundle();

			if (result instanceof EventObject) {
				fragment = new EventDetailsFragment();
				args.putString(EventDetailsFragment.ARG_EVENT_ID, (result.getId()));
			} else if (result instanceof POIObject) {
				fragment = new PoiDetailsFragment();
				args.putString(PoiDetailsFragment.ARG_POI_ID, result.getId());
			} else if (result instanceof StoryObject) {
				fragment = new StoryDetailsFragment();
				args.putString(StoryDetailsFragment.ARG_STORY_ID, result.getId());
			}
			fragment.setArguments(args);
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(R.id.fragment_container, fragment, "details");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
		}
	}

	private void startHomeFragment() {
		// drawerState = "on";
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		HomeFragment fragment = new HomeFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.replace(R.id.fragment_container, fragment);
		// ft.addToBackStack(fragment.getTag());
		ft.commit();

	}

	@Override
	protected void onResume() {
		if (DTHelper.getLocationHelper() != null)
			DTHelper.getLocationHelper().start();
		super.onResume();
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
		if (DTHelper.wantTour(this))
			showTutorial();
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.emptymenu, menu);
		return true;
	}

	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// if (item.getItemId() == android.R.id.home) {
	// onBackPressed();
	// return true;
	// } else
	// return super.onOptionsItemSelected(item);
	//
	// }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == TUTORIAL_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				String resData = data.getExtras().getString(BaseTutorialActivity.RESULT_DATA);
				if (resData.equals(BaseTutorialActivity.OK)) {
					DTHelper.setTutorialAsShowed(this, lastShowed);
				}
				if (DTHelper.wantTour(this))
					showTutorial();
			}
		} else if (requestCode == SCAccessProvider.SC_AUTH_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				String token = data.getExtras().getString(AccountManager.KEY_AUTHTOKEN);
				if (token == null) {
					Toast.makeText(this, R.string.app_failure_security, Toast.LENGTH_LONG).show();
					finish();
				} else {
					initData(token);
				}
			} else if (resultCode == RESULT_CANCELED && requestCode == SCAccessProvider.SC_AUTH_ACTIVITY_REQUEST_CODE) {
				DTHelper.endAppFailure(this, R.string.app_failure_security);
			}
		}
	}

	private void initDataManagement(Bundle savedInstanceState) {
		try {
			DTHelper.init(getApplicationContext());
			String token = DTHelper.getAuthToken();
			if (token != null) {
				initData(token);
			}
		} catch (Exception e) {
			Toast.makeText(this, R.string.app_failure_init, Toast.LENGTH_LONG).show();
			return;
		}
	}

	private boolean initData(String token) {
		try {
			new SCAsyncTask<Void, Void, BaseDTObject>(this, new LoadDataProcessor(this)).execute();
		} catch (Exception e1) {
			Toast.makeText(this, R.string.app_failure_init, Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}

	private void setUpContent(Integer pos) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setSupportProgressBarIndeterminateVisibility(false);

		setContentView(R.layout.main);

		mFragmentTitles = getResources().getStringArray(R.array.fragment_array);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerList.setAdapter(new MenuDrawerAdapter(this, getResources().getStringArray(R.array.fragment_array)));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		mTitle = mDrawerTitle = getTitle();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		//

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {

			public void onDrawerClosed(View view) {
				// getSupportActionBar().setTitle(mTitle);
				supportInvalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				// getSupportActionBar().setTitle(mDrawerTitle);
				supportInvalidateOptionsMenu();
			}

			public void onDrawerSlide(View drawerView, float slideOffset) {
				// getSupportActionBar().setTitle(mDrawerTitle);
				mDrawerLayout.bringChildToFront(drawerView);
				supportInvalidateOptionsMenu();
				super.onDrawerSlide(drawerView, slideOffset);
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);
		// enable ActionBar app icon to behave as action to toggle nav drawer
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

	}

	/* The click listner for ListView in the navigation drawer */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);
		}
	}

	private void selectItem(int position) {
		String fragmentString = mFragmentTitles[position];
		setTitle(fragmentString);
		// // update the main content by replacing fragments
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		if (fragmentString.equals(mFragmentTitles[0])) {
			HomeFragment fragment = new HomeFragment();
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(R.id.fragment_container, fragment, "map");
			// fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
			mDrawerLayout.closeDrawer(mDrawerList);
		} else if (fragmentString.equals(mFragmentTitles[1])) {
			AllPoisFragment fragment = new AllPoisFragment();
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(R.id.fragment_container, fragment, "pois");
			// fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
			mDrawerLayout.closeDrawer(mDrawerList);
		} else if (fragmentString.equals(mFragmentTitles[2])) {
			AllEventsFragment fragment = new AllEventsFragment();
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(R.id.fragment_container, fragment, "events");
			// fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
			mDrawerLayout.closeDrawer(mDrawerList);
		} else if (fragmentString.equals(mFragmentTitles[3])) {
			AllStoriesFragment fragment = new AllStoriesFragment();
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(R.id.fragment_container, fragment, "stories");
			// fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
			mDrawerLayout.closeDrawer(mDrawerList);
		} else if (fragmentString.equals(mFragmentTitles[4])) {
			// Intent i = (new Intent(DiscoverTrentoActivity.this,
			// NotificationsFragmentActivityDT.class));
			// startActivity(i);
			// mDrawerLayout.closeDrawer(mDrawerList);
			NotificationsFragmentListDT fragment = new NotificationsFragmentListDT();
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(R.id.fragment_container, fragment);
			// fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
			mDrawerLayout.closeDrawer(mDrawerList);
		} else if (fragmentString.equals(mFragmentTitles[5])) {
			prepareTutorial();
			showTutorial();
		}

	}

	private void prepareTutorial() {
		SharedPreferences pref = DTHelper.getTutorialPreferences(this);
		Editor editor = pref.edit();
		// reset tutorial
		for (Tutorial tut : Tutorial.values()) {
			editor.putBoolean(tut.toString(), false);
		}
		editor.commit();
		DTHelper.setWantTour(DiscoverTrentoActivity.this, true);		
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		// mDrawerToggle.setDrawerIndicatorEnabled(true);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}

	private class LoadDataProcessor extends AbstractAsyncTaskProcessor<Void, BaseDTObject> {

		private int syncRequired = 0;
		private SherlockFragmentActivity currentRootActivity = null;

		public LoadDataProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public BaseDTObject performAction(Void... params) throws SecurityException, Exception {
			// String entityId =
			// getIntent().getStringExtra(getString(R.string.view_intent_arg_object_id));
			// String type =
			// getIntent().getStringExtra(getString(R.string.view_intent_arg_entity_type));

			Exception res = null;

			try {
				syncRequired = DTHelper.syncRequired();
			} catch (Exception e) {
				res = e;
			}

			// if (entityId != null && type != null) {
			// if ("event".equals(type))
			// return DTHelper.findEventByEntityId(entityId).getObjectForBean();
			// else if ("location".equals(type))
			// return DTHelper.findPOIByEntityId(entityId).getObjectForBean();
			// else if ("narrative".equals(type))
			// return DTHelper.findStoryByEntityId(entityId).getObjectForBean();
			// } else
			if (res != null) {
				throw res;
			}
			return null;
		}

		@Override
		public void handleResult(BaseDTObject result) {
			if (syncRequired != DTHelper.SYNC_NOT_REQUIRED) {
				if (syncRequired == DTHelper.SYNC_REQUIRED_FIRST_TIME) {
					Toast.makeText(DiscoverTrentoActivity.this, R.string.initial_data_load, Toast.LENGTH_LONG).show();
				}
				setSupportProgressBarIndeterminateVisibility(true);
				isLoading = true;
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							currentRootActivity = DTHelper.start(DiscoverTrentoActivity.this);
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if (currentRootActivity != null) {
								currentRootActivity.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										currentRootActivity.setSupportProgressBarIndeterminateVisibility(false);
										if (DiscoverTrentoActivity.this != null) {
											DiscoverTrentoActivity.this
													.setSupportProgressBarIndeterminateVisibility(false);
										}
										isLoading = false;
									}
								});
							}
						}
					}
				}).start();
			} else {
				setSupportProgressBarIndeterminateVisibility(false);
				DTHelper.activateAutoSync();
			}

			// String entityId =
			// getIntent().getStringExtra(getString(R.string.view_intent_arg_object_id));
			// if (entityId != null) {
			// if (result == null) {
			// Toast.makeText(DiscoverTrentoActivity.this,
			// R.string.app_failure_obj_not_found, Toast.LENGTH_LONG).show();
			// return;
			// }
			//
			// SherlockFragment fragment = null;
			// String tag = null;
			// Bundle args = new Bundle();
			// if (result instanceof POIObject) {
			// fragment = new PoiDetailsFragment();
			// args.putString(PoiDetailsFragment.ARG_POI_ID, result.getId());
			// tag = "pois";
			// } else if (result instanceof EventObject) {
			// fragment = new EventDetailsFragment();
			// args.putString(EventDetailsFragment.ARG_EVENT_ID,
			// (result.getId()));
			// tag = "events";
			// } else if (result instanceof StoryObject) {
			// fragment = new StoryDetailsFragment();
			// args.putString(StoryDetailsFragment.ARG_STORY_ID,
			// result.getId());
			// tag = "stories";
			// // } else if (result instanceof StoryObject) {
			// // fragment = new EventDetailsFragment();
			// // args.putSerializable(StoryDetailsFragment.ARG_STORY_OBJECT,
			// // result);
			// // tag = "stories";
			// }
			// if (fragment != null) {
			// FragmentTransaction fragmentTransaction =
			// getSupportFragmentManager().beginTransaction();
			// fragment.setArguments(args);
			//
			// fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			// fragmentTransaction.replace(android.R.id.content, fragment, tag);
			// fragmentTransaction.addToBackStack(fragment.getTag());
			// fragmentTransaction.commit();
			// }
			// }
		}

	}

	@Override
	public void onNewIntent(Intent intent) {
		try {
			DTHelper.getAuthToken();
		} catch (Exception e) {
			Toast.makeText(this, R.string.app_failure_init, Toast.LENGTH_LONG).show();
			return;
		}

	}

	private void showTourDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this).setMessage(getString(R.string.dt_first_launch))
				.setPositiveButton(getString(R.string.begin_tut), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						DTHelper.setWantTour(DiscoverTrentoActivity.this, true);
						showTutorial();
					}
				}).setNeutralButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						DTHelper.setWantTour(DiscoverTrentoActivity.this, false);
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	private void showTutorial() {
		// open menu if it is needed
		openNavDrawerIfNeeded();

		DTHelper.Tutorial t = getFirstValidTutorial();
		int id;
		String title = "Tip!";
		String msg = "";
		View view = null;
		boolean isLast = false;
		if (t != null) {
			switch (t) {
			case HOME:
				view = getViewFromNavDrawer(0);
				// id = R.id.menu_item_notifications;
				title = getString(R.string.home_title);
				msg = getString(R.string.dt_home_tut);
				break;
			case NOTIF:
				view = getViewFromNavDrawer(4);
				title = getString(R.string.notifications_unread);
				msg = getString(R.string.dt_notif_tut);
				break;
			case EVENTS:
				view = getViewFromNavDrawer(2);

				title = getString(R.string.menu_item__events_layers_text);
				msg = getString(R.string.dt_events_tut);
				break;
			case PLACES:
				view = getViewFromNavDrawer(1);

				title = getString(R.string.menu_item__places_layers_text);
				msg = getString(R.string.dt_places_tut);
				break;
			case STORIES:
				view = getViewFromNavDrawer(3);
				title = getString(R.string.tab_stories);
				msg = getString(R.string.dt_stories_tut);
				break;
			case TUTORIAL:
				view = getViewFromNavDrawer(5);
				title = getString(R.string.tab_stories);
				msg = getString(R.string.dt_stories_tut);
				isLast = true;
				break;

			default:
				id = -1;
			}
		}
		if (t != null) {
			lastShowed = t;
			displayShowcaseView(view, title, msg, isLast);
		} else
			DTHelper.setWantTour(this, false);
		closeNavDrawerIfItIsTheEnd(t);
	}

	private void closeNavDrawerIfItIsTheEnd(Tutorial t) {
		if (t==null){
			DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
			if (mDrawerLayout.isDrawerOpen(Gravity.LEFT))
				mDrawerLayout.closeDrawer(Gravity.LEFT);
		}
	}

	private View getViewFromNavDrawer(int i) {
		ListView menu = (ListView) findViewById(R.id.left_drawer);
		if (menu != null) {
			return menu.getChildAt(i).findViewById(R.id.logo);
		}
		return null;
	}

	private void openNavDrawerIfNeeded() {
		DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (!mDrawerLayout.isDrawerOpen(Gravity.LEFT))
			mDrawerLayout.openDrawer(Gravity.LEFT);
	}

	private Tutorial getFirstValidTutorial() {
		Tutorial t = DTHelper.getLastTutorialNotShowed(this);
		/* if smartcampus (no notif) salta notifiche (setta a true notif */
		ApplicationInfo ai;
		try {
			ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
			Bundle aBundle = ai.metaData;
			if (aBundle.getBoolean("hidden-notification") && t.equals(t.NOTIF)) {
				DTHelper.setTutorialAsShowed(this, t);
				t = DTHelper.getLastTutorialNotShowed(this);

			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return t;
	}

	private void displayShowcaseView(View view, String title, String msg, boolean isLast) {
		int[] position = new int[2];
		int radius = 0;
		if (view != null) {
			view.getLocationOnScreen(position);
			radius = view.getWidth();
			BaseTutorialActivity.newIstance(this, position, radius, Color.WHITE, null, title, msg, isLast,
					TUTORIAL_REQUEST_CODE, TutorialActivity.class);
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

		switch (item.getItemId()) {
		case android.R.id.home:
			if (drawerState.equals("on")) {
				if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
					mDrawerLayout.closeDrawer(mDrawerList);
				} else {
					mDrawerLayout.openDrawer(mDrawerList);
				}
			} else {
				// drawerState = "on";
				onBackPressed();
			}
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
