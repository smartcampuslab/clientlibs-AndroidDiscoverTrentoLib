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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

import eu.trentorise.smartcampus.ac.UserRegistration;
import eu.trentorise.smartcampus.ac.authenticator.AMSCAccessProvider;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.common.SCAsyncTask.SCAsyncTaskProcessor;
import eu.trentorise.smartcampus.android.common.follow.FollowEntityObject;
import eu.trentorise.smartcampus.android.common.follow.FollowHelper;
import eu.trentorise.smartcampus.android.common.follow.model.Topic;
import eu.trentorise.smartcampus.android.common.listing.AbstractLstingFragment;
import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog;
import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog.TagProvider;
import eu.trentorise.smartcampus.android.feedback.utils.FeedbackFragmentInflater;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.dt.custom.CategoryHelper;
import eu.trentorise.smartcampus.dt.custom.CategoryHelper.CategoryDescriptor;
import eu.trentorise.smartcampus.dt.custom.EventAdapter;
import eu.trentorise.smartcampus.dt.custom.EventPlaceholder;
import eu.trentorise.smartcampus.dt.custom.SearchHelper;
import eu.trentorise.smartcampus.dt.custom.StoryAdapter;
import eu.trentorise.smartcampus.dt.custom.data.Constants;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
import eu.trentorise.smartcampus.dt.custom.data.FollowAsyncTaskProcessor;
import eu.trentorise.smartcampus.dt.custom.map.MapManager;
import eu.trentorise.smartcampus.dt.model.BaseDTObject;
import eu.trentorise.smartcampus.dt.model.Concept;
import eu.trentorise.smartcampus.dt.model.DTConstants;
import eu.trentorise.smartcampus.dt.model.EventObject;
import eu.trentorise.smartcampus.dt.notifications.NotificationsSherlockFragmentDT;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

// to be used for event listing both in categories and in My Events
public class EventsListingFragment extends AbstractLstingFragment<EventObject> implements TagProvider {
	private ListView list;
	private Context context;

	public static final String ARG_CATEGORY = "event_category";
	public static final String ARG_POI = "event_poiId";
	public static final String ARG_POI_NAME = "event_poi_title";
	public static final String ARG_QUERY = "event_query";
	public static final String ARG_QUERY_TODAY = "event_query_today";
	public static final String ARG_MY = "event_my";
	public static final String ARG_CATEGORY_SEARCH = "category_search";
	public static final String ARG_MY_EVENTS_SEARCH = "my_events_search";
	public static final String ARG_LIST = "event_list";

	private String category;
	private EventAdapter eventsAdapter;
	private boolean mFollowByIntent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.context = this.getSherlockActivity();
		setHasOptionsMenu(true);
		setFollowByIntent();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.eventslist, container, false);
	}

	private void setFollowByIntent() {
		try {
			ApplicationInfo ai = getSherlockActivity().getPackageManager().getApplicationInfo(
					getSherlockActivity().getPackageName(), PackageManager.GET_META_DATA);
			Bundle aBundle = ai.metaData;
			mFollowByIntent = aBundle.getBoolean("follow-by-intent");
		} catch (NameNotFoundException e) {
			mFollowByIntent = false;
			Log.e(EventsListingFragment.class.getName(), "you should set the follow-by-intent metadata in app manifest");
		}

	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		/*
		 * menu.clear(); MenuItem item = menu.add(Menu.CATEGORY_SYSTEM,
		 * R.id.map_view, Menu.NONE, R.string.map_view);
		 * item.setIcon(R.drawable.ic_map);
		 * item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		 */
		menu.clear();
		getSherlockActivity().getSupportMenuInflater().inflate(R.menu.gripmenu, menu);

		SubMenu submenu = menu.getItem(0).getSubMenu();
		submenu.clear();
		submenu.add(Menu.CATEGORY_SYSTEM, R.id.map_view, Menu.NONE, R.string.map_view);
		if (getArguments() == null || !getArguments().containsKey(ARG_POI) && !getArguments().containsKey(ARG_LIST)
				&& !getArguments().containsKey(ARG_QUERY_TODAY) && !getArguments().containsKey(ARG_MY)
				&& !getArguments().containsKey(ARG_QUERY)) {
			SearchHelper.createSearchMenu(submenu, getActivity(), new SearchHelper.OnSearchListener() {
				@Override
				public void onSearch(String query) {
					FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager()
							.beginTransaction();
					EventsListingFragment fragment = new EventsListingFragment();
					Bundle args = new Bundle();
					args.putString(EventsListingFragment.ARG_QUERY, query);
					String category = (getArguments() != null) ? getArguments().getString(ARG_CATEGORY) : null;
					args.putString(EventsListingFragment.ARG_CATEGORY_SEARCH, category);
					fragment.setArguments(args);
					fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					fragmentTransaction.replace(android.R.id.content, fragment, "events");
					fragmentTransaction.addToBackStack(fragment.getTag());
					fragmentTransaction.commit();
				}
			});
		} else if (getArguments() == null || !getArguments().containsKey(ARG_POI) && !getArguments().containsKey(ARG_LIST)
				&& !getArguments().containsKey(ARG_QUERY_TODAY) && !getArguments().containsKey(ARG_QUERY)
				&& !getArguments().containsKey(ARG_MY_EVENTS_SEARCH) && !getArguments().containsKey(ARG_CATEGORY_SEARCH)) {
			SearchHelper.createSearchMenu(submenu, getActivity(), new SearchHelper.OnSearchListener() {
				@Override
				public void onSearch(String query) {
					FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager()
							.beginTransaction();
					EventsListingFragment fragment = new EventsListingFragment();
					Bundle args = new Bundle();
					args.putString(EventsListingFragment.ARG_QUERY, query);
					args.putBoolean(EventsListingFragment.ARG_MY_EVENTS_SEARCH, true);
					fragment.setArguments(args);
					fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					fragmentTransaction.replace(android.R.id.content, fragment, "events");
					fragmentTransaction.addToBackStack(fragment.getTag());
					fragmentTransaction.commit();
				}
			});
		}
		if (category == null)
			category = (getArguments() != null) ? getArguments().getString(ARG_CATEGORY) : null;
		if (category != null) {
			String addString = getString(R.string.add)
					+ " "
					+ getString(CategoryHelper.getCategoryDescriptorByCategory(CategoryHelper.CATEGORY_TYPE_EVENTS, category).description)
					+ " " + getString(R.string.event);
			if (Locale.getDefault().equals(Locale.ITALY))
				addString = getString(R.string.add)
						+ " "
						+ getString(R.string.event)
						+ " su "
						+ getString(CategoryHelper.getCategoryDescriptorByCategory(CategoryHelper.CATEGORY_TYPE_EVENTS,
								category).description);

			submenu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_addevent, Menu.NONE, addString);
		}

		NotificationsSherlockFragmentDT.onPrepareOptionsMenuNotifications(menu);

		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		NotificationsSherlockFragmentDT.onOptionsItemSelectedNotifications(getSherlockActivity(), item);

		if (item.getItemId() == R.id.map_view) {
			ArrayList<BaseDTObject> target = new ArrayList<BaseDTObject>();
			if (list != null) {
				for (int i = 0; i < list.getAdapter().getCount(); i++) {
					BaseDTObject o = (BaseDTObject) list.getAdapter().getItem(i);
					if (o.getLocation() != null && o.getLocation()[0] != 0 && o.getLocation()[1] != 0) {
						target.add(o);
					}
				}
			}
			MapManager.switchToMapView(target, this);
			return true;
		} else if (item.getItemId() == R.id.menu_item_addevent) {
			if (new AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
				// show dialog box
				UserRegistration.upgradeuser(getSherlockActivity());
				return false;
			} else {
				FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager().beginTransaction();
				Fragment fragment = new CreateEventFragment();
				Bundle args = new Bundle();
				args.putString(ARG_CATEGORY, category);
				fragment.setArguments(args);
				fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				// fragmentTransaction.detach(this);
				fragmentTransaction.replace(android.R.id.content, fragment, "events");
				fragmentTransaction.addToBackStack(fragment.getTag());
				fragmentTransaction.commit();
				return true;
			}
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onStart() {
		Bundle bundle = this.getArguments();

		list = (ListView) getSherlockActivity().findViewById(R.id.events_list);
		// new SCAsyncTask<Bundle, Void, EventObject[]>(getActivity(),
		// new EventLoader(getActivity())).execute(bundle);

		eventsAdapter = new EventAdapter(context, R.layout.events_row);
		setAdapter(eventsAdapter);

		// set title
		TextView title = (TextView) getView().findViewById(R.id.list_title);
		String category = bundle.getString(ARG_CATEGORY);
		CategoryDescriptor catDescriptor = CategoryHelper.getCategoryDescriptorByCategory("events", category);
		String categoryString = (catDescriptor != null) ? context.getResources().getString(catDescriptor.description) : null;

		if (bundle != null && bundle.containsKey(ARG_CATEGORY)) {
			title.setText(categoryString);
		} else if (bundle != null && bundle.containsKey(ARG_MY)) {
			title.setText(R.string.myevents);
		} else if (bundle != null && bundle.containsKey(ARG_POI_NAME)) {
			String poiName = bundle.getString(ARG_POI_NAME);
			title.setText(getResources().getString(R.string.eventlist_at_place) + " " + poiName);
		} else if (bundle != null && bundle.containsKey(ARG_QUERY)) {
			String query = bundle.getString(ARG_QUERY);
			title.setText(context.getResources().getString(R.string.search_for) + " '" + query + "'");
			if (bundle.containsKey(ARG_CATEGORY_SEARCH)) {
				category = bundle.getString(ARG_CATEGORY_SEARCH);
				if (category != null)
					title.append(context.getResources().getString(R.string.search_in_category) + " " + category);
			}
		} else if (bundle != null && bundle.containsKey(ARG_QUERY_TODAY)) {
			title.setText(context.getResources().getString(R.string.search_today_events));
		}

		// close items menus if open
		((View) list.getParent()).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				hideListItemsMenu(v);
			}
		});
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				hideListItemsMenu(view);
			}
		});

		// open items menu for that entry
		list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				ViewSwitcher vs = (ViewSwitcher) view.findViewById(R.id.event_viewswitecher);
				setupOptionsListeners(vs, position);
				vs.showNext();
				return true;
			}
		});
		FeedbackFragmentInflater.inflateHandleButton(getSherlockActivity(), getView());
		super.onStart();

	}

	private void hideListItemsMenu(View v) {
		boolean toBeHidden = false;
		for (int index = 0; index < list.getChildCount(); index++) {
			View view = list.getChildAt(index);
			if (view != null && view instanceof LinearLayout && ((LinearLayout) view).getChildCount() == 2)
				view = ((LinearLayout) view).getChildAt(1);
			if (view instanceof ViewSwitcher && ((ViewSwitcher) view).getDisplayedChild() == 1) {
				((ViewSwitcher) view).showPrevious();
				toBeHidden = true;
			}
		}
		if (!toBeHidden && v != null && v.getTag() != null) {
			// no items needed to be flipped, fill and open details page
			FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager().beginTransaction();
			EventDetailsFragment fragment = new EventDetailsFragment();

			Bundle args = new Bundle();
			args.putSerializable(EventDetailsFragment.ARG_EVENT_OBJECT, ((EventPlaceholder) v.getTag()).event);
			fragment.setArguments(args);

			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			// fragmentTransaction.detach(this);
			fragmentTransaction.replace(android.R.id.content, fragment, "events");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();

		}
	}

	protected void setupOptionsListeners(final ViewSwitcher vs, final int position) {
		final EventObject event = ((EventPlaceholder) ((View) vs.getParent()).getTag()).event;
		ImageButton b = (ImageButton) vs.findViewById(R.id.delete_btn);
		if (DTHelper.isOwnedObject(event)) {
			b.setVisibility(View.VISIBLE);
			b.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (new AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
						// show dialog box
						UserRegistration.upgradeuser(getSherlockActivity());
					} else {
						new SCAsyncTask<EventObject, Void, Boolean>(getActivity(), new EventDeleteProcessor(getActivity()))
								.execute(event);
						hideListItemsMenu(vs);
					}
				}
			});
		} else {
			b.setVisibility(View.GONE);
		}
		b = (ImageButton) vs.findViewById(R.id.edit_btn);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (new AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
					// show dialog box
					UserRegistration.upgradeuser(getSherlockActivity());
				} else {
					FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager()
							.beginTransaction();
					Fragment fragment = new CreateEventFragment();
					Bundle args = new Bundle();
					args.putSerializable(CreateEventFragment.ARG_EVENT, event);
					fragment.setArguments(args);
					fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					// fragmentTransaction.detach(this);
					fragmentTransaction.replace(android.R.id.content, fragment, "events");
					fragmentTransaction.addToBackStack(fragment.getTag());
					fragmentTransaction.commit();
				}
			}
		});
		// b = (ImageButton) vs.findViewById(R.id.share_btn);
		// b.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// Toast.makeText(getActivity(), "Event shared.",
		// Toast.LENGTH_SHORT).show();
		//
		// }
		// });
		b = (ImageButton) vs.findViewById(R.id.tag_btn);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (new AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
					// show dialog box
					UserRegistration.upgradeuser(getSherlockActivity());
				} else {
					TaggingDialog taggingDialog = new TaggingDialog(getActivity(), new TaggingDialog.OnTagsSelectedListener() {

						@SuppressWarnings("unchecked")
						@Override
						public void onTagsSelected(Collection<SemanticSuggestion> suggestions) {
							new TaggingAsyncTask(event).execute(Concept.convertSS(suggestions));
						}
					}, EventsListingFragment.this, Concept.convertToSS(event.getCommunityData().getTags()));
					taggingDialog.show();
				}
			}
		});
		b = (ImageButton) vs.findViewById(R.id.follow_btn);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				FollowEntityObject obj = new FollowEntityObject(event.getEntityId(), event.getTitle(),
						DTConstants.ENTITY_TYPE_EVENT);
				if (mFollowByIntent) {
					FollowHelper.follow(getSherlockActivity(), obj);
				} else {
					SCAsyncTask<Object, Void, Topic> followTask = new SCAsyncTask<Object, Void, Topic>(getSherlockActivity(),
							new FollowAsyncTaskProcessor(getSherlockActivity()));
					followTask.execute(getSherlockActivity().getApplicationContext(), Constants.APP_TOKEN,
							DTHelper.getAuthToken(), obj);
				}
			}
		});
	}

	private List<EventObject> getEvents(AbstractLstingFragment.ListingRequest... params) {
		try {
			Collection<EventObject> result = null;
			Bundle bundle = getArguments();
			if (bundle == null) {
				return Collections.emptyList();
			} else if (bundle.containsKey(ARG_CATEGORY)) {
				result = DTHelper.getEventsByCategories(params[0].position, params[0].size, bundle.getString(ARG_CATEGORY));
			} else if (bundle.containsKey(ARG_POI)) {
				result = DTHelper.getEventsByPOI(params[0].position, params[0].size, bundle.getString(ARG_POI));
			} else if (bundle.containsKey(ARG_MY)) {
				result = DTHelper.getMyEvents(params[0].position, params[0].size);
			} else if (bundle.containsKey(ARG_QUERY)) {
				if (bundle.containsKey(ARG_CATEGORY_SEARCH)) {
					HashSet<String> set = new HashSet<String>(1);
					set.add(bundle.getString(ARG_CATEGORY_SEARCH));
					result = DTHelper.searchEventsByCategory(params[0].position, params[0].size, bundle.getString(ARG_QUERY),
							bundle.getString(ARG_CATEGORY_SEARCH));
				} else if (bundle.containsKey(ARG_MY_EVENTS_SEARCH)) {
					HashSet<String> set = new HashSet<String>(1);
					set.add(bundle.getString(ARG_CATEGORY_SEARCH));
					result = DTHelper.searchMyEvents(params[0].position, params[0].size, bundle.getString(ARG_QUERY));
				} else
					result = DTHelper.searchEvents(params[0].position, params[0].size, bundle.getString(ARG_QUERY));
			} else if (bundle.containsKey(ARG_QUERY_TODAY)) {
				result = DTHelper.searchTodayEvents(params[0].position, params[0].size, bundle.getString(ARG_QUERY));
			} else if (bundle.containsKey(ARG_LIST)) {
				result = (List<EventObject>) bundle.get(ARG_LIST);
			} else {
				return Collections.emptyList();
			}

			List<EventObject> sorted = new ArrayList<EventObject>(result);
			for (EventObject eventObject : sorted) {
				if (eventObject.getPoiId() != null) {
					eventObject.assignPoi(DTHelper.findPOIById(eventObject.getPoiId()));
				}
			}
			// Collections.sort(sorted, new Comparator<EventObject>() {
			// @Override
			// public int compare(EventObject lhs, EventObject rhs) {
			// return lhs.getFromTime().compareTo(rhs.getFromTime());
			// }
			//
			// });

			return sorted;
		} catch (Exception e) {
			Log.e(EventsListingFragment.class.getName(), e.getMessage());
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	@Override
	public List<SemanticSuggestion> getTags(CharSequence text) {
		try {
			return DTHelper.getSuggestions(text);
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	private class TaggingAsyncTask extends SCAsyncTask<List<Concept>, Void, Void> {

		public TaggingAsyncTask(final EventObject p) {
			super(getSherlockActivity(), new AbstractAsyncTaskProcessor<List<Concept>, Void>(getSherlockActivity()) {
				@Override
				public Void performAction(List<Concept>... params) throws SecurityException, Exception {
					p.getCommunityData().setTags(params[0]);
					DTHelper.saveEvent(p);
					return null;
				}

				@Override
				public void handleResult(Void result) {
					Toast.makeText(getSherlockActivity(), getString(R.string.tags_successfully_added), Toast.LENGTH_SHORT)
							.show();
				}
			});
		}
	}

	private class EventLoader extends AbstractAsyncTaskProcessor<AbstractLstingFragment.ListingRequest, List<EventObject>> {

		public EventLoader(Activity activity) {
			super(activity);
		}

		// fetches the events
		@Override
		public List<EventObject> performAction(AbstractLstingFragment.ListingRequest... params) throws SecurityException,
				Exception {
			return getEvents(params);
		}

		// populates the listview with the events
		@Override
		public void handleResult(List<EventObject> result) {
			// Bundle bundle = getArguments();
			// if (bundle != null && bundle.containsKey(ARG_CATEGORY) && (result
			// == null || result.size() == 0) && getListView().getCount() == 0)
			// {
			// Toast.makeText(getActivity(), getString(R.string.noevents),
			// Toast.LENGTH_LONG).show();
			// }
			// } else
			// list.setAdapter(new EventAdapter(context, R.layout.events_row,
			// result));
			updateList(result == null || result.isEmpty());
		}
	}

	private class EventDeleteProcessor extends AbstractAsyncTaskProcessor<EventObject, Boolean> {
		private EventObject object = null;

		public EventDeleteProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public Boolean performAction(EventObject... params) throws SecurityException, Exception {
			object = params[0];
			return DTHelper.deleteEvent(params[0]);
		}

		@Override
		public void handleResult(Boolean result) {
			if (result) {
				((EventAdapter) list.getAdapter()).remove(object);
				((EventAdapter) list.getAdapter()).notifyDataSetChanged();
				updateList(((EventAdapter) list.getAdapter()).isEmpty());
			} else {
				Toast.makeText(getActivity(), getActivity().getString(R.string.app_failure_cannot_delete), Toast.LENGTH_LONG)
						.show();
			}
		}

	}

	@Override
	protected SCAsyncTaskProcessor<AbstractLstingFragment.ListingRequest, List<EventObject>> getLoader() {
		return new EventLoader(getActivity());
	}

	@Override
	protected ListView getListView() {
		return list;
	}

	private void updateList(boolean empty) {
		eu.trentorise.smartcampus.dt.custom.ViewHelper.removeEmptyListView((LinearLayout) getView().findViewById(
				R.id.eventlistcontainer));
		if (empty) {
			eu.trentorise.smartcampus.dt.custom.ViewHelper.addEmptyListView((LinearLayout) getView().findViewById(
					R.id.eventlistcontainer));
		}
		hideListItemsMenu(null);
	}

}
