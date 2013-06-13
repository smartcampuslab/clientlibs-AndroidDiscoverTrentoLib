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
package eu.trentorise.smartcampus.dt.fragments.events;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.google.android.maps.GeoPoint;

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
import eu.trentorise.smartcampus.dt.custom.CategoryHelper;
import eu.trentorise.smartcampus.dt.custom.RatingHelper;
import eu.trentorise.smartcampus.dt.custom.RatingHelper.RatingHandler;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
import eu.trentorise.smartcampus.dt.custom.data.FollowAsyncTaskProcessor;
import eu.trentorise.smartcampus.dt.custom.data.UnfollowAsyncTaskProcessor;
import eu.trentorise.smartcampus.dt.custom.map.MapManager;
import eu.trentorise.smartcampus.dt.fragments.pois.PoiDetailsFragment;
import eu.trentorise.smartcampus.dt.model.BaseDTObject;
import eu.trentorise.smartcampus.dt.model.Concept;
import eu.trentorise.smartcampus.dt.model.DTConstants;
import eu.trentorise.smartcampus.dt.model.EventObject;
import eu.trentorise.smartcampus.dt.model.POIObject;
import eu.trentorise.smartcampus.dt.model.TmpComment;
import eu.trentorise.smartcampus.dt.notifications.NotificationsSherlockFragmentDT;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class EventDetailsFragment extends NotificationsSherlockFragmentDT {
	public static final String ARG_EVENT_OBJECT = "event_object";
	private POIObject poi = null;
	private EventObject mEvent = null;
	private TmpComment tmp_comments[];
	private boolean mFollowByIntent;
	private String eventId;

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setHasOptionsMenu(true);

		tmp_comments = new TmpComment[0];
		// tmp_comments = new TmpComment[5];
		for (int i = 0; i < tmp_comments.length; i++)
			tmp_comments[i] = new TmpComment(
					"This is a very nice, detailed and lengthy comment about the event",
					"student", new Date());
		setFollowByIntent();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.eventdetails, container, false);
	}

	private POIObject getPOI() {
		if (poi == null) {
			getEvent();
		}
		return poi;
	}

	private EventObject getEvent() {
		Bundle bundle = this.getArguments();
		if (eventId == null)
			eventId = bundle.getString(ARG_EVENT_OBJECT);
		mEvent = DTHelper.findEventById(eventId);
		if (mEvent != null) {
			poi = DTHelper.findPOIById(mEvent.getPoiId());
			mEvent.assignPoi(poi);
		}
		return mEvent;
	}

	@Override
	public void onStart() {
		super.onStart();
		EventObject event = getEvent();
		if (event != null) {
			ImageView certifiedBanner = (ImageView) this.getView()
					.findViewById(R.id.banner_certified);
			if (CategoryHelper.FAMILY_CATEGORY_EVENT.equals(event.getType())
					&& isCertified(event))
				certifiedBanner.setVisibility(View.VISIBLE);
			else
				certifiedBanner.setVisibility(View.GONE);

			// title
			TextView tv = (TextView) this.getView().findViewById(
					R.id.event_details_title);
			tv.setText(event.getTitle());

			// timing
			tv = (TextView) this.getView().findViewById(R.id.event_timing);
			if (getEvent().getTiming() != null
					&& event.getTiming().length() > 0) {
				tv.setText(event.getTimingFormatted());
			} else {
				((LinearLayout) this.getView().findViewById(R.id.eventdetails))
						.removeView(tv);
			}

			// description, optional
			tv = (TextView) this.getView().findViewById(
					R.id.event_details_descr);
			if (event.getDescription() != null
					&& event.getDescription().length() > 0) {
				tv.setText(event.getFormattedDescription());
			} else {
				((LinearLayout) this.getView().findViewById(R.id.eventdetails))
						.removeView(tv);
			}

			// notes
			tv = (TextView) this.getView().findViewById(
					R.id.event_details_notes);
			if (event.getCommunityData() != null
					&& event.getCommunityData().getNotes() != null
					&& event.getCommunityData().getNotes().length() > 0) {
				tv.setText(event.getCommunityData().getNotes());
			} else {
				((LinearLayout) this.getView().findViewById(R.id.eventdetails))
						.removeView(tv);
			}

			// location
			tv = (TextView) this.getView().findViewById(R.id.event_details_loc);
			POIObject poi = getPOI();
			if (poi != null)
				tv.setText(poi.shortAddress());
			else
				((LinearLayout) this.getView().findViewById(R.id.eventdetails))
						.removeView(tv);

			// tags
			tv = (TextView) this.getView()
					.findViewById(R.id.event_details_tags);
			if (event.getCommunityData() != null
					&& event.getCommunityData().getTags() != null
					&& event.getCommunityData().getTags().size() > 0) {
				tv.setText(Concept.toSimpleString(event.getCommunityData()
						.getTags()));
			} else {
				((LinearLayout) this.getView().findViewById(R.id.eventdetails))
						.removeView(tv);
			}

			// date
			tv = (TextView) this.getView()
					.findViewById(R.id.event_details_date);
			if (event.getFromTime() != null && event.getFromTime() > 0) {
				CharSequence fromTime = event.dateTimeString();
				CharSequence toTime = event.toDateTimeString();
				if (fromTime.equals(toTime)) {
					tv.setText(fromTime);
				} else {
					tv.setText(fromTime + " - " + toTime);
				}
			} else {
				tv.setText("");
			}

			// multimedia
			((LinearLayout) getView().findViewById(R.id.multimedia_source))
					.removeView(getView().findViewById(R.id.gallery_btn));

			/*
			 * ImageButton b = (ImageButton) getView().findViewById(
			 * R.id.gallery_btn); if (hasMultimediaAttached())
			 * b.setOnClickListener(new OnClickListener() {
			 * 
			 * @Override public void onClick(View v) { FragmentTransaction
			 * fragmentTransaction = getSherlockActivity()
			 * .getSupportFragmentManager().beginTransaction(); GalleryFragment
			 * fragment = new GalleryFragment(); Bundle args = new Bundle(); //
			 * add args args.putString("title", getEvent().getTitle());
			 * fragment.setArguments(args); fragmentTransaction
			 * .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			 * fragmentTransaction.replace(android.R.id.content, fragment,
			 * "gallery");
			 * fragmentTransaction.addToBackStack(fragment.getTag());
			 * fragmentTransaction.commit(); } }); else ((LinearLayout)
			 * this.getView().findViewById(R.id.tablerow)) .removeView(b);
			 */

			// source
			tv = (TextView) this.getView().findViewById(
					R.id.event_details_source);
			if (event.getSource() != null && event.getSource().length() > 0) {
				tv.setText(event.getSource());
			} else if (event.createdByUser()) {
				tv.setText(getString(R.string.source_smartcampus));
			} else {
				((LinearLayout) this.getView().findViewById(R.id.eventdetails))
						.removeView(tv);
			}

			// rating
			/*
			 * It may be not useful to rate events a posteriori, unless they are
			 * recurrent (which is a situation we do not handle)
			 */
			RatingBar rating = (RatingBar) getView().findViewById(
					R.id.event_rating);
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

			if (event.getCommunityData() != null) {
				rating.setRating(event.getCommunityData().getAverageRating());
			}
			updateAttending();

			if (tmp_comments.length > 0) {
				// Comments
				LinearLayout commentsList = (LinearLayout) getView()
						.findViewById(R.id.comments_list);
				for (int i = 0; i < tmp_comments.length; i++) {
					View entry = getSherlockActivity().getLayoutInflater()
							.inflate(R.layout.comment_row, null);

					TextView tmp = (TextView) entry
							.findViewById(R.id.comment_text);
					tmp.setText(tmp_comments[i].getText());
					tmp = (TextView) entry.findViewById(R.id.comment_author);
					tmp.setText(tmp_comments[i].getAuthor());
					tmp = (TextView) entry.findViewById(R.id.comment_date);
					tmp.setText(tmp_comments[i].getDate());
					commentsList.addView(entry);
				}
			} else {
				((LinearLayout) getView().findViewById(R.id.eventdetails))
						.removeView(getView().findViewById(R.id.event_comments));
				((LinearLayout) getView().findViewById(R.id.eventdetails))
						.removeView(getView().findViewById(R.id.comments_list));
				((LinearLayout) getView().findViewById(R.id.eventdetails))
						.removeView(getView().findViewById(
								R.id.event_comments_separator));

			}
		}

	}

	private boolean isCertified(EventObject event) {
		if ((Boolean) event.getCustomData().get("certified"))
			return true;
		else
			return false;

	}

	private void updateAttending() {
		TextView tv;
		// attendees
		tv = (TextView) this.getView().findViewById(R.id.attendees_num);
		if (getEvent().getAttendees() != null) {
			tv.setText(getEvent().getAttendees() + " "
					+ getString(R.string.attendees_extended));
		} else {
			tv.setText("0 " + getString(R.string.attendees_extended));
		}
	}

	/*
	 * private boolean hasMultimediaAttached() { return true; }
	 */

	private void updateRating(Integer result) {
		getEvent().getCommunityData().setAverageRating(result);
		RatingBar rating = (RatingBar) getView()
				.findViewById(R.id.event_rating);
		rating.setRating(getEvent().getCommunityData().getAverageRating());
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		getSherlockActivity().getSupportMenuInflater().inflate(R.menu.gripmenu,
				menu);
		String userId = DTHelper.getUserId();
		SubMenu submenu = menu.getItem(0).getSubMenu();
		submenu.clear();
		submenu.add(Menu.CATEGORY_SYSTEM, R.id.rate, Menu.NONE, R.string.rate);
		EventObject event = getEvent();

		if (event.getAttending() == null || event.getAttending().isEmpty()) {
			submenu.add(Menu.CATEGORY_SYSTEM, R.id.attend, Menu.NONE,
					R.string.attend);
		} else {
			submenu.add(Menu.CATEGORY_SYSTEM, R.id.attend, Menu.NONE,
					R.string.attend_not);
		}
		if (getEvent().getCommunityData().getFollowing().containsKey(userId)) {
			submenu.add(Menu.CATEGORY_SYSTEM, R.id.unfollow, Menu.NONE,
					R.string.unfollow);
		} else {

			submenu.add(Menu.CATEGORY_SYSTEM, R.id.follow, Menu.NONE,
					R.string.follow);
		}

		if (getPOI() != null) {
			submenu.add(Menu.CATEGORY_SYSTEM, R.id.see_on_map, Menu.NONE,
					R.string.onmap);
			submenu.add(Menu.CATEGORY_SYSTEM, R.id.show_related_poi, Menu.NONE,
					R.string.related_poi);
			submenu.add(Menu.CATEGORY_SYSTEM, R.id.get_dir, Menu.NONE,
					R.string.getdir);
		}
		submenu.add(Menu.CATEGORY_SYSTEM, R.id.edit_btn, Menu.NONE,
				R.string.edit);
		// CAN DELETE ONLY OWN OBJECTS
		if (DTHelper.isOwnedObject(getEvent())) {
			submenu.add(Menu.CATEGORY_SYSTEM, R.id.delete_btn, Menu.NONE,
					R.string.delete);
		}

		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.show_related_poi) {
			FragmentTransaction fragmentTransaction = getSherlockActivity()
					.getSupportFragmentManager().beginTransaction();
			PoiDetailsFragment fragment = new PoiDetailsFragment();
			Bundle args = new Bundle();
			args.putSerializable(PoiDetailsFragment.ARG_POI, getPOI());
			fragment.setArguments(args);
			fragmentTransaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			// fragmentTransaction.detach(this);
			fragmentTransaction.replace(android.R.id.content, fragment,
					"events");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
			return true;
		} else if (item.getItemId() == R.id.get_dir) {
			// if (bringMeThere(getEvent())){
			// Address to = getPOI().asGoogleAddress();
			// Address from = null;
			// GeoPoint mylocation =
			// MapManager.requestMyLocation(getActivity());
			// if (mylocation != null) {
			// from = new Address(Locale.getDefault());
			// from.setLatitude(mylocation.getLatitudeE6() / 1E6);
			// from.setLongitude(mylocation.getLongitudeE6() / 1E6);
			// }
			// NavigationHelper.bringMeThere(getActivity(), from, to);
			bringMeThere(getEvent());
			return true;
			// }
			// else return false;
		} else if (item.getItemId() == R.id.see_on_map) {
			ArrayList<BaseDTObject> list = new ArrayList<BaseDTObject>();
			getEvent().setLocation(poi.getLocation());
			list.add(getEvent());
			MapManager.switchToMapView(list, this);
			return true;
		} else if (item.getItemId() == R.id.follow) {
			FollowEntityObject obj = new FollowEntityObject(getEvent()
					.getEntityId(), getEvent().getTitle(),
					DTConstants.ENTITY_TYPE_EVENT);
			if (mFollowByIntent) {
				// FollowHelper.follow(getSherlockActivity(), obj);
				FollowHelper.follow(this, obj, 3000);
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
				obj = DTHelper.findEventByEntityId(getEvent().getEntityId());
				if (obj != null) {
					SCAsyncTask<BaseDTObject, Void, BaseDTObject> unfollowTask = new SCAsyncTask<BaseDTObject, Void, BaseDTObject>(
							getSherlockActivity(),
							new UnfollowAsyncTaskProcessor(
									getSherlockActivity()));
					unfollowTask.execute(obj);

				}
			} catch (Exception e) {
				Log.e(EventDetailsFragment.class.getName(), String.format(
						"Error unfollowing event %s", getEvent().getEntityId()));
			}
			return true;
		} else if (item.getItemId() == R.id.rate) {
			if (new AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
				// show dialog box
				UserRegistration.upgradeuser(getSherlockActivity());
				return false;
			} else {
				ratingDialog();
				return true;
			}
		} else if (item.getItemId() == R.id.attend) {
			if (new AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
				// show dialog box
				UserRegistration.upgradeuser(getSherlockActivity());
				return false;
			} else {
				new SCAsyncTask<Boolean, Void, EventObject>(getActivity(),
						new AttendProcessor(getActivity())).execute(getEvent()
						.getAttending() == null
						|| getEvent().getAttending().isEmpty());
				return true;
			}
		} else if (item.getItemId() == R.id.edit_btn) {
			if (new AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
				// show dialog box
				UserRegistration.upgradeuser(getSherlockActivity());
				return false;
			} else {
				FragmentTransaction fragmentTransaction = getSherlockActivity()
						.getSupportFragmentManager().beginTransaction();
				Fragment fragment = new CreateEventFragment();
				Bundle args = new Bundle();
				// args.putSerializable(CreateEventFragment.ARG_EVENT,
				// getEvent());
				args.putString(CreateEventFragment.ARG_EVENT, getEvent()
						.getId());
				fragment.setArguments(args);
				fragmentTransaction
						.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				// fragmentTransaction.detach(this);
				fragmentTransaction.replace(android.R.id.content, fragment,
						"events");
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
				new SCAsyncTask<EventObject, Void, Boolean>(getActivity(),
						new EventDeleteProcessor(getActivity()))
						.execute(getEvent());
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
				eu.trentorise.smartcampus.cm.model.Topic topic = (eu.trentorise.smartcampus.cm.model.Topic) data
						.getSerializableExtra("topic");
				new FollowAsyncTask().execute(topic.getId());
				// fix to avoid onActivityResult DiscoverTrentoActivity failure
				data.putExtra(AccountManager.KEY_AUTHTOKEN,
						DTHelper.getAuthToken());
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onResume() {
		// getSherlockActivity().invalidateOptionsMenu();
		super.onResume();
	}

	private void bringMeThere(EventObject eventObject) {
		AlertDialog.Builder builder;

		builder = new AlertDialog.Builder(getSherlockActivity());
		/* check event Object */
		if (CategoryHelper.FAMILY_CATEGORY_EVENT.equals(eventObject.getType())) {
			/* if it's not a family event, no problem */
			return;
		} else {
			/* if it is, show the dialog box */
			/* press true return true, press false return false */
			DialogInterface.OnClickListener updateDialogClickListener;

			updateDialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {

					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						// upgrade the user
						Address to = getPOI().asGoogleAddress();
						Address from = null;
						GeoPoint mylocation = MapManager
								.requestMyLocation(getActivity());
						if (mylocation != null) {
							from = new Address(Locale.getDefault());
							from.setLatitude(mylocation.getLatitudeE6() / 1E6);
							from.setLongitude(mylocation.getLongitudeE6() / 1E6);
						}
						NavigationHelper.bringMeThere(getActivity(), from, to);
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						// CLOSE

						break;

					}

				}
			};
			builder.setCancelable(false)
					.setMessage(
							getSherlockActivity().getString(
									R.string.warning_for_direction))
					.setPositiveButton(android.R.string.yes,
							updateDialogClickListener)
					.setNegativeButton(R.string.cancel,
							updateDialogClickListener).show();
		}
		return;
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
			Log.e(EventDetailsFragment.class.getName(),
					"you should set the follow-by-intent metadata in app manifest");
		}

	}

	private void ratingDialog() {
		float rating = (getEvent() != null
				&& getEvent().getCommunityData() != null && getEvent()
				.getCommunityData().getAverageRating() > 0) ? getEvent()
				.getCommunityData().getAverageRating() : 2.5f;
		RatingHelper.ratingDialog(getActivity(), rating, new RatingProcessor(
				getActivity()), R.string.rating_event_dialog_title);
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
			return DTHelper.rate(getEvent(), params[0]);
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

	private class EventDeleteProcessor extends
			AbstractAsyncTaskProcessor<EventObject, Boolean> {
		public EventDeleteProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public Boolean performAction(EventObject... params)
				throws SecurityException, Exception {
			return DTHelper.deleteEvent(params[0]);
		}

		@Override
		public void handleResult(Boolean result) {
			if (result) {
				getSherlockActivity().getSupportFragmentManager()
						.popBackStack();
			} else {
				Toast.makeText(
						getActivity(),
						getActivity().getString(
								R.string.app_failure_cannot_delete),
						Toast.LENGTH_LONG).show();
			}
		}

	}

	private class AttendProcessor extends
			AbstractAsyncTaskProcessor<Boolean, EventObject> {

		public AttendProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public EventObject performAction(Boolean... params)
				throws SecurityException, Exception {
			if (params[0])
				return DTHelper.attend(getEvent());
			return DTHelper.notAttend(getEvent());
		}

		@Override
		public void handleResult(EventObject result) {
			mEvent = result;
			updateAttending();
			getSherlockActivity().invalidateOptionsMenu();
			EventObject event = getEvent();
			if (event.getAttending() == null || event.getAttending().isEmpty())
				Toast.makeText(getSherlockActivity(),
						R.string.not_attend_success, Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(getSherlockActivity(), R.string.attend_success,
						Toast.LENGTH_SHORT).show();
		}

	}

	class FollowAsyncTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			String topicId = params[0];
			try {
				DTHelper.follow(DTHelper.findEventById(eventId), topicId);
			} catch (Exception e) {
				Log.e(FollowAsyncTask.class.getName(),
						String.format("Exception following event %s", eventId));
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			getSherlockActivity().invalidateOptionsMenu();
		}

	}
}
