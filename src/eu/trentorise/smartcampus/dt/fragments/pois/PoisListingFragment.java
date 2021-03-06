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
import java.util.SortedMap;
import java.util.TreeMap;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
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

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.common.SCAsyncTask.SCAsyncTaskProcessor;
import eu.trentorise.smartcampus.android.common.listing.AbstractLstingFragment;
import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog;
import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog.TagProvider;
import eu.trentorise.smartcampus.android.feedback.utils.FeedbackFragmentInflater;
import eu.trentorise.smartcampus.dt.DTParamsHelper;
import eu.trentorise.smartcampus.dt.DiscoverTrentoActivity;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.dt.custom.CategoryHelper;
import eu.trentorise.smartcampus.dt.custom.CategoryHelper.CategoryDescriptor;
import eu.trentorise.smartcampus.dt.custom.PoiAdapter;
import eu.trentorise.smartcampus.dt.custom.PoiPlaceholder;
import eu.trentorise.smartcampus.dt.custom.Utils;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
import eu.trentorise.smartcampus.dt.custom.data.FollowAsyncTaskProcessor;
import eu.trentorise.smartcampus.dt.custom.map.MapManager;
import eu.trentorise.smartcampus.dt.fragments.home.HomeFragment;
import eu.trentorise.smartcampus.dt.fragments.search.SearchFragment;
import eu.trentorise.smartcampus.dt.fragments.search.WhenForSearch;
import eu.trentorise.smartcampus.dt.fragments.search.WhereForSearch;
import eu.trentorise.smartcampus.dt.model.PoiObjectForBean;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.social.model.Concept;
import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;
import eu.trentorise.smartcampus.territoryservice.model.POIObject;

public class PoisListingFragment extends AbstractLstingFragment<POIObject> implements TagProvider {

	private ListView list;
	private Context context;
	private String category;
	public static final String ARG_ID = "id_poi";
	public static final String ARG_INDEX = "index_adapter";
	private String idPoi = "";
	private Integer indexAdapter;
	private Boolean reload = false;
	private Integer postitionSelected = 0;

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(ARG_ID, idPoi);
		if (indexAdapter != null)
			outState.putInt(ARG_INDEX, indexAdapter);

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.context = this.getSherlockActivity();
        
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
		list = (ListView) getSherlockActivity().findViewById(R.id.pois_list);

		if (arg0 != null) {
			// Restore last state for checked position.
			idPoi = arg0.getString(ARG_ID);
			indexAdapter = arg0.getInt(ARG_INDEX);

		}
		if (getPoiAdapter() == null) {
			setAdapter(new PoiAdapter(context, R.layout.pois_row));
		} else {
			setAdapter(getPoiAdapter());
		}

	}

	private PoiAdapter getPoiAdapter() {
		return (PoiAdapter) getAdapter();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		return inflater.inflate(R.layout.poislist, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!idPoi.equals("")) {
			// get info of the event
			POIObject poi = DTHelper.findPOIById(idPoi);

			if (poi == null) {
				// cancellazione
				removePoi(indexAdapter);

			} else {
				// modifica se numero della versione e' diverso
				 if (poi.getUpdateTime() != getPoiAdapter().getItem(indexAdapter).getUpdateTime()) {
					removePoi(indexAdapter);
					insertPOI(poi);
				}
			}
			// notify
			getPoiAdapter().notifyDataSetChanged();
			updateList(getAdapter().getCount() == 0);
			idPoi = "";
			indexAdapter = 0;
		}
	}

	/*
	 * insert in the same adapter the new item
	 */
	private void insertPOI(POIObject poi) {
		PoiAdapter poiAdapter = getPoiAdapter();

		// add in the right place
		int i = 0;
		boolean insert = false;
		while (i < poiAdapter.getCount()) {
			if (poiAdapter.getItem(i).getTitle() != null) {
				if (poiAdapter.getItem(i).getTitle().toLowerCase().compareTo(poi.getTitle().toLowerCase()) <= 0) {
					i++;
				} else {
					poiAdapter.insert(poi, i);
					insert = true;
					break;
				}
			}
		}

		if (!insert) {
			poiAdapter.insert(poi, poiAdapter.getCount());
		}
	}

	/* clean the adapter from the items modified or erased */
	private void removePoi(Integer indexAdapter) {
		PoiAdapter poisAdapter = getPoiAdapter();
		POIObject objectToRemove = poisAdapter.getItem(indexAdapter);
		int i = 0;
		while (i < poisAdapter.getCount()) {
			if (poisAdapter.getItem(i).getEntityId() == objectToRemove.getEntityId()) {
				poisAdapter.remove(poisAdapter.getItem(i));
			} else {
				i++;
			}
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		getSherlockActivity().getSupportMenuInflater().inflate(R.menu.gripmenu, menu);
		SubMenu submenu = menu.getItem(0).getSubMenu();
		submenu.clear();

		if (category == null) {
			category = (getArguments() != null) ? getArguments().getString(SearchFragment.ARG_CATEGORY) : null;
		}

		if (getArguments() == null || !getArguments().containsKey(SearchFragment.ARG_LIST)
				&& !getArguments().containsKey(SearchFragment.ARG_QUERY)) {
			submenu.add(Menu.CATEGORY_SYSTEM, R.id.submenu_search, Menu.NONE, R.string.search_txt);
		}

		submenu.add(Menu.CATEGORY_SYSTEM, R.id.map_view, Menu.NONE, R.string.map_view);

		if (category != null) {
			String addString = getString(R.string.add_poi_in_cat, getString(CategoryHelper.getCategoryDescriptorByCategoryFiltered(CategoryHelper.CATEGORY_TYPE_POIS,
							category).description));

			submenu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_addpoi, Menu.NONE, addString);
		}

		//NotificationsSherlockFragmentDT.onPrepareOptionsMenuNotifications(menu);

		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {


		if (item.getItemId() == R.id.map_view) {
			category = (getArguments() != null) ? getArguments().getString(SearchFragment.ARG_CATEGORY) : null;
			if (category != null) {
				MapManager.switchToMapView(category, HomeFragment.ARG_POI_CATEGORY, this);
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
			{
				FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager().beginTransaction();
				Fragment fragment = new CreatePoiFragment();
				Bundle args = new Bundle();
				args.putString(SearchFragment.ARG_CATEGORY, category);
				fragment.setArguments(args);
				fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				// fragmentTransaction.detach(this);
				fragmentTransaction.replace(R.id.fragment_container, fragment, "pois");
				fragmentTransaction.addToBackStack(fragment.getTag());
				fragmentTransaction.commit();
				reload = true;
				return true;
			}
		} else if (item.getItemId() == R.id.submenu_search) {
			FragmentTransaction fragmentTransaction;
			Fragment fragment;
			fragmentTransaction = getSherlockActivity().getSupportFragmentManager().beginTransaction();
			fragment = new SearchFragment();
			Bundle args = new Bundle();
			args.putString(SearchFragment.ARG_CATEGORY, category);
			args.putString(CategoryHelper.CATEGORY_TYPE_POIS, CategoryHelper.CATEGORY_TYPE_POIS);
			if (getArguments() != null && getArguments().containsKey(SearchFragment.ARG_MY)
					&& getArguments().getBoolean(SearchFragment.ARG_MY))
				args.putBoolean(SearchFragment.ARG_MY, getArguments().getBoolean(SearchFragment.ARG_MY));
			fragment.setArguments(args);
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(R.id.fragment_container, fragment, "pois");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
			/* add category to bundle */
			return true;

		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onStart() {
		if (reload) {
			getPoiAdapter().clear();
			reload = false;
		}
		
		DiscoverTrentoActivity.mDrawerToggle.setDrawerIndicatorEnabled(false);
    	DiscoverTrentoActivity.drawerState = "off";
        getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(true);
        getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(true);
        
		Bundle bundle = this.getArguments();
		String category = (bundle != null) ? bundle.getString(SearchFragment.ARG_CATEGORY) : null;
		CategoryDescriptor catDescriptor = CategoryHelper.getCategoryDescriptorByCategoryFiltered("pois", category);
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
				title.append(" " + where.getDescription() + " ");
		}

		// close items menus if open
		((View) list.getParent()).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				hideListItemsMenu(v, false);
			}
		});
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				hideListItemsMenu(view, false);
				setStorePoiId(view, position);

			}
		});

		FeedbackFragmentInflater.inflateHandleButton(getSherlockActivity(), getView());
		super.onStart();
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		super.onScrollStateChanged(view, scrollState);
		if ((postitionSelected != -1) && (scrollState == SCROLL_STATE_TOUCH_SCROLL)) {
			hideListItemsMenu(view, false);
		}
	}

	protected void setupOptionsListeners(final ViewSwitcher vs, final int position) {
		final POIObject poi = ((PoiPlaceholder) vs.getTag()).poi;

		ImageButton b = (ImageButton) vs.findViewById(R.id.poi_delete_btn);
		// CAN DELETE ONLY OWN OBJECTS
		if (DTHelper.isOwnedObject(poi)) {
			b.setVisibility(View.VISIBLE);
			b.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					{
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
				{
					FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager()
							.beginTransaction();
					Fragment fragment = new CreatePoiFragment();
					setStorePoiId((View) vs, position);
					Bundle args = new Bundle();
					args.putSerializable(CreatePoiFragment.ARG_POI, poi);
					fragment.setArguments(args);
					fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					// fragmentTransaction.detach(this);
					fragmentTransaction.replace(R.id.fragment_container, fragment, "pois");
					fragmentTransaction.addToBackStack(fragment.getTag());
					fragmentTransaction.commit();
				}
			}
		});
		b = (ImageButton) vs.findViewById(R.id.poi_tag_btn);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				{
					TaggingDialog taggingDialog = new TaggingDialog(getActivity(), new TaggingDialog.OnTagsSelectedListener() {

						@SuppressWarnings("unchecked")
						@Override
						public void onTagsSelected(Collection<SemanticSuggestion> suggestions) {
							new TaggingAsyncTask(poi).execute(Utils.conceptConvertSS(suggestions));
						}
					}, PoisListingFragment.this, Utils.conceptConvertToSS(poi.getCommunityData().getTags()));
					taggingDialog.show();
				}
			}
		});
		b = (ImageButton) vs.findViewById(R.id.poi_follow_btn);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SCAsyncTask<Object, Void, BaseDTObject> followTask = new SCAsyncTask<Object, Void, BaseDTObject>(getSherlockActivity(),
						new FollowAsyncTaskProcessor(getSherlockActivity(), null));
				followTask.execute(getSherlockActivity().getApplicationContext(), DTParamsHelper.getAppToken(),
						DTHelper.getAuthToken(), poi);

			}
		});
	}

	private void hideListItemsMenu(View v, boolean close) {
		boolean toBeHidden = false;
		for (int index = 0; index < list.getChildCount(); index++) {
			View view = list.getChildAt(index);
			if (view instanceof ViewSwitcher && ((ViewSwitcher) view).getDisplayedChild() == 1) {
				((ViewSwitcher) view).showPrevious();
				toBeHidden = true;
				getPoiAdapter().setElementSelected(-1);
				postitionSelected = -1;
			}
		}
		if (!toBeHidden && v != null && v.getTag() != null && !close) {
			// no items needed to be flipped, fill and open details page
			FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager().beginTransaction();
			PoiDetailsFragment fragment = new PoiDetailsFragment();

			Bundle args = new Bundle();
			args.putString(PoiDetailsFragment.ARG_POI_ID, ((PoiPlaceholder) v.getTag()).poi.getId());
			fragment.setArguments(args);

			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			// fragmentTransaction.detach(this);
			fragmentTransaction.replace(R.id.fragment_container, fragment, "pois");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
		}
	}

	private void setStorePoiId(View v, int position) {
		final POIObject poi = ((PoiPlaceholder) v.getTag()).poi;
		idPoi = poi.getId();
		indexAdapter = position;
	}

	private List<POIObject> getPOIs(AbstractLstingFragment.ListingRequest... params) {
		try {
			Collection<PoiObjectForBean> result = null;
			List<POIObject> returnArray = new ArrayList<POIObject>();
			Bundle bundle = getArguments();
			boolean my = false;
			if (bundle.getBoolean(SearchFragment.ARG_MY))
				my = true;
			String categories = bundle.getString(SearchFragment.ARG_CATEGORY);

			SortedMap<String, Integer> sort = new TreeMap<String, Integer>();
			sort.put("title", 1);
			if (bundle.containsKey(SearchFragment.ARG_CATEGORY) && (bundle.getString(SearchFragment.ARG_CATEGORY) != null)) {

				result = DTHelper.searchInGeneral(params[0].position, params[0].size,
						bundle.getString(SearchFragment.ARG_QUERY),
						(WhereForSearch) bundle.getParcelable(SearchFragment.ARG_WHERE_SEARCH),
						(WhenForSearch) bundle.getParcelable(SearchFragment.ARG_WHEN_SEARCH), my, PoiObjectForBean.class, sort,
						categories);

			} else if (bundle.containsKey(SearchFragment.ARG_MY) && (bundle.getBoolean(SearchFragment.ARG_MY))) {

				result = DTHelper.searchInGeneral(params[0].position, params[0].size,
						bundle.getString(SearchFragment.ARG_QUERY),
						(WhereForSearch) bundle.getParcelable(SearchFragment.ARG_WHERE_SEARCH),
						(WhenForSearch) bundle.getParcelable(SearchFragment.ARG_WHEN_SEARCH), my, PoiObjectForBean.class, sort,
						categories);

			} else if (bundle.containsKey(SearchFragment.ARG_QUERY)) {

				result = DTHelper.searchInGeneral(params[0].position, params[0].size,
						bundle.getString(SearchFragment.ARG_QUERY),
						(WhereForSearch) bundle.getParcelable(SearchFragment.ARG_WHERE_SEARCH),
						(WhenForSearch) bundle.getParcelable(SearchFragment.ARG_WHEN_SEARCH), my, PoiObjectForBean.class, sort,
						categories);

			} else if (bundle.containsKey(SearchFragment.ARG_LIST)) {
				List<PoiObjectForBean> results = (List<PoiObjectForBean>) bundle.get(SearchFragment.ARG_LIST);
				for (PoiObjectForBean storyBean : results) {
					returnArray.add(storyBean.getObjectForBean());
				}
				return returnArray;
			} else {
				return Collections.emptyList();
			}


			for (PoiObjectForBean storyBean : result) {
				returnArray.add(storyBean.getObjectForBean());
			}
			return returnArray;
			
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
		if (getView()!=null){

		eu.trentorise.smartcampus.dt.custom.ViewHelper.removeEmptyListView((LinearLayout) getView().findViewById(
				R.id.poilistcontainer));
		if (empty) {
			eu.trentorise.smartcampus.dt.custom.ViewHelper.addEmptyListView((LinearLayout) getView().findViewById(
					R.id.poilistcontainer));
		}
		hideListItemsMenu(null, false);
		}
	}

}
