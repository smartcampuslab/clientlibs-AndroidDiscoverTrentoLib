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
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.google.android.maps.GeoPoint;

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.common.follow.model.Topic;
import eu.trentorise.smartcampus.android.common.navigation.NavigationHelper;
import eu.trentorise.smartcampus.android.feedback.fragment.FeedbackFragment;
import eu.trentorise.smartcampus.dt.DiscoverTrentoActivity;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.dt.custom.RatingHelper;
import eu.trentorise.smartcampus.dt.custom.RatingHelper.RatingHandler;
import eu.trentorise.smartcampus.dt.custom.Utils;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
import eu.trentorise.smartcampus.dt.custom.data.FollowAsyncTaskProcessor;
import eu.trentorise.smartcampus.dt.custom.data.UnfollowAsyncTaskProcessor;
import eu.trentorise.smartcampus.dt.custom.map.MapManager;
import eu.trentorise.smartcampus.dt.fragments.pois.PoiDetailsFragment;
import eu.trentorise.smartcampus.dt.model.LocalEventObject;
import eu.trentorise.smartcampus.dt.model.TmpComment;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;
import eu.trentorise.smartcampus.territoryservice.model.CommunityData;
import eu.trentorise.smartcampus.territoryservice.model.POIObject;

public class EventDetailsFragment extends FeedbackFragment {
	public static final String ARG_EVENT_ID = "event_id";

	private boolean mStart = true;
	private boolean mCanceledFollow = false;

	private POIObject mPoi = null;
	private LocalEventObject mEvent = null;
	private TmpComment tmp_comments[];
	private String mEventId;

	private CompoundButton followButtonView;
	private Fragment mFragment = this;

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		setHasOptionsMenu(true);

		if (getArguments() != null) {
			mEventId = getArguments().getString(ARG_EVENT_ID);
			mEvent = DTHelper.findEventById(mEventId);
		}

		tmp_comments = new TmpComment[0];
		// tmp_comments = new TmpComment[5];
		for (int i = 0; i < tmp_comments.length; i++)
			tmp_comments[i] = new TmpComment("This is a very nice, detailed and lengthy comment about the event",
					"student", new Date());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.eventdetails, container, false);
	}

	private POIObject getPOI() {
		if (mPoi == null) {
			getEvent();
		}
		return mPoi;
	}

	private LocalEventObject getEvent() {
		if (mEventId == null) {
			mEventId = getArguments().getString(ARG_EVENT_ID);
		}

		if (mEvent == null) {
			mEvent = DTHelper.findEventById(mEventId);
		}
		if (mEvent != null) {
			mPoi = DTHelper.findPOIById(mEvent.getPoiId());
			mEvent.assignPoi(mPoi);
		}

		return mEvent;
	}

	@Override
	public void onStart() {
		super.onStart();
		DiscoverTrentoActivity.mDrawerToggle.setDrawerIndicatorEnabled(false);
		DiscoverTrentoActivity.drawerState = "off";
		getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(true);
		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(true);
		mEvent = getEvent();
		if (mEvent != null) {
			// title
			TextView tv = (TextView) this.getView().findViewById(R.id.event_details_title);
			tv.setText(mEvent.getTitle());

			/*
			 * BUTTONS
			 */
			// follow/unfollow
			if (mStart) {
				ToggleButton followTbtn = (ToggleButton) this.getView().findViewById(R.id.event_details_follow_tbtn);
				if (getEvent().getCommunityData().getFollowing().containsKey(DTHelper.getUserId())) {
					followTbtn.setBackgroundResource(R.drawable.ic_btn_monitor_on);
					followTbtn.setChecked(true);
				} else {
					followTbtn.setBackgroundResource(R.drawable.ic_btn_monitor_off);
					followTbtn.setChecked(false);
				}

				followTbtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if (!mCanceledFollow) {
							if (isChecked) {
								// FOLLOW
								{
									SCAsyncTask<Object, Void, BaseDTObject> followTask = new SCAsyncTask<Object, Void, BaseDTObject>(
											getSherlockActivity(), new FollowAsyncTaskProcessor(getSherlockActivity(),
													buttonView));
									followTask.execute(mEvent);
								}
							} else {
								// UNFOLLOW
								SCAsyncTask<BaseDTObject, Void, BaseDTObject> unfollowTask = new SCAsyncTask<BaseDTObject, Void, BaseDTObject>(
										getSherlockActivity(), new UnfollowAsyncTaskProcessor(getSherlockActivity(),
												buttonView));
								unfollowTask.execute(mEvent);

							}
						} else {
							mCanceledFollow = false;
						}
					}
				});
			}

			// attend
			ToggleButton attendTbtn = (ToggleButton) this.getView().findViewById(R.id.event_details_attend_tbtn);
			if (getEvent().getAttending() == null || getEvent().getAttending().isEmpty()) {
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
						new SCAsyncTask<Boolean, Void, LocalEventObject>(getActivity(), new AttendProcessor(
								getSherlockActivity(), buttonView)).execute(getEvent().getAttending() == null
								|| getEvent().getAttending().isEmpty());
					}
				}
			});

			// map
			ImageButton mapBtn = (ImageButton) getView().findViewById(R.id.event_details_map);
			mapBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mPoi != null) {
						ArrayList<BaseDTObject> list = new ArrayList<BaseDTObject>();
						getEvent().setLocation(mPoi.getLocation());
						list.add(getEvent());
						MapManager.switchToMapView(list, mFragment);
					} else {
						Toast.makeText(getSherlockActivity(), R.string.toast_poi_not_found, Toast.LENGTH_SHORT).show();
					}
				}
			});

			// directions
			ImageButton directionsBtn = (ImageButton) getView().findViewById(R.id.event_details_directions);
			directionsBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mPoi != null) {

						bringMeThere(getEvent());
					} else {
						Toast.makeText(getSherlockActivity(), R.string.toast_poi_not_found, Toast.LENGTH_SHORT).show();
					}
				}
			});
			/*
			 * END BUTTONS
			 */

			// location
			tv = (TextView) this.getView().findViewById(R.id.event_details_loc);
			POIObject poi = getPOI();
			if (poi != null) {
				tv.setText(Utils.getPOIshortAddress(poi));
			} else {
				((LinearLayout) this.getView().findViewById(R.id.eventdetails)).removeView(tv);
			}

			// timing
			tv = (TextView) this.getView().findViewById(R.id.event_timing);
			if (getEvent().getTiming() != null && mEvent.getTiming().length() > 0) {
				tv.setText(mEvent.getTimingFormatted());
			} else {
				((LinearLayout) this.getView().findViewById(R.id.eventdetails)).removeView(tv);
			}

			// description, optional
			tv = (TextView) this.getView().findViewById(R.id.event_details_descr);
			if (mEvent.getDescription() != null && mEvent.getDescription().length() > 0) {
				tv.setText(mEvent.getFormattedDescription(getActivity(), tv));
			} else {
				((LinearLayout) this.getView().findViewById(R.id.eventdetails)).removeView(tv);
			}

			// notes
			tv = (TextView) this.getView().findViewById(R.id.event_details_notes);
			// if (mEvent.getCommunityData() != null &&
			// mEvent.getCommunityData().getNotes() != null
			// && mEvent.getCommunityData().getNotes().length() > 0) {
			// tv.setText(mEvent.getCommunityData().getNotes());

			// } else {
			((LinearLayout) this.getView().findViewById(R.id.eventdetails)).removeView(tv);
			// }

			// tags
			tv = (TextView) this.getView().findViewById(R.id.event_details_tags);
			if (mEvent.getCommunityData() != null && mEvent.getCommunityData().getTags() != null
					&& mEvent.getCommunityData().getTags().size() > 0) {
				tv.setText(Utils.conceptToSimpleString(mEvent.getCommunityData().getTags()));
			} else {
				((LinearLayout) this.getView().findViewById(R.id.eventdetails)).removeView(tv);
			}

			// date
			tv = (TextView) this.getView().findViewById(R.id.event_details_date);
			if (mEvent.getFromTime() != null && mEvent.getFromTime() > 0) {
				CharSequence fromTime = mEvent.dateTimeString();
				CharSequence toTime = mEvent.toDateTimeString();
				if (fromTime.equals(toTime)) {
					tv.setText(fromTime);
				} else {
					tv.setText(fromTime + " - " + toTime);
				}
			} else {
				tv.setText("");
			}

			// multimedia
			((LinearLayout) getView().findViewById(R.id.multimedia_source)).removeView(getView().findViewById(
					R.id.gallery_btn));

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
			tv = (TextView) this.getView().findViewById(R.id.event_details_source);
			if (mEvent.getSource() != null && mEvent.getSource().length() > 0) {
				tv.setText(mEvent.getSource());
			} else if (mEvent.createdByUser()) {
				tv.setText(getString(R.string.source_smartcampus));
			} else {
				((LinearLayout) this.getView().findViewById(R.id.eventdetails)).removeView(tv);
			}

			// rating
			/*
			 * It may be not useful to rate events a posteriori, unless they are
			 * recurrent (which is a situation we do not handle)
			 */
			RatingBar rating = (RatingBar) getView().findViewById(R.id.event_rating);
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

			if (tmp_comments.length > 0) {
				// Comments
				LinearLayout commentsList = (LinearLayout) getView().findViewById(R.id.comments_list);
				for (int i = 0; i < tmp_comments.length; i++) {
					View entry = getSherlockActivity().getLayoutInflater().inflate(R.layout.comment_row, null);

					TextView tmp = (TextView) entry.findViewById(R.id.comment_text);
					tmp.setText(tmp_comments[i].getText());
					tmp = (TextView) entry.findViewById(R.id.comment_author);
					tmp.setText(tmp_comments[i].getAuthor());
					tmp = (TextView) entry.findViewById(R.id.comment_date);
					tmp.setText(tmp_comments[i].getDate());
					commentsList.addView(entry);
				}
			} else {
				((LinearLayout) getView().findViewById(R.id.eventdetails)).removeView(getView().findViewById(
						R.id.event_comments));
				((LinearLayout) getView().findViewById(R.id.eventdetails)).removeView(getView().findViewById(
						R.id.comments_list));
				((LinearLayout) getView().findViewById(R.id.eventdetails)).removeView(getView().findViewById(
						R.id.event_comments_separator));
			}
		}

	}

	private void updateAttending() {
		TextView tv;
		if (this.getView() != null) {
			// attendees
			tv = (TextView) this.getView().findViewById(R.id.attendees_num);
			if (getEvent().getAttendees() != null) {
				tv.setText(getEvent().getAttendees() + " " + getString(R.string.attendees_extended));
			} else {
				tv.setText("0 " + getString(R.string.attendees_extended));
			}
		}
	}

	/*
	 * private boolean hasMultimediaAttached() { return true; }
	 */

	private void updateRating() {
		if (this.getView() != null) {
			RatingBar rating = (RatingBar) getView().findViewById(R.id.event_rating);
			if (mEvent.getCommunityData() != null) {
				CommunityData cd = mEvent.getCommunityData();

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

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		// CAN DELETE ONLY OWN OBJECTS
		if (DTHelper.isOwnedObject(getEvent())) {
			getSherlockActivity().getSupportMenuInflater().inflate(R.menu.gripmenu, menu);
			SubMenu submenu = menu.getItem(0).getSubMenu();
			submenu.clear();
			submenu.add(Menu.CATEGORY_SYSTEM, R.id.submenu_edit, Menu.NONE, R.string.edit);
			submenu.add(Menu.CATEGORY_SYSTEM, R.id.submenu_delete, Menu.NONE, R.string.delete);
		}

		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.submenu_show_related_poi) {
			FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager()
					.beginTransaction();
			PoiDetailsFragment fragment = new PoiDetailsFragment();
			Bundle args = new Bundle();
			args.putSerializable(PoiDetailsFragment.ARG_POI_ID, getPOI().getId());
			fragment.setArguments(args);
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			// fragmentTransaction.detach(this);
			fragmentTransaction.replace(R.id.fragment_container, fragment, "events");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
			return true;
			// } else if (item.getItemId() == R.id.submenu_get_dir) {
			// bringMeThere(getEvent());
			// return true;
			// } else if (item.getItemId() == R.id.submenu_see_on_map) {
			// ArrayList<BaseDTObject> list = new ArrayList<BaseDTObject>();
			// getEvent().setLocation(poi.getLocation());
			// list.add(getEvent());
			// MapManager.switchToMapView(list, this);
			// return true;
			// } else if (item.getItemId() == R.id.submenu_follow) {
			// FollowEntityObject obj = new
			// FollowEntityObject(getEvent().getEntityId(),
			// getEvent().getTitle(),
			// DTConstants.ENTITY_TYPE_EVENT);
			// if (mFollowByIntent) {
			// FollowHelper.follow(this, obj, 3000);
			// } else {
			// SCAsyncTask<Object, Void, Topic> followTask = new
			// SCAsyncTask<Object, Void, Topic>(getSherlockActivity(),
			// new FollowAsyncTaskProcessor(getSherlockActivity()));
			// followTask.execute(getSherlockActivity().getApplicationContext(),
			// DTParamsHelper.getAppToken(),
			// DTHelper.getAuthToken(), obj);
			// }
			// return true;
			// } else if (item.getItemId() == R.id.submenu_unfollow) {
			// BaseDTObject obj;
			// try {
			// obj = DTHelper.findEventByEntityId(getEvent().getEntityId());
			// if (obj != null) {
			// SCAsyncTask<BaseDTObject, Void, BaseDTObject> unfollowTask = new
			// SCAsyncTask<BaseDTObject, Void, BaseDTObject>(
			// getSherlockActivity(), new
			// UnfollowAsyncTaskProcessor(getSherlockActivity()));
			// unfollowTask.execute(obj);
			//
			// }
			// } catch (Exception e) {
			// Log.e(EventDetailsFragment.class.getName(),
			// String.format("Error unfollowing event %s",
			// getEvent().getEntityId()));
			// }
			// return true;
			// } else if (item.getItemId() == R.id.submenu_rate) {
			// if (new
			// AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
			// // show dialog box
			// UserRegistration.upgradeuser(getSherlockActivity());
			// return false;
			// } else {
			// ratingDialog();
			// return true;
			// }
			// } else if (item.getItemId() == R.id.submenu_attend) {
			// if (new
			// AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
			// // show dialog box
			// UserRegistration.upgradeuser(getSherlockActivity());
			// return false;
			// } else {
			// new SCAsyncTask<Boolean, Void, EventObject>(getActivity(), new
			// AttendProcessor(getActivity()))
			// .execute(getEvent().getAttending() == null ||
			// getEvent().getAttending().isEmpty());
			// return true;
			// }
		} else if (item.getItemId() == R.id.submenu_edit || item.getItemId() == R.id.submenu_tag) {
			// if (new
			// AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
			// // show dialog box
			// UserRegistration.upgradeuser(getSherlockActivity());
			// return false;
			// } else
			{
				FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager()
						.beginTransaction();
				Fragment fragment = new CreateEventFragment();
				Bundle args = new Bundle();
				// args.putSerializable(CreateEventFragment.ARG_EVENT,
				// getEvent());
				args.putString(CreateEventFragment.ARG_EVENT, getEvent().getId());
				fragment.setArguments(args);
				fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				// fragmentTransaction.detach(this);
				fragmentTransaction.replace(R.id.fragment_container, fragment, "events");
				fragmentTransaction.addToBackStack(fragment.getTag());
				fragmentTransaction.commit();
				resetEvent();
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
				AlertDialog.Builder mAlert = new AlertDialog.Builder(getSherlockActivity());
				mAlert.setMessage(getText(R.string.alert_message));
				mAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						new SCAsyncTask<LocalEventObject, Void, Boolean>(getActivity(), new EventDeleteProcessor(
								getActivity())).execute(getEvent());
					}
				});
				mAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						// getSherlockActivity().onBackPressed();

					}
				});

				AlertDialog alert = mAlert.create();

				alert.show();
				// new SCAsyncTask<LocalEventObject, Void,
				// Boolean>(getActivity(),
				// new EventDeleteProcessor(getActivity()))
				// .execute(getEvent());
				return true;
			}
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	private void resetEvent() {
		mEvent = null;
		mEventId = null;
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
				getEvent().getCommunityData().getFollowing().clear();
				mCanceledFollow = true;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onResume() {
		// getSherlockActivity().invalidateOptionsMenu();
		super.onResume();
	}

	private void bringMeThere(LocalEventObject eventObject) {
		/* check event Object */
		callBringMeThere();
	}

	private void ratingDialog() {
		float rating = (getEvent() != null && getEvent().getCommunityData() != null && getEvent().getCommunityData()
				.getAverageRating() > 0) ? getEvent().getCommunityData().getAverageRating() : 2.5f;
		RatingHelper.ratingDialog(getActivity(), rating, new RatingProcessor(getActivity()),
				R.string.rating_event_dialog_title);
	}

	/**
	 * 
	 */
	protected void callBringMeThere() {
		Address to = Utils.getPOIasGoogleAddress(getPOI());
		Address from = null;
		GeoPoint mylocation = MapManager.requestMyLocation(getActivity());
		if (mylocation != null) {
			from = new Address(Locale.getDefault());
			from.setLatitude(mylocation.getLatitudeE6() / 1E6);
			from.setLongitude(mylocation.getLongitudeE6() / 1E6);
		}
		NavigationHelper.bringMeThere(getActivity(), from, to);
	}

	private class RatingProcessor extends AbstractAsyncTaskProcessor<Integer, Integer> implements RatingHandler {

		public RatingProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public Integer performAction(Integer... params) throws SecurityException, Exception {
			return DTHelper.rate(getEvent(), params[0]);
		}

		@Override
		public void handleResult(Integer result) {
			mEvent = null;
			getEvent();
			updateRating();
			if (getSherlockActivity() != null)
				Toast.makeText(getSherlockActivity(), R.string.rating_success, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onRatingChanged(float rating) {
			new SCAsyncTask<Integer, Void, Integer>(getActivity(), this).execute((int) rating);
		}
	}

	private class EventDeleteProcessor extends AbstractAsyncTaskProcessor<LocalEventObject, Boolean> {
		public EventDeleteProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public Boolean performAction(LocalEventObject... params) throws SecurityException, Exception {
			return DTHelper.deleteEvent(params[0]);
		}

		@Override
		public void handleResult(Boolean result) {
			if (result) {
				getSherlockActivity().getSupportFragmentManager().popBackStack();
			} else {
				Toast.makeText(getActivity(), getActivity().getString(R.string.app_failure_cannot_delete),
						Toast.LENGTH_LONG).show();
			}
		}

	}

	private class AttendProcessor extends AbstractAsyncTaskProcessor<Boolean, LocalEventObject> {

		private CompoundButton buttonView;
		private Boolean attend;

		public AttendProcessor(Activity activity, CompoundButton buttonView) {
			super(activity);
			this.buttonView = buttonView;
		}

		@Override
		public LocalEventObject performAction(Boolean... params) throws SecurityException, Exception {
			attend = params[0];
			if (attend) {
				return DTHelper.attend(getEvent());
			}
			return DTHelper.notAttend(getEvent());
		}

		@Override
		public void handleResult(LocalEventObject result) {
			mEvent = result;
			updateAttending();
			// getSherlockActivity().invalidateOptionsMenu();
			// LocalEventObject event = getEvent();
			if (getSherlockActivity() != null)
				if (mEvent.getAttending() == null || mEvent.getAttending().isEmpty()) {
					Toast.makeText(getSherlockActivity(), R.string.not_attend_success, Toast.LENGTH_SHORT).show();
					buttonView.setBackgroundResource(R.drawable.ic_btn_monitor_off);
				} else {
					Toast.makeText(getSherlockActivity(), R.string.attend_success, Toast.LENGTH_SHORT).show();
					buttonView.setBackgroundResource(R.drawable.ic_btn_monitor_on);
				}
		}

	}

	class FollowAsyncTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			try {
				DTHelper.follow(mEvent);
			} catch (Exception e) {
				Log.e(FollowAsyncTask.class.getName(), String.format("Exception following event %s", mEventId));
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
}
