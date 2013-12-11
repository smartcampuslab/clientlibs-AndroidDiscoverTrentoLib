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
package eu.trentorise.smartcampus.dt.fragments.stories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.maps.GeoPoint;

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.common.follow.model.Topic;
import eu.trentorise.smartcampus.android.common.navigation.NavigationHelper;
import eu.trentorise.smartcampus.android.feedback.utils.FeedbackFragmentInflater;
import eu.trentorise.smartcampus.dt.DiscoverTrentoActivity;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.dt.custom.RatingHelper;
import eu.trentorise.smartcampus.dt.custom.RatingHelper.RatingHandler;
import eu.trentorise.smartcampus.dt.custom.Utils;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
import eu.trentorise.smartcampus.dt.custom.map.MapManager;
import eu.trentorise.smartcampus.dt.fragments.pois.PoiDetailsFragment;
import eu.trentorise.smartcampus.dt.fragments.stories.AddStepToStoryFragment.StepHandler;
import eu.trentorise.smartcampus.dt.model.LocalStepObject;
import eu.trentorise.smartcampus.dt.notifications.NotificationsSherlockFragmentDT;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;
import eu.trentorise.smartcampus.territoryservice.model.CommunityData;
import eu.trentorise.smartcampus.territoryservice.model.POIObject;
import eu.trentorise.smartcampus.territoryservice.model.StepObject;
import eu.trentorise.smartcampus.territoryservice.model.StoryObject;

/*
 * Shows the detail of the story and steps, manages the mapview and the refresh of it
 */
public class StoryDetailsFragment extends NotificationsSherlockFragmentDT implements OnCameraChangeListener,
		OnMarkerClickListener {

	private boolean mFollowByIntent;
	private boolean mStart = true;
	private boolean mCanceledFollow = false;

	public static final String ARG_STORY_ID = "story_id";
	private StoryObject mStory = null;
	private String mStoryId;
	private int actualStepPosition = -1;
	// private MapView mapViewStory = null;
	// private DTStoryItemizedOverlay mItemizedoverlay = null;
	private AddStep stepHandler = new AddStep();

	private CompoundButton followButtonView;
	// private Fragment mFragment = this;

	private ScrollView stepScollView;
	private GoogleMap mMap;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setHasOptionsMenu(true);
		
		if (getArguments() != null) {
			mStoryId = getArguments().getString(ARG_STORY_ID);
			getStory();
		}

		setFollowByIntent();
	}

	private StoryObject getStory() {
		if (mStoryId == null || mStory == null) {
			mStoryId = getArguments().getString(ARG_STORY_ID);
			mStory = DTHelper.findStoryById(mStoryId);
			if (mStory != null) {
				try {
					List<POIObject> poiList = DTHelper.getPOIBySteps(mStory.getSteps());
					for (int i = 0; i < poiList.size(); i++) {
						Utils.getLocalStepFromStep(mStory.getSteps().get(i)).assignPoi(poiList.get(i));
					}
				} catch (Exception e) {
					Log.e(getClass().getName(), "Error reading story places: " + e.getMessage());
				}
			}

		}

		return mStory;
	}

	private void resetStory() {
		mStoryId = null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.story_details, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (getSupportMap() != null) {
			getSupportMap().setMyLocationEnabled(true);
			getSupportMap().setOnCameraChangeListener(this);
			getSupportMap().setOnMarkerClickListener(this);
			initCamera();
			// if (objects != null) {
			// render(objects);
			// }
		}
	}

	/**
	 * 
	 */
	protected void initCamera() {
		double[] coords = null;
		if (getSupportMap() != null) {
			if (getStory() != null
					&& getStory().getSteps() != null
					&& getStory().getSteps().size() > 0
					&& Utils.getLocalStepFromStep(getStory().getSteps().get(0)).assignedPoi() != null
					&& (coords = Utils.getLocalStepFromStep(getStory().getSteps().get(0)).assignedPoi().getLocation()) != null) {
				getSupportMap().moveCamera(
						CameraUpdateFactory.newLatLngZoom(new LatLng(coords[0], coords[1]), MapManager.ZOOM_DEFAULT));
			} else {
				getSupportMap().moveCamera(
						CameraUpdateFactory.newLatLngZoom(MapManager.DEFAULT_POINT, MapManager.ZOOM_DEFAULT));
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration arg0) {
		super.onConfigurationChanged(arg0);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (getSupportMap() != null) {
			getSupportMap().setMyLocationEnabled(false);
			getSupportMap().setOnCameraChangeListener(null);
			getSupportMap().setOnMarkerClickListener(null);
		}
	}

	private void setFollowByIntent() {
		try {
			ApplicationInfo ai = getSherlockActivity().getPackageManager().getApplicationInfo(
					getSherlockActivity().getPackageName(), PackageManager.GET_META_DATA);
			Bundle aBundle = ai.metaData;
			mFollowByIntent = aBundle.getBoolean("follow-by-intent");
		} catch (NameNotFoundException e) {
			mFollowByIntent = false;
			Log.e(StoryDetailsFragment.class.getName(), "you should set the follow-by-intent metadata in app manifest");
		}

	}

	@Override
	public void onStart() {
		super.onStart();

		DiscoverTrentoActivity.mDrawerToggle.setDrawerIndicatorEnabled(false);
    	DiscoverTrentoActivity.drawerState = "off";
        getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(true);
        getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(true);
        
        
		FeedbackFragmentInflater.inflateHandleButton(getSherlockActivity(), getView());

		this.stepScollView = (ScrollView) getView().findViewById(R.id.step_details);

		if (getStory() != null) {
			// title
			TextView titleText = (TextView) this.getView().findViewById(R.id.story_details_title);
			titleText.setText(getStory().getTitle());

			/*
			 * BUTTONS
			 */
			// follow/unfollow
			if (mStart) {
				ToggleButton followTbtn = (ToggleButton) this.getView().findViewById(R.id.storydetails_follow_tbtn);
				// TODO disabled for the moment
//				followTbtn.setVisibility(View.GONE);
//				if (getStory().getCommunityData().getFollowing() != null
//						&& getStory().getCommunityData().getFollowing().containsKey(DTHelper.getUserId())) {
//					followTbtn.setBackgroundResource(R.drawable.ic_btn_monitor_on);
//					followTbtn.setChecked(true);
//				} else {
//					followTbtn.setBackgroundResource(R.drawable.ic_btn_monitor_off);
//					followTbtn.setChecked(false);
//				}
//
//				followTbtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//					@Override
//					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//						if (!mCanceledFollow) {
//							if (isChecked) {
//								// FOLLOW
//								{
//									SCAsyncTask<Object, Void, BaseDTObject> followTask = new SCAsyncTask<Object, Void, BaseDTObject>(
//											getSherlockActivity(), new FollowAsyncTaskProcessor(getSherlockActivity(),
//													buttonView));
//									followTask.execute(mStory);
//								}
//							} else {
//								// UNFOLLOW
//										SCAsyncTask<BaseDTObject, Void, BaseDTObject> unfollowTask = new SCAsyncTask<BaseDTObject, Void, BaseDTObject>(
//												getSherlockActivity(), new UnfollowAsyncTaskProcessor(
//														getSherlockActivity(), buttonView));
//										unfollowTask.execute(mStory);
//
//									}
//						} else {
//							mCanceledFollow = false;
//						}
//						resetStory();
//					}
//				});
			}

			// attend
			ToggleButton attendTbtn = (ToggleButton) this.getView().findViewById(R.id.storydetails_attend_tbtn);
			if (getStory().getAttending() == null || getStory().getAttending().isEmpty()) {
				attendTbtn.setBackgroundResource(R.drawable.ic_btn_monitor_off);
				attendTbtn.setChecked(false);
			} else {
				attendTbtn.setBackgroundResource(R.drawable.ic_btn_monitor_on);
				attendTbtn.setChecked(true);
			}

			attendTbtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					// if (new
					// AMSCAccessProvider().isUserAnonymous(getSherlockActivity()))
					// {
					// // show dialog box
					// UserRegistration.upgradeuser(getSherlockActivity());
					// } else
					{
						new SCAsyncTask<Boolean, Void, StoryObject>(getActivity(), new AttendProcessor(
								getSherlockActivity(), buttonView)).execute(getStory().getAttending() == null
								|| getStory().getAttending().isEmpty());
						resetStory();
					}
				}
			});
			/*
			 * END BUTTONS
			 */

			// rating
			RatingBar rating = (RatingBar) getView().findViewById(R.id.story_details_rating);
			rating.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_UP) {
						// if (new
						// AMSCAccessProvider().isUserAnonymous(getSherlockActivity()))
						// {
						// // show dialog box
						// UserRegistration.upgradeuser(getSherlockActivity());
						// return false;
						// } else
						{
							ratingDialog();
						}
					}
					return true;
				}
			});

			updateRating();
			updateAttending();

			// description, optional
			titleText = (TextView) this.getView().findViewById(R.id.story_details_descr);
			if (getStory().getDescription() != null && getStory().getDescription().length() > 0) {
				titleText.setText(getStory().getDescription());
			}

			// detail of the story (contains all the story elements)
			final ScrollView detailStory = (ScrollView) this.getView().findViewById(R.id.story_details);
			detailStory.setVisibility(View.VISIBLE);

			// detail of the step (contains all the step elements)
			final ScrollView detailStep = (ScrollView) this.getView().findViewById(R.id.step_details);

			// disable the step part at the start
			detailStep.setVisibility(View.GONE);
			final LinearLayout buttonStep = (LinearLayout) this.getView().findViewById(R.id.navigation_buttons);
			buttonStep.setVisibility(View.GONE);

			// start button
			final LinearLayout buttonSart = (LinearLayout) this.getView().findViewById(R.id.start_buttons);
			buttonSart.setVisibility(View.VISIBLE);
			final Button startStory = (Button) this.getView().findViewById(R.id.btn_story_start);
			startStory.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// visualize first step enabling the its elements and
					// disabling the story part
					// mItemizedoverlay.fithMaptOnTheStory();
					changeStep(actualStepPosition + 1);
					detailStep.setVisibility(View.VISIBLE);
					buttonStep.setVisibility(View.VISIBLE);
					buttonSart.setVisibility(View.GONE);
					detailStory.setVisibility(View.GONE);
					// mItemizedoverlay.changeElementsonMap(actualStepPosition,
					// mStory);
				}
			});

			// prevbutton
			final Button prevButton = (Button) this.getView().findViewById(R.id.btn_story_prev);
			prevButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// mItemizedoverlay.fithMaptOnTheStory();
					changeStep(actualStepPosition - 1);
					// mItemizedoverlay.changeElementsonMap(actualStepPosition,
					// mStory);
					stepScollView.scrollTo(0, stepScollView.getTop());
				}
			});

			// next button
			final Button nextButton = (Button) this.getView().findViewById(R.id.btn_story_next);
			nextButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// mItemizedoverlay.fithMaptOnTheStory();
					changeStep(actualStepPosition + 1);
					// mItemizedoverlay.changeElementsonMap(actualStepPosition,
					// mStory);
					stepScollView.scrollTo(0, stepScollView.getTop());
				}
			});
			// reinit the story every time this fragment is loaded
			changeStep(-1);
			// hide the keyboard
			InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(
					Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(nextButton.getWindowToken(), 0);
			// changeTheMapConfiguration();
			// mItemizedoverlay.fithMaptOnTheStory();

			// setPOIStoryToLoad(getStory());
		}
	}

	private void updateAttending() {
		TextView tv;
		// attendees
		if (getView() != null) {
			tv = (TextView) this.getView().findViewById(R.id.attendees_num);
			if (getStory().getAttendees() != null) {
				tv.setText(getStory().getAttendees() + " ");
			} else {
				tv.setText(" 0 ");
			}
		}
	}

	/*
	 * Method used to change all the element in the fragment (except the Map) if
	 * the actualStepPosition is -1, shows the story's details if it is
	 * different, shows the others details and buttons
	 */

	private void changeStep(int i) {
		if (getView() != null) {
			actualStepPosition = i;
			// detail of the story
			ScrollView detailStory = (ScrollView) this.getView().findViewById(R.id.story_details);
			// disable the step part
			ScrollView detailStep = (ScrollView) this.getView().findViewById(R.id.step_details);
			LinearLayout buttonStep = (LinearLayout) this.getView().findViewById(R.id.navigation_buttons);
			// start button
			LinearLayout startStory = (LinearLayout) this.getView().findViewById(R.id.start_buttons);

			if (getStory().getSteps() == null || getStory().getSteps().size() == 0) {
				startStory.setVisibility(View.GONE);
			} else

			// show the details of the story
			if (actualStepPosition == -1) {
				detailStory.setVisibility(View.VISIBLE);
				startStory.setVisibility(View.VISIBLE);
				detailStep.setVisibility(View.GONE);
				buttonStep.setVisibility(View.GONE);
			}
			// else load the details of the step
			else {
				// change layout
				if (getStory().getSteps().get(actualStepPosition) != null) {

					detailStory.setVisibility(View.GONE);
					startStory.setVisibility(View.GONE);
					detailStep.setVisibility(View.VISIBLE);
					buttonStep.setVisibility(View.VISIBLE);
					// number of the step
					TextView numberOfStepText = (TextView) this.getView().findViewById(R.id.number_of_step);
					numberOfStepText.setText(String.valueOf(actualStepPosition + 1));
					// name of the step (if the POI hasn't been erased)
					TextView nameOfStepText = (TextView) this.getView().findViewById(R.id.step_details_name);
					if (Utils.getLocalStepFromStep(getStory().getSteps().get(actualStepPosition)).assignedPoi() != null)
						nameOfStepText.setText(Utils
								.getLocalStepFromStep(getStory().getSteps().get(actualStepPosition)).assignedPoi()
								.getTitle());
					else
						nameOfStepText.setText(getString(R.string.poi_erased));
					// notes of the step
					TextView noteOfStepText = (TextView) this.getView().findViewById(R.id.step_details_note);
					// if
					// (story.getSteps().get(actualStepPosition).assignedPoi()
					// !=
					// null)
					// noteOfStepText.setText(story.getSteps()
					// .get(actualStepPosition).getNote());
					// else
					// noteOfStepText.setText(" ");
					noteOfStepText.setText(getStory().getSteps().get(actualStepPosition).getNote());
					Button nextStep = (Button) this.getView().findViewById(R.id.btn_story_next);

					// If it is at the end of the story, hides the "next" button
					if (actualStepPosition == getStory().getSteps().size() - 1)
						nextStep.setVisibility(View.GONE);
					else
						nextStep.setVisibility(View.VISIBLE);
				}
			}
			/* forse meglio se tirato fuori da change step */

			Collection<LocalStepObject> stepsParam = new ArrayList<LocalStepObject>();
			for (StepObject object : getStory().getSteps()) {
				stepsParam.add(Utils.getLocalStepFromStep(object));
			}

			renderSteps(stepsParam, actualStepPosition);
			if (actualStepPosition < 0) {
				initCamera();
			}
			getSherlockActivity().supportInvalidateOptionsMenu();
		}
	}

	@Override
	public void onDestroyView() {
		try {
			FragmentTransaction transaction = getFragmentManager().beginTransaction();
			transaction.remove(getFragmentManager().findFragmentById(R.id.my_map_fragment1)).commit();
			mMap = null;
		} catch (Exception e) {
		}

		super.onDestroyView();
	}

	/*
	 * There are many different cases to build the options menu: 1-story
	 * visualization 2-POI 3-POI created by user 4-POI erased
	 */
	@Override
	public void onPrepareOptionsMenu(Menu menu) {

		menu.clear();
		getSherlockActivity().getSupportMenuInflater().inflate(R.menu.gripmenu, menu);

		SubMenu submenu = menu.getItem(0).getSubMenu();
		submenu.clear();

		if (actualStepPosition == -1 || getStory().getSteps() == null || getStory().getSteps().size() == 0) {
			// story visualization
			// String userId = DTHelper.getUserId();

			// submenu.add(Menu.CATEGORY_SYSTEM, R.id.submenu_rate, Menu.NONE,
			// R.string.rate);

			// if (getStory().getAttending() == null ||
			// getStory().getAttending().isEmpty()) {
			// submenu.add(Menu.CATEGORY_SYSTEM, R.id.add_my_stories, Menu.NONE,
			// R.string.add_my_stories);
			// } else {
			// submenu.add(Menu.CATEGORY_SYSTEM, R.id.add_my_stories, Menu.NONE,
			// R.string.delete_my_stories);
			// }

			// if
			// (getStory().getCommunityData().getFollowing().containsKey(userId))
			// {
			// submenu.add(Menu.CATEGORY_SYSTEM, R.id.submenu_unfollow,
			// Menu.NONE, R.string.unfollow);
			// } else {
			// submenu.add(Menu.CATEGORY_SYSTEM, R.id.submenu_follow, Menu.NONE,
			// R.string.follow);
			// }

			submenu.add(Menu.CATEGORY_SYSTEM, R.id.submenu_tag, Menu.NONE, R.string.submenu_tag);

			// CAN EDIT OR DELETE ONLY OWN STORY
			if (DTHelper.isOwnedObject(getStory())) {
				submenu.add(Menu.CATEGORY_SYSTEM, R.id.submenu_edit, Menu.NONE, R.string.edit);
				submenu.add(Menu.CATEGORY_SYSTEM, R.id.submenu_delete, Menu.NONE, R.string.delete);
			}
		} else {
			// POI visualization
			if (Utils.getLocalStepFromStep(getStory().getSteps().get(actualStepPosition)).assignedPoi() != null) {
				submenu.add(Menu.CATEGORY_SYSTEM, R.id.related_step_btn, Menu.NONE, R.string.related_poi);
				submenu.add(Menu.CATEGORY_SYSTEM, R.id.direction_step_btn, Menu.NONE, R.string.getdir);

			}
			// CAN EDIT AND DELETE STEPS ONLY IN OWN STORIES
			if (DTHelper.isOwnedObject(getStory())) {
				// TODO implement the step cancellation!!!!
				// submenu.add(Menu.CATEGORY_SYSTEM, R.id.delete_step_btn,
				// Menu.NONE, R.string.delete);
				submenu.add(Menu.CATEGORY_SYSTEM, R.id.edit_step_btn, Menu.NONE, R.string.edit);
			}

		}
		super.onPrepareOptionsMenu(menu);
	}

	private void ratingDialog() {
		float rating = (getStory() != null && getStory().getCommunityData() != null && getStory().getCommunityData()
				.getAverageRating() > 0) ? getStory().getCommunityData().getAverageRating() : 2.5f;
		RatingHelper.ratingDialog(getActivity(), rating, new RatingProcessor(getActivity()),
				R.string.rating_story_dialog_title);
	}

	private void updateRating() {
		if (getView() != null) {
			RatingBar rating = (RatingBar) getView().findViewById(R.id.story_details_rating);
			if (getStory().getCommunityData() != null) {
				CommunityData cd = getStory().getCommunityData();

				if (cd.getRating() != null && !cd.getRating().isEmpty()) {
					Iterator<Map.Entry<String, Integer>> entries = cd.getRating().entrySet().iterator();
					float rate = 0;
					while (entries.hasNext()) {
						Map.Entry<String, Integer> entry = entries.next();
						rate = entry.getValue();
					}
					rating.setRating(rate);
				}

				// user rating

				// total raters
				((TextView) getView().findViewById(R.id.event_rating_raters)).setText(getString(
						R.string.ratingtext_raters, cd.getRatingsCount()));

				// averange rating
				((TextView) getView().findViewById(R.id.event_rating_average)).setText(getString(
						R.string.ratingtext_average, cd.getAverageRating()));
			}
		}
	}

	private class RatingProcessor extends AbstractAsyncTaskProcessor<Integer, Integer> implements RatingHandler {

		public RatingProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public Integer performAction(Integer... params) throws SecurityException, Exception {
			return DTHelper.rate(getStory(), params[0]);
		}

		@Override
		public void handleResult(Integer result) {
			resetStory();
			getStory();
			updateRating();
			if (getSherlockActivity() != null)
				Toast.makeText(getSherlockActivity(), R.string.rating_success, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onRatingChanged(float rating) {
			new SCAsyncTask<Integer, Void, Integer>(getActivity(), this).execute((int) rating);
		}
	}

	/*
	 * Manage the options
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// if (item.getItemId() == R.id.submenu_rate) {
		// if (new AMSCAccessProvider().isUserAnonymous(getSherlockActivity()))
		// {
		// // show dialog box
		// UserRegistration.upgradeuser(getSherlockActivity());
		// return false;
		// } else {
		// ratingDialog();
		// return true;
		// }
		// } else if (item.getItemId() == R.id.add_my_stories) {
		// if (new
		// AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
		// // show dialog box
		// UserRegistration.upgradeuser(getSherlockActivity());
		// return false;
		// } else {
		// new SCAsyncTask<Boolean, Void, StoryObject>(getActivity(), new
		// AttendProcessor(getActivity()))
		// .execute(getStory().getAttending() == null ||
		// getStory().getAttending().isEmpty());
		// return true;
		// }
		// } else if (item.getItemId() == R.id.submenu_follow) {
		// FollowEntityObject obj = new
		// FollowEntityObject(getStory().getEntityId(),
		// getStory().getTitle(),
		// DTConstants.ENTITY_TYPE_STORY);
		// if (mFollowByIntent) {
		// FollowHelper.follow(this, obj, 3000);
		// } else {
		// SCAsyncTask<Object, Void, Topic> followTask = new
		// SCAsyncTask<Object, Void, Topic>(getSherlockActivity(),
		// new FollowAsyncTaskProcessor(getSherlockActivity(), null));
		// followTask.execute(getSherlockActivity().getApplicationContext(),
		// DTParamsHelper.getAppToken(),
		// DTHelper.getAuthToken(), obj);
		// }
		// return true;
		// } else if (item.getItemId() == R.id.submenu_unfollow) {
		// BaseDTObject obj;
		// try {
		// obj = DTHelper.findStoryByEntityId(getStory().getEntityId());
		// if (obj != null) {
		// SCAsyncTask<BaseDTObject, Void, BaseDTObject> unfollowTask = new
		// SCAsyncTask<BaseDTObject, Void, BaseDTObject>(
		// getSherlockActivity(), new
		// UnfollowAsyncTaskProcessor(getSherlockActivity(), null));
		// unfollowTask.execute(obj);
		//
		// }
		// } catch (Exception e) {
		// Log.e(EventDetailsFragment.class.getName(),
		// String.format("Error unfollowing event %s",
		// getStory().getEntityId()));
		// }
		// return true;
		if (item.getItemId() == R.id.submenu_edit || item.getItemId() == R.id.submenu_tag) {
			// if (new
			// AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
			// // show dialog box
			// UserRegistration.upgradeuser(getSherlockActivity());
			// return false;
			// } else
			{
				FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager()
						.beginTransaction();
				Fragment fragment = new CreateStoryFragment();
				Bundle args = new Bundle();
				args.putSerializable(CreateStoryFragment.ARG_STORY, getStory());
				fragment.setArguments(args);
				fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				fragmentTransaction.replace(R.id.fragment_container, fragment, "stories");
				fragmentTransaction.addToBackStack(fragment.getTag());
				fragmentTransaction.commit();
				return true;
			}
		} else if (item.getItemId() == R.id.submenu_delete) {
			// if (new
			// AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
			// // show dialog box
			// UserRegistration.upgradeuser(getSherlockActivity());
			// return false;
			// } else
			{
				new SCAsyncTask<StoryObject, Void, Boolean>(getActivity(), new StoryDeleteProcessor(getActivity()))
						.execute(getStory());
				return true;
			}
		} else if (item.getItemId() == R.id.related_step_btn) {
			FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager()
					.beginTransaction();
			PoiDetailsFragment fragment = new PoiDetailsFragment();
			Bundle args = new Bundle();
			args.putString(PoiDetailsFragment.ARG_POI_ID,
					Utils.getLocalStepFromStep(getStory().getSteps().get(actualStepPosition)).assignedPoi().getId());
			fragment.setArguments(args);
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(R.id.fragment_container, fragment, "stories");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
			return true;
		} else if (item.getItemId() == R.id.direction_step_btn) {
			Address to = Utils.getPOIasGoogleAddress(Utils.getLocalStepFromStep(
					getStory().getSteps().get(actualStepPosition)).assignedPoi());
			Address from = null;
			GeoPoint mylocation = MapManager.requestMyLocation(getActivity());
			if (mylocation != null) {
				from = new Address(Locale.getDefault());
				from.setLatitude(mylocation.getLatitudeE6() / 1E6);
				from.setLongitude(mylocation.getLongitudeE6() / 1E6);
			}
			NavigationHelper.bringMeThere(getActivity(), from, to);
			return true;
		} else if (item.getItemId() == R.id.edit_step_btn) {
			// if (new
			// AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
			// // show dialog box
			// UserRegistration.upgradeuser(getSherlockActivity());
			// return false;
			// } else
			{
				FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager()
						.beginTransaction();
				AddStepToStoryFragment fragment = new AddStepToStoryFragment();
				Bundle args = new Bundle();
				args.putParcelable(AddStepToStoryFragment.ARG_STEP_HANDLER, stepHandler);
				args.putSerializable(AddStepToStoryFragment.ARG_STORY_OBJECT, getStory());
				args.putInt(AddStepToStoryFragment.ARG_STEP_POSITION, actualStepPosition);
				fragment.setArguments(args);
				fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				fragmentTransaction.replace(R.id.fragment_container, fragment, "stories");
				fragmentTransaction.addToBackStack(fragment.getTag());
				fragmentTransaction.commit();
				return true;
			}
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 3000) {
			if (resultCode == Activity.RESULT_OK) {
				Topic topic = (Topic) data.getSerializableExtra("topic");
				new FollowAsyncTask().execute(topic.getId());
				// fix to avoid onActivityResult DiscoverTrentoActivity failure
				data.putExtra(AccountManager.KEY_AUTHTOKEN, DTHelper.getAuthToken());
				mStart = false;
			} else {
				getStory().getCommunityData().getFollowing().clear();
				mCanceledFollow = true;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/*
	 * load on the map the story passed by parameter using an asynch task.
	 */

	// @Override
	// public void setPOIStoryToLoad(final StoryObject story) {
	// // mItemizedoverlay.clearMarkers();
	//
	// new SCAsyncTask<Void, Void, Collection<? extends
	// BaseDTObject>>(getActivity(), new MapLoadProcessor(getActivity(),
	// this, getSupportMap()) {
	// @Override
	// protected Collection<? extends BaseDTObject> getObjects() {
	// try {
	// ArrayList<POIObject> poiList = new ArrayList<POIObject>();
	// poiList = DTHelper.getPOIBySteps(story.getSteps());
	// for (int i = 0; i < poiList.size(); i++) {
	// story.getSteps().get(i).assignPoi(poiList.get(i));
	// }
	// return poiList;
	// } catch (Exception e) {
	// e.printStackTrace();
	// return Collections.emptyList();
	// }
	// }
	// }).execute();
	// }

	// /*
	// * Method used by MyMapFragment to load the MapView. It requires the reset
	// * of the map
	// */
	// public void setMap(MapView mapViewStory) {
	//
	// this.mapViewStory = mapViewStory;
	// changeTheMapConfiguration();
	// }
	//
	// /*
	// * Reset of the mapView and its overlays
	// */
	// private void changeTheMapConfiguration() {
	// mapViewStory.setClickable(true);
	// mapViewStory.getController().setZoom(15);
	// List<Overlay> listOfOverlays = mapViewStory.getOverlays();
	//
	// mItemizedoverlay = new DTStoryItemizedOverlay(getSherlockActivity(),
	// mapViewStory, mStory);
	// mItemizedoverlay.setMapItemTapListener(this);
	// listOfOverlays.add(mItemizedoverlay);
	// setPOIStoryToLoad(mStory);
	// mItemizedoverlay.fithMaptOnTheStory();
	//
	// }

	/*
	 * class passsed to the AddStepToStoryFragment and implements two method
	 * used in it.
	 */
	private class AddStep implements StepHandler, Parcelable {
		private static final long serialVersionUID = 16774297617446649L;

		/*
		 * add the step to my story and refresh the overlay items(non-Javadoc)
		 */
		@Override
		public void addStep(LocalStepObject step) {
			getStory().getSteps().add(step);
			// TODO ?
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {

		}

		/*
		 * update the step, if the user confirms in the dialog box
		 */
		@Override
		public void updateStep(final LocalStepObject step, final Integer position) {
			// generate dialog box for confirming the update
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			// Add the buttons
			builder.setMessage(getActivity().getString(R.string.sure_change));
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// User clicked OK button
					getStory().getSteps().set(position, step);
					new SCAsyncTask<StoryObject, Void, Boolean>(getActivity(), new CreateStoryProcessor(getActivity()))
							.execute(getStory());

				}
			});
			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// User cancelled the dialog
					dialog.dismiss();
				}
			});

			// Create the AlertDialog
			AlertDialog dialog = builder.create();
			dialog.show();
		}

	}

	/*
	 * Delete a story
	 */
	private class StoryDeleteProcessor extends AbstractAsyncTaskProcessor<StoryObject, Boolean> {
		public StoryDeleteProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public Boolean performAction(StoryObject... params) throws SecurityException, Exception {
			return DTHelper.deleteStory(params[0]);
		}

		@Override
		public void handleResult(Boolean result) {
			if (getSherlockActivity() != null)
				if (result) {
					getSherlockActivity().getSupportFragmentManager().popBackStack();
				} else {
					Toast.makeText(getSherlockActivity(), R.string.app_failure_cannot_delete, Toast.LENGTH_LONG).show();
				}
		}

	}

	/*
	 * Attend to a story
	 */
	private class AttendProcessor extends AbstractAsyncTaskProcessor<Boolean, StoryObject> {

		private CompoundButton buttonView;
		private Boolean attend;

		public AttendProcessor(Activity activity, CompoundButton buttonView) {
			super(activity);
			this.buttonView = buttonView;
		}

		@Override
		public StoryObject performAction(Boolean... params) throws SecurityException, Exception {
			attend = params[0];
			if (attend) {
				return DTHelper.addToMyStories(getStory());
			}
			return DTHelper.removeFromMyStories(getStory());
		}

		@Override
		public void handleResult(StoryObject result) {
//			resetStory();
			mStory = result;
			updateAttending();
			// changeTheMapConfiguration();
			// getSherlockActivity().invalidateOptionsMenu();
			if (getSherlockActivity() != null)
				if (mStory.getAttending() == null || mStory.getAttending().isEmpty()) {
					Toast.makeText(getSherlockActivity(), R.string.not_attend_story_success, Toast.LENGTH_SHORT).show();
					buttonView.setBackgroundResource(R.drawable.ic_btn_monitor_off);
				} else {
					Toast.makeText(getSherlockActivity(), R.string.attend_story_success, Toast.LENGTH_SHORT).show();
					buttonView.setBackgroundResource(R.drawable.ic_btn_monitor_on);
				}
		}
	}

	/*
	 * Create a story
	 */
	private class CreateStoryProcessor extends AbstractAsyncTaskProcessor<StoryObject, Boolean> {

		public CreateStoryProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public Boolean performAction(StoryObject... params) throws SecurityException, Exception {
			return DTHelper.saveStory(params[0]);
		}

		@Override
		public void handleResult(Boolean result) {
			if (getSherlockActivity() != null)
				if (result) {
					Toast.makeText(getSherlockActivity(), R.string.story_create_success, Toast.LENGTH_SHORT).show();
					getSherlockActivity().getSupportFragmentManager().popBackStack();

				} else {
					Toast.makeText(getSherlockActivity(), R.string.update_success, Toast.LENGTH_SHORT).show();
					getSherlockActivity().getSupportFragmentManager().popBackStack();
					changeStep(-1);
				}
		}
	}

	class FollowAsyncTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			String topicId = params[0];
			try {
				DTHelper.follow(DTHelper.findStoryById(mStoryId));
			} catch (Exception e) {
				Log.e(FollowAsyncTask.class.getName(), String.format("Exception following event %s", mStoryId));
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// getSherlockActivity().invalidateOptionsMenu();
			if (followButtonView != null) {
				followButtonView.setBackgroundResource(R.drawable.ic_btn_monitor_on);
				followButtonView = null;
			}
			mStart = true;
		}

	}

	class GetStoryAsyncTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			if (mStoryId == null || mStory == null) {
				mStoryId = getArguments().getString(ARG_STORY_ID);
				mStory = DTHelper.findStoryById(mStoryId);
				if (mStory != null) {
					try {
						List<POIObject> poiList = DTHelper.getPOIBySteps(mStory.getSteps());
						for (int i = 0; i < poiList.size(); i++) {
							Utils.getLocalStepFromStep(mStory.getSteps().get(i)).assignPoi(poiList.get(i));
						}
					} catch (Exception e) {
						Log.e(getClass().getName(), "Error reading story places: " + e.getMessage());
					}

				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// getSherlockActivity().invalidateOptionsMenu();
			if (followButtonView != null) {
				followButtonView.setBackgroundResource(R.drawable.ic_btn_monitor_on);
				followButtonView = null;
			}
			mStart = true;
		}

	}

	private GoogleMap getSupportMap() {
		if (mMap == null) {
			if (getFragmentManager().findFragmentById(R.id.my_map_fragment1) != null
					&& getFragmentManager().findFragmentById(R.id.my_map_fragment1) instanceof SupportMapFragment)
				mMap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.my_map_fragment1)).getMap();
		}
		return mMap;
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		int pos = Integer.parseInt(marker.getTitle());
		actualStepPosition = pos - 1;
		changeStep(actualStepPosition);
		return true;
	}

	@Override
	public void onCameraChange(CameraPosition position) {
		// TODO Auto-generated method stub

	}

	private void renderSteps(Collection<LocalStepObject> objects, int selection) {
		if (getSupportMap() != null) {
			getSupportMap().clear();
			if (objects != null) {
				int i = 0;
				BaseDTObject from = null, to = null;
				for (LocalStepObject object : objects) {
					to = object.assignedPoi();
					if (to != null) {
						getSupportMap().addMarker(
								MapManager.createStoryStepMarker(getSherlockActivity(), to, i + 1, selection == i));
						if (from != null) {
							getSupportMap()
									.addPolyline(MapManager.createStoryStepLine(getSherlockActivity(), from, to));
						}
					}
					from = to;
					i++;
				}
			}
		}
	}

}
