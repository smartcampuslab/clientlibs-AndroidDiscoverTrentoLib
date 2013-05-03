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
package eu.trentorise.smartcampus.dt.fragments.pois;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

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
import eu.trentorise.smartcampus.dt.custom.PoiAdapter;
import eu.trentorise.smartcampus.dt.custom.PoiPlaceholder;
import eu.trentorise.smartcampus.dt.custom.SearchHelper;
import eu.trentorise.smartcampus.dt.custom.data.Constants;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
import eu.trentorise.smartcampus.dt.custom.data.FollowAsyncTaskProcessor;
import eu.trentorise.smartcampus.dt.custom.map.MapManager;
import eu.trentorise.smartcampus.dt.fragments.events.EventDetailsFragment;
import eu.trentorise.smartcampus.dt.fragments.search.SearchFragment;
import eu.trentorise.smartcampus.dt.fragments.search.WhenForSearch;
import eu.trentorise.smartcampus.dt.fragments.search.WhereForSearch;
import eu.trentorise.smartcampus.dt.model.BaseDTObject;
import eu.trentorise.smartcampus.dt.model.Concept;
import eu.trentorise.smartcampus.dt.model.DTConstants;
import eu.trentorise.smartcampus.dt.model.EventObject;
import eu.trentorise.smartcampus.dt.model.POIObject;
import eu.trentorise.smartcampus.dt.notifications.NotificationsSherlockFragmentDT;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class PoisListingFragment extends AbstractLstingFragment<POIObject> implements TagProvider {


	private ListView list;
	private Context context;
	private String category;
	private boolean mFollowByIntent;
	private PoiAdapter poiAdapter ;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.context = this.getSherlockActivity();
		setHasOptionsMenu(true);
		setFollowByIntent();
	}


	@Override
	public void onActivityCreated(Bundle arg0) {
	super.onActivityCreated(arg0);
	list = (ListView) getSherlockActivity().findViewById(R.id.pois_list);
	if (poiAdapter == null){
		poiAdapter = new PoiAdapter(context, R.layout.pois_row);
	}
	setAdapter(poiAdapter);
	
}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			
		return inflater.inflate(R.layout.poislist, container, false); 
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
		if (getArguments() == null || !getArguments().containsKey(SearchFragment.ARG_LIST) && !getArguments().containsKey(SearchFragment.ARG_QUERY)) {
//			SearchHelper.createSearchMenu(submenu, getActivity(), new SearchHelper.OnSearchListener() {
//				@Override
//				public void onSearch(String query) {
//					FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager()
//							.beginTransaction();
//					PoisListingFragment fragment = new PoisListingFragment();
//					Bundle args = new Bundle();
//					args.putString(SearchFragment.ARG_QUERY, query);
//					String category = (getArguments() != null) ? getArguments().getString(SearchFragment.ARG_CATEGORY) : null;
//					args.putString(SearchFragment.ARG_CATEGORY_SEARCH, category);
//					fragment.setArguments(args);
//					fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
//					fragmentTransaction.replace(android.R.id.content, fragment, "pois");
//					fragmentTransaction.addToBackStack(fragment.getTag());
//					fragmentTransaction.commit();
//				}
//			});
			submenu.add(Menu.CATEGORY_SYSTEM, R.id.search, Menu.NONE, R.string.search_txt);
		}
		if (category == null)
			category = (getArguments() != null) ? getArguments().getString(SearchFragment.ARG_CATEGORY) : null;
		if (category != null){
			String addString = getString(R.string.add)
					+ " "
					+ getString(CategoryHelper.getCategoryDescriptorByCategory(CategoryHelper.CATEGORY_TYPE_POIS, category).description)
					+ " " + getString(R.string.place);
			if (Locale.getDefault().equals(Locale.ITALY))
				addString = getString(R.string.add)
						+ " "
						+ getString(R.string.place)
						+ " su "
						+ getString(CategoryHelper.getCategoryDescriptorByCategory(CategoryHelper.CATEGORY_TYPE_POIS,
								category).description);

			submenu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_addpoi, Menu.NONE, addString);
		}
		
		NotificationsSherlockFragmentDT.onPrepareOptionsMenuNotifications(menu);
		
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		NotificationsSherlockFragmentDT.onOptionsItemSelectedNotifications(getSherlockActivity(), item);
		
		if (item.getItemId() == R.id.map_view) {
			category = (getArguments() != null) ? getArguments().getString(SearchFragment.ARG_CATEGORY) : null;
			if (category != null) {
				MapManager.switchToMapView(category, this);
			} else {
				ArrayList<BaseDTObject> target = new ArrayList<BaseDTObject>();
				for (int i = 0; i < list.getAdapter().getCount(); i++) {
					BaseDTObject o = (BaseDTObject) list.getAdapter().getItem(i);
					target.add(o);
				}
				MapManager.switchToMapView(target, this);
			}
			return true;
		} else if (item.getItemId() == R.id.menu_item_addpoi) {
			if (new AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
				// show dialog box
				UserRegistration.upgradeuser(getSherlockActivity());
				return false;
			} else {
			FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager().beginTransaction();
			Fragment fragment = new CreatePoiFragment();
			Bundle args = new Bundle();
			args.putString(SearchFragment.ARG_CATEGORY, category);
			fragment.setArguments(args);
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			// fragmentTransaction.detach(this);
			fragmentTransaction.replace(android.R.id.content, fragment, "pois");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
			return true;
			}
		}else if (item.getItemId() == R.id.search) {
			FragmentTransaction fragmentTransaction;
			Fragment fragment;
			fragmentTransaction = getSherlockActivity().getSupportFragmentManager().beginTransaction();
			fragment = new SearchFragment();
			Bundle args = new Bundle();
			args.putString(SearchFragment.ARG_CATEGORY, category);
			args.putString(CategoryHelper.CATEGORY_TYPE_POIS, CategoryHelper.CATEGORY_TYPE_POIS);
			if (getArguments() != null && getArguments().containsKey(SearchFragment.ARG_MY) && getArguments().getBoolean(SearchFragment.ARG_MY))
				args.putBoolean(SearchFragment.ARG_MY, getArguments().getBoolean(SearchFragment.ARG_MY));
			fragment.setArguments(args);
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(android.R.id.content, fragment, "pois");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
			/* add category to bundle */
			return true;

		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	private void setFollowByIntent() {
		try {
			ApplicationInfo ai = getSherlockActivity().getPackageManager().getApplicationInfo(getSherlockActivity().getPackageName(),
					PackageManager.GET_META_DATA);
			Bundle aBundle=ai.metaData;
			mFollowByIntent=aBundle.getBoolean("follow-by-intent");
		} catch (NameNotFoundException e) {
			mFollowByIntent = false;
			Log.e(PoisListingFragment.class.getName(), "you should set the follow-by-intent metadata in app manifest");
		}

	}

	@Override
	public void onStart() {
		Bundle bundle = this.getArguments();
		String category = (bundle != null) ? bundle.getString(SearchFragment.ARG_CATEGORY) : null;
		CategoryDescriptor catDescriptor = CategoryHelper.getCategoryDescriptorByCategory("pois", category);
		String categoryString = (catDescriptor != null) ? context.getResources().getString(catDescriptor.description) : null;

		// set title
		TextView title = (TextView) getView().findViewById(R.id.list_title);
		if (categoryString != null) {
			title.setText(categoryString);
		} else if (bundle != null && bundle.containsKey(SearchFragment.ARG_QUERY)) {
			String query = bundle.getString(SearchFragment.ARG_QUERY);
			title.setText(context.getResources().getString(R.string.search_for) + "'" + query + "'");

			if (bundle.containsKey(SearchFragment.ARG_CATEGORY_SEARCH)) {
				category = bundle.getString(SearchFragment.ARG_CATEGORY_SEARCH);
				if (category != null)
					title.append(" " + context.getResources().getString(R.string.search_for) + " " + categoryString);
			}
		

			
		}
		if (bundle.containsKey(SearchFragment.ARG_WHERE_SEARCH)) {
			WhereForSearch where = bundle.getParcelable(SearchFragment.ARG_WHERE_SEARCH); 	
			if (where != null)
				title.append( " " +where.getDescription() + " " );
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
				ViewSwitcher vs = (ViewSwitcher) view;
				setupOptionsListeners(vs, position);
				vs.showNext();
				return true;
			}
		});
		FeedbackFragmentInflater.inflateHandleButton(getSherlockActivity(), getView());
		super.onStart();
	}

	protected void setupOptionsListeners(ViewSwitcher vs, final int position) {
		final POIObject poi = ((PoiPlaceholder) vs.getTag()).poi;

		ImageButton b = (ImageButton) vs.findViewById(R.id.poi_delete_btn);
		// CAN DELETE ONLY OWN OBJECTS
		if (DTHelper.isOwnedObject(poi)) {
			b.setVisibility(View.VISIBLE);
			b.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (new AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
						// show dialog box
						UserRegistration.upgradeuser(getSherlockActivity());
					} else {
						new SCAsyncTask<POIObject, Void, Boolean>(getActivity(), new POIDeleteProcessor(getActivity()))
								.execute(poi);
					}
				}
			});
		} else {
			b.setVisibility(View.GONE);
		}

		b = (ImageButton) vs.findViewById(R.id.poi_edit_btn);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (new AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
					// show dialog box
					UserRegistration.upgradeuser(getSherlockActivity());
				} else {
				FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager().beginTransaction();
				Fragment fragment = new CreatePoiFragment();
				Bundle args = new Bundle();
				args.putSerializable(CreatePoiFragment.ARG_POI, poi);
				fragment.setArguments(args);
				fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				// fragmentTransaction.detach(this);
				fragmentTransaction.replace(android.R.id.content, fragment, "pois");
				fragmentTransaction.addToBackStack(fragment.getTag());
				fragmentTransaction.commit();
				}
			}
		});
		// b = (ImageButton) vs.findViewById(R.id.poi_share_btn);
		// b.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// Toast.makeText(getSherlockActivity(), getString(R.string.toast_poi_shared),
		// Toast.LENGTH_SHORT).show();
		//
		// }
		// });
		b = (ImageButton) vs.findViewById(R.id.poi_tag_btn);
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
						new TaggingAsyncTask(poi).execute(Concept.convertSS(suggestions));
					}
				}, PoisListingFragment.this, Concept.convertToSS(poi.getCommunityData().getTags()));
				taggingDialog.show();
				}
			}
		});
		b = (ImageButton) vs.findViewById(R.id.poi_follow_btn);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				FollowEntityObject obj = new FollowEntityObject(poi.getEntityId(), poi.getTitle(), DTConstants.ENTITY_TYPE_POI);
				if (mFollowByIntent){
					FollowHelper.follow(getSherlockActivity(), obj);
				} else {
					SCAsyncTask<Object, Void, Topic> followTask = new SCAsyncTask<Object, Void, Topic>(getSherlockActivity(),
							new FollowAsyncTaskProcessor(getSherlockActivity()));
					followTask
							.execute(getSherlockActivity().getApplicationContext(), Constants.APP_TOKEN, DTHelper.getAuthToken(), obj);
					
				}
			}
		});
	}

	private void hideListItemsMenu(View v) {
		boolean toBeHidden = false;
		for (int index = 0; index < list.getChildCount(); index++) {
			View view = list.getChildAt(index);
			if (view instanceof ViewSwitcher && ((ViewSwitcher) view).getDisplayedChild() == 1) {
				((ViewSwitcher) view).showPrevious();
				toBeHidden = true;
			}
		}
		if (!toBeHidden && v != null && v.getTag() != null) {
			// no items needed to be flipped, fill and open details page
			FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager().beginTransaction();
			PoiDetailsFragment fragment = new PoiDetailsFragment();

			Bundle args = new Bundle();
			args.putSerializable(PoiDetailsFragment.ARG_POI, ((PoiPlaceholder) v.getTag()).poi);
			fragment.setArguments(args);

			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			// fragmentTransaction.detach(this);
			fragmentTransaction.replace(android.R.id.content, fragment, "pois");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
		}
	}

	private List<POIObject> getPOIs(AbstractLstingFragment.ListingRequest... params) {
		try {
			Collection<POIObject> result = null;
			Bundle bundle = getArguments();
			boolean my = false;
			if (bundle.getBoolean(SearchFragment.ARG_MY))
				my = true;
			String categories = bundle.getString(SearchFragment.ARG_CATEGORY);
//			if (bundle.containsKey(SearchFragment.ARG_CATEGORY)) {
//				result = DTHelper.getPOIByCategory(params[0].position, params[0].size, bundle.getString(SearchFragment.ARG_CATEGORY));
//			} else if (bundle.containsKey(SearchFragment.ARG_QUERY)) {
//				if (bundle.containsKey(SearchFragment.ARG_CATEGORY_SEARCH)) {
////					result = DTHelper.searchPOIsByCategory(params[0].position, params[0].size, bundle.getString(SearchFragment.ARG_QUERY),
////							bundle.getString(SearchFragment.ARG_CATEGORY_SEARCH));
//					result = DTHelper.searchInGeneral(position, lastSize, bundle.getString(SearchFragment.ARG_QUERY),
//							(WhereForSearch) bundle.getParcelable(SearchFragment.ARG_WHERE_SEARCH),
//							(WhenForSearch) bundle.getParcelable(SearchFragment.ARG_WHEN_SEARCH), my, POIObject.class, categories);
//				} else
//					result = DTHelper.searchPOIs(params[0].position, params[0].size, bundle.getString(SearchFragment.ARG_QUERY));
//			} else if (bundle.containsKey(SearchFragment.ARG_LIST)) {
//				result = (List<POIObject>) bundle.getSerializable(SearchFragment.ARG_LIST);
//			} else {
//				return Collections.emptyList();
//			}
			SortedMap<String, Integer> sort = new TreeMap<String, Integer>();
			sort.put("title", 1);
			if (bundle.containsKey(SearchFragment.ARG_CATEGORY) && (bundle.getString(SearchFragment.ARG_CATEGORY) != null)) {

				result = DTHelper.searchInGeneral(params[0].position, params[0].size, bundle.getString(SearchFragment.ARG_QUERY),
						(WhereForSearch) bundle.getParcelable(SearchFragment.ARG_WHERE_SEARCH),
						(WhenForSearch) bundle.getParcelable(SearchFragment.ARG_WHEN_SEARCH), my,POIObject.class, sort, categories);

			}  else if (bundle.containsKey(SearchFragment.ARG_MY) && (bundle.getBoolean(SearchFragment.ARG_MY))) {

				result = DTHelper.searchInGeneral(params[0].position, params[0].size, bundle.getString(SearchFragment.ARG_QUERY),
						(WhereForSearch) bundle.getParcelable(SearchFragment.ARG_WHERE_SEARCH),
						(WhenForSearch) bundle.getParcelable(SearchFragment.ARG_WHEN_SEARCH), my, POIObject.class,sort, categories);

			} else if (bundle.containsKey(SearchFragment.ARG_QUERY)) {


				result = DTHelper.searchInGeneral(params[0].position, params[0].size, bundle.getString(SearchFragment.ARG_QUERY),
						(WhereForSearch) bundle.getParcelable(SearchFragment.ARG_WHERE_SEARCH),
						(WhenForSearch) bundle.getParcelable(SearchFragment.ARG_WHEN_SEARCH), my,POIObject.class, sort, categories);

			}  else if (bundle.containsKey(SearchFragment.ARG_LIST)) {
				result = (List<POIObject>) bundle.get(SearchFragment.ARG_LIST);
			} else {
				return Collections.emptyList();
			}

			List<POIObject> sorted = new ArrayList<POIObject>(result);
			// Collections.sort(sorted, new Comparator<POIObject>() {
			// @Override
			// public int compare(POIObject lhs, POIObject rhs) {
			// return lhs.getTitle().compareTo(rhs.getTitle());
			// }
			//
			// });
			for (POIObject poi : sorted) {
				Log.e("lista", poi.getTitle());
			}
			return sorted;
		} catch (Exception e) {
			Log.e(PoisListingFragment.class.getName(), e.getMessage());
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	private class PoiLoader extends AbstractAsyncTaskProcessor<AbstractLstingFragment.ListingRequest, List<POIObject>> {

		public PoiLoader(Activity activity) {
			super(activity);
		}

		@Override
		public List<POIObject> performAction(AbstractLstingFragment.ListingRequest... params) throws SecurityException,
				Exception {
			return getPOIs(params);
		}

		@Override
		public void handleResult(List<POIObject> result) {
			// list.setAdapter(new PoiAdapter(context, R.layout.pois_row,
			// result));
			updateList(result == null || result.isEmpty());
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

		public TaggingAsyncTask(final POIObject p) {
			super(getSherlockActivity(), new AbstractAsyncTaskProcessor<List<Concept>, Void>(getSherlockActivity()) {
				@Override
				public Void performAction(List<Concept>... params) throws SecurityException, Exception {
					p.getCommunityData().setTags(params[0]);
					DTHelper.savePOI(p);
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

	private class POIDeleteProcessor extends AbstractAsyncTaskProcessor<POIObject, Boolean> {
		private POIObject object = null;

		public POIDeleteProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public Boolean performAction(POIObject... params) throws SecurityException, Exception {
			object = params[0];
			return DTHelper.deletePOI(params[0]);
		}

		@Override
		public void handleResult(Boolean result) {
			if (result) {
				((PoiAdapter) list.getAdapter()).remove(object);
				((PoiAdapter) list.getAdapter()).notifyDataSetChanged();
				updateList(((PoiAdapter) list.getAdapter()).isEmpty());
			} else {
				Toast.makeText(getSherlockActivity(), getString(R.string.app_failure_cannot_delete), Toast.LENGTH_LONG).show();
			}
		}

	}

	@Override
	protected SCAsyncTaskProcessor<AbstractLstingFragment.ListingRequest, List<POIObject>> getLoader() {
		return new PoiLoader(getActivity());
	}

	@Override
	protected ListView getListView() {
		return list;
	}

	private void updateList(boolean empty) {
		eu.trentorise.smartcampus.dt.custom.ViewHelper.removeEmptyListView((LinearLayout) getView().findViewById(
				R.id.poilistcontainer));
		if (empty) {
			eu.trentorise.smartcampus.dt.custom.ViewHelper.addEmptyListView((LinearLayout) getView().findViewById(
					R.id.poilistcontainer));
		}
		hideListItemsMenu(null);
	}

}
