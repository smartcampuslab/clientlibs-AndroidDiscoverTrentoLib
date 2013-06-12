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
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Address;
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
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import eu.trentorise.smartcampus.ac.UserRegistration;
import eu.trentorise.smartcampus.ac.authenticator.AMSCAccessProvider;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.common.follow.FollowEntityObject;
import eu.trentorise.smartcampus.android.common.follow.FollowHelper;
import eu.trentorise.smartcampus.android.common.follow.model.Topic;
import eu.trentorise.smartcampus.android.common.navigation.NavigationHelper;
import eu.trentorise.smartcampus.dt.DTParamsHelper;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.dt.custom.RatingHelper;
import eu.trentorise.smartcampus.dt.custom.RatingHelper.RatingHandler;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
import eu.trentorise.smartcampus.dt.custom.data.FollowAsyncTaskProcessor;
import eu.trentorise.smartcampus.dt.custom.data.UnfollowAsyncTaskProcessor;
import eu.trentorise.smartcampus.dt.custom.map.BasicObjectMapItemTapListener;
import eu.trentorise.smartcampus.dt.custom.map.DTStoryItemizedOverlay;
import eu.trentorise.smartcampus.dt.custom.map.MapManager;
import eu.trentorise.smartcampus.dt.custom.map.MapStoryHandler;
import eu.trentorise.smartcampus.dt.custom.map.MapStoryLoadProcessor;
import eu.trentorise.smartcampus.dt.fragments.events.EventDetailsFragment;
import eu.trentorise.smartcampus.dt.fragments.pois.PoiDetailsFragment;
import eu.trentorise.smartcampus.dt.fragments.stories.AddStepToStoryFragment.StepHandler;
import eu.trentorise.smartcampus.dt.model.BaseDTObject;
import eu.trentorise.smartcampus.dt.model.DTConstants;
import eu.trentorise.smartcampus.dt.model.POIObject;
import eu.trentorise.smartcampus.dt.model.StepObject;
import eu.trentorise.smartcampus.dt.model.StoryObject;
import eu.trentorise.smartcampus.dt.notifications.NotificationsSherlockFragmentDT;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.storage.BasicObject;

/*
 * Shows the detail of the story and steps, manages the mapview and the refresh of it
 */
public class StoryDetailsFragment extends NotificationsSherlockFragmentDT
		implements MapStoryHandler, BasicObjectMapItemTapListener {

	public static final String ARG_STORY = "story_object";
	private StoryObject story = null;
	private String storyId;
	private int actualStepPosition = -1;
	private MapView mapViewStory = null;
	private DTStoryItemizedOverlay mItemizedoverlay = null;
	private AddStep stepHandler = new AddStep();
	private boolean mFollowByIntent;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setHasOptionsMenu(true);
		setFollowByIntent();
	}

	private StoryObject getStory() {
		if (story == null) {
			story = (StoryObject) getArguments().getSerializable(ARG_STORY);
			storyId = story.getId();
		}
		story = DTHelper.findStoryById(storyId);
		return story;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.story_details, container, false);
	}

	private void setFollowByIntent() {
		try {
			ApplicationInfo ai = getSherlockActivity().getPackageManager()
					.getApplicationInfo(getSherlockActivity().getPackageName(),
							PackageManager.GET_META_DATA);
			Bundle aBundle = ai.metaData;
			mFollowByIntent = aBundle.getBoolean("follow-by-intent");
		} catch (NameNotFoundException e) {
			mFollowByIntent = false;
			Log.e(StoryDetailsFragment.class.getName(),
					"you should set the follow-by-intent metadata in app manifest");
		}

	}

	@Override
	public void onStart() {
		super.onStart();
		if (getStory() != null) {

			// title
			TextView titleText = (TextView) this.getView().findViewById(
					R.id.story_details_title);
			titleText.setText(getStory().getTitle());

			// rating
			RatingBar rating = (RatingBar) getView().findViewById(
					R.id.story_details_rating);
			rating.setOnTouchListener(new View.OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_UP) {
						if (new AMSCAccessProvider()
								.isUserAnonymous(getSherlockActivity())) {
							// show dialog box
							UserRegistration.upgradeuser(getSherlockActivity());
							return false;
						} else {
							ratingDialog();
						}
					}
					return true;
				}
			});
			if (story.getCommunityData() != null) {
				rating.setRating(story.getCommunityData().getAverageRating());
			}

			// description, optional
			titleText = (TextView) this.getView().findViewById(
					R.id.story_details_descr);
			if (story.getDescription() != null
					&& story.getDescription().length() > 0) {
				titleText.setText(story.getDescription());
			}

			// detail of the story (contains all the story elements)
			final ScrollView detailStory = (ScrollView) this.getView()
					.findViewById(R.id.story_details);
			detailStory.setVisibility(View.VISIBLE);

			// update the attending part
			updateAttending();

			// detail of the step (contains all the step elements)
			final ScrollView detailStep = (ScrollView) this.getView()
					.findViewById(R.id.step_details);

			// disable the step part at the start
			detailStep.setVisibility(View.GONE);
			final LinearLayout buttonStep = (LinearLayout) this.getView()
					.findViewById(R.id.navigation_buttons);
			buttonStep.setVisibility(View.GONE);

			// start button
			final LinearLayout buttonSart = (LinearLayout) this.getView()
					.findViewById(R.id.start_buttons);
			buttonSart.setVisibility(View.VISIBLE);
			final Button startStory = (Button) this.getView().findViewById(
					R.id.btn_story_start);
			startStory.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// visualize first step enabling the its elements and
					// disabling the story part
					mItemizedoverlay.fithMaptOnTheStory();
					changeStep(actualStepPosition + 1);
					detailStep.setVisibility(View.VISIBLE);
					buttonStep.setVisibility(View.VISIBLE);
					buttonSart.setVisibility(View.GONE);
					detailStory.setVisibility(View.GONE);
					mItemizedoverlay.changeElementsonMap(actualStepPosition,
							story);
				}
			});

			// prevbutton
			final Button prevButton = (Button) this.getView().findViewById(
					R.id.btn_story_prev);
			prevButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mItemizedoverlay.fithMaptOnTheStory();
					changeStep(actualStepPosition - 1);
					mItemizedoverlay.changeElementsonMap(actualStepPosition,
							story);
				}
			});

			// next button
			final Button nextButton = (Button) this.getView().findViewById(
					R.id.btn_story_next);
			nextButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mItemizedoverlay.fithMaptOnTheStory();
					changeStep(actualStepPosition + 1);
					mItemizedoverlay.changeElementsonMap(actualStepPosition,
							story);

				}
			});
			// reinit the story every time this fragment is loaded
			changeStep(-1);
			// hide the keyboard
			InputMethodManager imm = (InputMethodManager) getSherlockActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(nextButton.getWindowToken(), 0);
			changeTheMapConfiguration();
			mItemizedoverlay.fithMaptOnTheStory();

		}
	}

	private void updateAttending() {
		TextView tv;
		// attendees
		tv = (TextView) this.getView().findViewById(R.id.attendees_num);
		if (getStory().getAttendees() != null) {
			tv.setText(getStory().getAttendees() + " ");
		} else {
			tv.setText(" 0 ");
		}
	}

	/*
	 * Method used to change all the element in the fragment (except the Map) if
	 * the actualStepPosition is -1, shows the story's details if it is
	 * different, shows the others details and buttons
	 */

	private void changeStep(int i) {

		actualStepPosition = i;
		// detail of the story
		ScrollView detailStory = (ScrollView) this.getView().findViewById(
				R.id.story_details);
		// disable the step part
		ScrollView detailStep = (ScrollView) this.getView().findViewById(
				R.id.step_details);
		LinearLayout buttonStep = (LinearLayout) this.getView().findViewById(
				R.id.navigation_buttons);
		// start button
		LinearLayout startStory = (LinearLayout) this.getView().findViewById(
				R.id.start_buttons);

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
			if (story.getSteps().get(actualStepPosition) != null) {

				detailStory.setVisibility(View.GONE);
				startStory.setVisibility(View.GONE);
				detailStep.setVisibility(View.VISIBLE);
				buttonStep.setVisibility(View.VISIBLE);
				// number of the step
				TextView numberOfStepText = (TextView) this.getView()
						.findViewById(R.id.number_of_step);
				numberOfStepText
						.setText(String.valueOf(actualStepPosition + 1));
				// name of the step (if the POI hasn't been erased)
				TextView nameOfStepText = (TextView) this.getView()
						.findViewById(R.id.step_details_name);
				if (story.getSteps().get(actualStepPosition).assignedPoi() != null)
					nameOfStepText.setText(story.getSteps()
							.get(actualStepPosition).assignedPoi().getTitle());
				else
					nameOfStepText.setText(getString(R.string.poi_erased));
				// notes of the step
				TextView noteOfStepText = (TextView) this.getView()
						.findViewById(R.id.step_details_note);
				// if (story.getSteps().get(actualStepPosition).assignedPoi() !=
				// null)
				// noteOfStepText.setText(story.getSteps()
				// .get(actualStepPosition).getNote());
				// else
				// noteOfStepText.setText(" ");
				noteOfStepText.setText(story.getSteps().get(actualStepPosition)
						.getNote());
				Button nextStep = (Button) this.getView().findViewById(
						R.id.btn_story_next);

				// If it is at the end of the story, hides the "next" button
				if (actualStepPosition == story.getSteps().size() - 1)
					nextStep.setVisibility(View.GONE);
				else
					nextStep.setVisibility(View.VISIBLE);

			}
		}
		getSherlockActivity().invalidateOptionsMenu();

	}

	@Override
	public void onDestroyView() {
		try {
			FragmentTransaction transaction = getFragmentManager()
					.beginTransaction();

			transaction.remove(
					getFragmentManager()
							.findFragmentById(R.id.my_map_fragment1)).commit();
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
		getSherlockActivity().getSupportMenuInflater().inflate(R.menu.gripmenu,
				menu);

		SubMenu submenu = menu.getItem(0).getSubMenu();
		submenu.clear();

		if (actualStepPosition == -1 || getStory().getSteps() == null
				|| getStory().getSteps().size() == 0) {
			// story visualization
			String userId = DTHelper.getUserId();
			submenu.add(Menu.CATEGORY_SYSTEM, R.id.rate, Menu.NONE,
					R.string.rate);
			if (getStory().getAttending() == null
					|| getStory().getAttending().isEmpty()) {
				submenu.add(Menu.CATEGORY_SYSTEM, R.id.add_my_stories,
						Menu.NONE, R.string.add_my_stories);
			} else
				submenu.add(Menu.CATEGORY_SYSTEM, R.id.add_my_stories,
						Menu.NONE, R.string.delete_my_stories);
			if (getStory().getCommunityData().getFollowing()
					.containsKey(userId)) {
				submenu.add(Menu.CATEGORY_SYSTEM, R.id.unfollow, Menu.NONE,
						R.string.unfollow);
			} else {
				submenu.add(Menu.CATEGORY_SYSTEM, R.id.follow, Menu.NONE,
						R.string.follow);
			}
			submenu.add(Menu.CATEGORY_SYSTEM, R.id.edit_btn, Menu.NONE,
					R.string.edit);
			// CAN DELETE ONLY OWN STORY
			if (DTHelper.isOwnedObject(getStory())) {
				submenu.add(Menu.CATEGORY_SYSTEM, R.id.delete_btn, Menu.NONE,
						R.string.delete);
			}
		} else {
			// POI visualization
			if (getStory().getSteps().get(actualStepPosition).assignedPoi() != null) {
				submenu.add(Menu.CATEGORY_SYSTEM, R.id.related_step_btn,
						Menu.NONE, R.string.related_poi);
				submenu.add(Menu.CATEGORY_SYSTEM, R.id.direction_step_btn,
						Menu.NONE, R.string.getdir);

			}
			// CAN EDIT AND DELETE STEPS ONLY IN OWN STORIES
			if (DTHelper.isOwnedObject(getStory())) {
				// TODO implement the step cancellation!!!!
				// submenu.add(Menu.CATEGORY_SYSTEM, R.id.delete_step_btn,
				// Menu.NONE, R.string.delete);
				submenu.add(Menu.CATEGORY_SYSTEM, R.id.edit_step_btn,
						Menu.NONE, R.string.edit);
			}

		}
		super.onPrepareOptionsMenu(menu);
	}

	private void ratingDialog() {
		float rating = (story != null && story.getCommunityData() != null && story
				.getCommunityData().getAverageRating() > 0) ? story
				.getCommunityData().getAverageRating() : 2.5f;
		RatingHelper.ratingDialog(getActivity(), rating, new RatingProcessor(
				getActivity()), R.string.rating_story_dialog_title);
	}

	private void updateRating(Integer result) {
		getStory().getCommunityData().setAverageRating(result);
		RatingBar rating = (RatingBar) getView().findViewById(
				R.id.story_details_rating);
		rating.setRating(getStory().getCommunityData().getAverageRating());
	}

	private class RatingProcessor extends
			AbstractAsyncTaskProcessor<Integer, Integer> implements
			RatingHandler {

		public RatingProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public Integer performAction(Integer... params)
				throws SecurityException, Exception {
			return DTHelper.rate(story, params[0]);
		}

		@Override
		public void handleResult(Integer result) {
			updateRating(result);
			Toast.makeText(getSherlockActivity(), R.string.rating_success,
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onRatingChanged(float rating) {
			new SCAsyncTask<Integer, Void, Integer>(getActivity(), this)
					.execute((int) rating);
		}
	}

	/*
	 * Manage the options
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.rate) {
			if (new AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
				// show dialog box
				UserRegistration.upgradeuser(getSherlockActivity());
				return false;
			} else {
				ratingDialog();
				return true;
			}
		} else if (item.getItemId() == R.id.add_my_stories) {
			if (new AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
				// show dialog box
				UserRegistration.upgradeuser(getSherlockActivity());
				return false;
			} else {
				new SCAsyncTask<Boolean, Void, StoryObject>(getActivity(),
						new AttendProcessor(getActivity())).execute(getStory()
						.getAttending() == null
						|| getStory().getAttending().isEmpty());
				return true;
			}
		} else if (item.getItemId() == R.id.follow) {
			FollowEntityObject obj = new FollowEntityObject(getStory()
					.getEntityId(), getStory().getTitle(),
					DTConstants.ENTITY_TYPE_STORY);
			if (mFollowByIntent) {
				FollowHelper.follow(getSherlockActivity(), obj);
			} else {
				SCAsyncTask<Object, Void, Topic> followTask = new SCAsyncTask<Object, Void, Topic>(
						getSherlockActivity(), new FollowAsyncTaskProcessor(
								getSherlockActivity()));
				followTask.execute(getSherlockActivity()
						.getApplicationContext(), DTParamsHelper.getAppToken(),
						DTHelper.getAuthToken(), obj);
			}
			return true;
		} else if (item.getItemId() == R.id.unfollow) {
			BaseDTObject obj;
			try {
				obj = DTHelper.findStoryByEntityId(getStory().getEntityId());
				if (obj != null) {
					SCAsyncTask<BaseDTObject, Void, BaseDTObject> unfollowTask = new SCAsyncTask<BaseDTObject, Void, BaseDTObject>(
							getSherlockActivity(),
							new UnfollowAsyncTaskProcessor(
									getSherlockActivity()));
					unfollowTask.execute(obj);

				}
			} catch (Exception e) {
				Log.e(EventDetailsFragment.class.getName(), String.format(
						"Error unfollowing event %s", getStory().getEntityId()));
			}
			return true;
		} else if (item.getItemId() == R.id.edit_btn) {
			if (new AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
				// show dialog box
				UserRegistration.upgradeuser(getSherlockActivity());
				return false;
			} else {
				FragmentTransaction fragmentTransaction = getSherlockActivity()
						.getSupportFragmentManager().beginTransaction();
				Fragment fragment = new CreateStoryFragment();
				Bundle args = new Bundle();
				args.putSerializable(CreateStoryFragment.ARG_STORY, story);
				fragment.setArguments(args);
				fragmentTransaction
						.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				fragmentTransaction.replace(android.R.id.content, fragment,
						"stories");
				fragmentTransaction.addToBackStack(fragment.getTag());
				fragmentTransaction.commit();
				return true;
			}
		} else if (item.getItemId() == R.id.delete_btn) {
			if (new AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
				// show dialog box
				UserRegistration.upgradeuser(getSherlockActivity());
				return false;
			} else {
				new SCAsyncTask<StoryObject, Void, Boolean>(getActivity(),
						new StoryDeleteProcessor(getActivity())).execute(story);
				return true;
			}
		} else if (item.getItemId() == R.id.related_step_btn) {
			FragmentTransaction fragmentTransaction = getSherlockActivity()
					.getSupportFragmentManager().beginTransaction();
			PoiDetailsFragment fragment = new PoiDetailsFragment();
			Bundle args = new Bundle();
			args.putSerializable(PoiDetailsFragment.ARG_POI, getStory()
					.getSteps().get(actualStepPosition).assignedPoi());
			fragment.setArguments(args);
			fragmentTransaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(android.R.id.content, fragment,
					"stories");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
			return true;
		} else if (item.getItemId() == R.id.direction_step_btn) {
			Address to = getStory().getSteps().get(actualStepPosition)
					.assignedPoi().asGoogleAddress();
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
			if (new AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
				// show dialog box
				UserRegistration.upgradeuser(getSherlockActivity());
				return false;
			} else {
				FragmentTransaction fragmentTransaction = getSherlockActivity()
						.getSupportFragmentManager().beginTransaction();
				AddStepToStoryFragment fragment = new AddStepToStoryFragment();
				Bundle args = new Bundle();
				args.putParcelable(AddStepToStoryFragment.ARG_STEP_HANDLER,
						stepHandler);
				args.putSerializable(AddStepToStoryFragment.ARG_STORY_OBJECT,
						story);
				args.putInt(AddStepToStoryFragment.ARG_STEP_POSITION,
						actualStepPosition);
				fragment.setArguments(args);
				fragmentTransaction
						.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				fragmentTransaction.replace(android.R.id.content, fragment,
						"stories");
				fragmentTransaction.addToBackStack(fragment.getTag());
				fragmentTransaction.commit();
				return true;
			}
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * load on the map the story passed by parameter using an asynch task.
	 */

	@Override
	public void setPOIStoryToLoad(final StoryObject story) {

		mItemizedoverlay.clearMarkers();

		new SCAsyncTask<Void, Void, Collection<? extends BaseDTObject>>(
				getActivity(), new MapStoryLoadProcessor(getActivity(),
						mItemizedoverlay, mapViewStory) {
					@Override
					protected Collection<? extends BaseDTObject> getObjects() {
						try {
							ArrayList<POIObject> poiList = new ArrayList<POIObject>();
							poiList = DTHelper.getPOIBySteps(story.getSteps());
							for (int i = 0; i < poiList.size(); i++) {
								story.getSteps().get(i)
										.assignPoi(poiList.get(i));
							}
							return poiList;
						} catch (Exception e) {
							e.printStackTrace();
							return Collections.emptyList();
						}
					}
				}).execute();
	}

	/*
	 * Method used by MyMapFragment to load the MapView. It requires the reset
	 * of the map
	 */
	public void setMap(MapView mapViewStory) {

		this.mapViewStory = mapViewStory;
		changeTheMapConfiguration();
	}

	/*
	 * Reset of the mapView and its overlays
	 */
	private void changeTheMapConfiguration() {
		mapViewStory.setClickable(true);
		mapViewStory.getController().setZoom(15);
		List<Overlay> listOfOverlays = mapViewStory.getOverlays();

		mItemizedoverlay = new DTStoryItemizedOverlay(getSherlockActivity(),
				mapViewStory, story);
		mItemizedoverlay.setMapItemTapListener(this);
		listOfOverlays.add(mItemizedoverlay);
		setPOIStoryToLoad(story);
		mItemizedoverlay.fithMaptOnTheStory();

	}

	/*
	 * implementation of the BasicObjectMapItemTapListener interface method,
	 * called when a user taps on an element it's called by
	 * DTStoryItemizedOverlay.onTap anche checks which element is clicked,
	 * compared with the element in the story, set the actual posistion and
	 * modifies the layout
	 */
	@Override
	public void onBasicObjectTap(BasicObject o) {
		// TODO Auto-generated method stub
		POIObject poiTapped = (POIObject) o;
		for (int i = 0; i < story.getSteps().size(); i++)
			if (story.getSteps().get(i).assignedPoi() != null)
				if (poiTapped.getId().equals(
						story.getSteps().get(i).assignedPoi().getId())) {
					actualStepPosition = i;
					changeStep(actualStepPosition);
				}
	}

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
		public void addStep(StepObject step) {
			story.getSteps().add(step);
			mItemizedoverlay.fithMaptOnTheStory();

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
		public void updateStep(final StepObject step, final Integer position) {
			// generate dialog box for confirming the update
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			// Add the buttons
			builder.setMessage(getActivity().getString(R.string.sure_change));
			builder.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// User clicked OK button
							story.getSteps().set(position, step);
							new SCAsyncTask<StoryObject, Void, Boolean>(
									getActivity(), new CreateStoryProcessor(
											getActivity())).execute(story);

						}
					});
			builder.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {
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
	private class StoryDeleteProcessor extends
			AbstractAsyncTaskProcessor<StoryObject, Boolean> {
		public StoryDeleteProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public Boolean performAction(StoryObject... params)
				throws SecurityException, Exception {
			return DTHelper.deleteStory(params[0]);
		}

		@Override
		public void handleResult(Boolean result) {
			if (result) {
				getSherlockActivity().getSupportFragmentManager()
						.popBackStack();
			} else {
				Toast.makeText(getSherlockActivity(),
						R.string.app_failure_cannot_delete, Toast.LENGTH_LONG)
						.show();
			}
		}

	}

	/*
	 * Attend to a story
	 */
	private class AttendProcessor extends
			AbstractAsyncTaskProcessor<Boolean, StoryObject> {

		public AttendProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public StoryObject performAction(Boolean... params)
				throws SecurityException, Exception {
			if (params[0])
				return DTHelper.addToMyStories(getStory());
			return DTHelper.removeFromMyStories(getStory());
		}

		@Override
		public void handleResult(StoryObject result) {
			story = result;
			updateAttending();
			changeTheMapConfiguration();
			getSherlockActivity().invalidateOptionsMenu();
			if (getStory().getAttending() == null
					|| getStory().getAttending().isEmpty())
				Toast.makeText(getSherlockActivity(),
						R.string.not_attend_story_success, Toast.LENGTH_SHORT)
						.show();
			else
				Toast.makeText(getSherlockActivity(),
						R.string.attend_story_success, Toast.LENGTH_SHORT)
						.show();
		}

	}

	/*
	 * Create a story
	 */
	private class CreateStoryProcessor extends
			AbstractAsyncTaskProcessor<StoryObject, Boolean> {

		public CreateStoryProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public Boolean performAction(StoryObject... params)
				throws SecurityException, Exception {
			return DTHelper.saveStory(params[0]);
		}

		@Override
		public void handleResult(Boolean result) {
			if (result) {
				Toast.makeText(getSherlockActivity(),
						R.string.story_create_success, Toast.LENGTH_SHORT)
						.show();
				getSherlockActivity().getSupportFragmentManager()
						.popBackStack();

			} else {
				Toast.makeText(getSherlockActivity(), R.string.update_success,
						Toast.LENGTH_SHORT).show();
				getSherlockActivity().getSupportFragmentManager()
						.popBackStack();

			}
			mItemizedoverlay.fithMaptOnTheStory();
		}
	}

}
