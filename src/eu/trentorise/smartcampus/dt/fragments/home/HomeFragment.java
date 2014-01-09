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
package eu.trentorise.smartcampus.dt.fragments.home;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockMapFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.feedback.utils.FeedbackFragmentInflater;
import eu.trentorise.smartcampus.dt.DTParamsHelper;
import eu.trentorise.smartcampus.dt.DiscoverTrentoActivity;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.dt.custom.CategoryHelper;
import eu.trentorise.smartcampus.dt.custom.CategoryHelper.CategoryDescriptor;
import eu.trentorise.smartcampus.dt.custom.ViewHelper;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
import eu.trentorise.smartcampus.dt.custom.map.MapItemsHandler;
import eu.trentorise.smartcampus.dt.custom.map.MapLayerDialogHelper;
import eu.trentorise.smartcampus.dt.custom.map.MapLoadProcessor;
import eu.trentorise.smartcampus.dt.custom.map.MapManager;
import eu.trentorise.smartcampus.dt.custom.map.MapObjectContainer;
import eu.trentorise.smartcampus.dt.fragments.events.EventDetailsFragment;
import eu.trentorise.smartcampus.dt.fragments.events.EventsListingFragment;
import eu.trentorise.smartcampus.dt.fragments.pois.PoiDetailsFragment;
import eu.trentorise.smartcampus.dt.fragments.pois.PoisListingFragment;
import eu.trentorise.smartcampus.dt.fragments.search.SearchFragment;
import eu.trentorise.smartcampus.dt.fragments.stories.StoryDetailsFragment;
import eu.trentorise.smartcampus.dt.model.LocalEventObject;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;
import eu.trentorise.smartcampus.territoryservice.model.EventObject;
import eu.trentorise.smartcampus.territoryservice.model.POIObject;
import eu.trentorise.smartcampus.territoryservice.model.StoryObject;

public class HomeFragment extends SherlockMapFragment implements MapItemsHandler, OnCameraChangeListener,
		OnMarkerClickListener, MapObjectContainer {

	public static final String ARG_OBJECTS = "objects";
	public static final String ARG_POI_CATEGORY = "poi category";
	public static final String ARG_EVENT_CATEGORY = "event category";

	protected GoogleMap mMap;

	private String[] poiCategories = null;
	private String[] eventsCategories = null;
	private String[] eventsNotTodayCategories = null;
	private Collection<? extends BaseDTObject> objects;

	private boolean loaded = false;
	private boolean fitMap = true;
	
	private Collection<BaseDTObject> dirtyObjects = null;
	
	
	@Override
	public void onStart() {
		super.onStart();
		DiscoverTrentoActivity.mDrawerToggle.setDrawerIndicatorEnabled(true);
		DiscoverTrentoActivity.drawerState = "on";
		// hide keyboard if it is still open
		ViewHelper.hideKeyboard(getActivity(), getActivity().findViewById(R.id.fragment_container));

		FeedbackFragmentInflater.inflateHandleButton(getSherlockActivity(), getView());

		if (!loaded) {
			String key = getString(R.string.view_intent_arg_object_id);
			if (getActivity().getIntent() != null && getActivity().getIntent().hasExtra(key)) {
				new SCAsyncTask<Void, Void, BaseDTObject>(getActivity(), new LoadDataProcessor(getActivity())).execute();
				eventsCategories = null;
				poiCategories = null;
			} else {
				initView();
			}
			loaded = true;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("loaded", loaded);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CategoryDescriptor[] eventsDefault = DTParamsHelper.getDefaultArrayByParams(CategoryHelper.CATEGORY_TYPE_EVENTS);
		if (eventsDefault != null) {
			List<String> eventCategory = new ArrayList<String>();
			for (CategoryDescriptor event : eventsDefault)
				eventCategory.add(event.category);
			eventsCategories = Arrays.asList(eventCategory.toArray()).toArray(new String[eventCategory.toArray().length]);
		}
		CategoryDescriptor[] poisDefault = DTParamsHelper.getDefaultArrayByParams(CategoryHelper.CATEGORY_TYPE_POIS);
		if (poisDefault != null) {
			List<String> poisCategory = new ArrayList<String>();
			for (CategoryDescriptor poi : poisDefault)
				poisCategory.add(poi.category);
			poiCategories = Arrays.asList(poisCategory.toArray()).toArray(new String[poisCategory.toArray().length]);

		}

		setHasOptionsMenu(true);
	}

	/**
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected void initView() {
		if (getSupportMap() != null) {
			getSupportMap().clear();
			getSupportMap().getUiSettings().setRotateGesturesEnabled(false);
			getSupportMap().getUiSettings().setTiltGesturesEnabled(false);
		}



		if (getArguments() != null && getArguments().containsKey(ARG_OBJECTS)) {
			poiCategories = null;
			eventsCategories = null;
			List<BaseDTObject> list = (List<BaseDTObject>) getArguments().getSerializable(ARG_OBJECTS);
			new AsyncTask<List<BaseDTObject>, Void, List<BaseDTObject>>() {
				@Override
				protected List<BaseDTObject> doInBackground(List<BaseDTObject>... params) {
					return params[0];
				}

				@Override
				protected void onPostExecute(List<BaseDTObject> result) {
					addObjects(result);
				}
			}.execute(list);
		} else if (getArguments() != null && getArguments().containsKey(ARG_POI_CATEGORY)) {
			eventsCategories = null;
			setPOICategoriesToLoad(getArguments().getString(ARG_POI_CATEGORY));
		} else if (getArguments() != null && getArguments().containsKey(ARG_EVENT_CATEGORY)) {
			poiCategories = null;
			setEventCategoriesToLoad(getArguments().getString(ARG_EVENT_CATEGORY));
		} else {
			// this is initial load, disable map fit
			fitMap = false;
			if (poiCategories != null) {
				setPOICategoriesToLoad(poiCategories);
			}
			if (eventsCategories != null) {
				setEventCategoriesToLoad(eventsCategories);
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (getSupportMap() != null) {
			getSupportMap().setMyLocationEnabled(true);
			getSupportMap().setOnCameraChangeListener(this);
			getSupportMap().setOnMarkerClickListener(this);
			if (dirtyObjects != null) {
				boolean found = false;
				Set<String> deleted = DTHelper.findDeleted(dirtyObjects);
				for (Iterator<? extends BaseDTObject> iterator = objects.iterator(); iterator.hasNext();) {
					BaseDTObject o = iterator.next();
					if (deleted.contains(o.getId())) {
						found = true;
						iterator.remove();
					}
				}
				if (found) render(objects);
				dirtyObjects = null;
			} 
			// if (objects != null) {
			// render(objects);
			// }
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

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		MenuItem item = menu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_show_places_layers, 1,
				R.string.menu_item__places_layers_text);
		item.setIcon(R.drawable.ic_menu_pois);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		item = menu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_show_events_layers, 1, R.string.menu_item__events_layers_text);
		item.setIcon(R.drawable.ic_menu_events);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_item_show_places_layers) {
			fitMap = true;
			MapLayerDialogHelper.createPOIDialog(getActivity(), this, getString(R.string.layers_title_places), poiCategories)
					.show();
			return true;
		} else if (item.getItemId() == R.id.menu_item_show_events_layers) {
			fitMap = true;
			Dialog eventsDialog = MapLayerDialogHelper.createEventsDialog(getActivity(), this,
					getString(R.string.layers_title_events), eventsCategories);
			eventsDialog.show();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	public void setPOICategoriesToLoad(final String... categories) {
		this.poiCategories = categories;
		/* actually only event or poi at the same time */
		this.eventsCategories = null;

		new SCAsyncTask<Void, Void, Collection<? extends BaseDTObject>>(getActivity(), new MapLoadProcessor(getActivity(),
				this, getSupportMap()) {
			@Override
			protected Collection<? extends BaseDTObject> getObjects() {
				try {
					/* check if todays is checked and cat with searchTodayEvents */
					return DTHelper.getPOIByCategory(0, -1, categories);
				} catch (Exception e) {
					e.printStackTrace();
					return Collections.emptyList();
				}
			}

		}).execute();
	}

	private void onBaseDTObjectTap(BaseDTObject o) {
		dirtyObjects = new ArrayList<BaseDTObject>(1);
		dirtyObjects.add(o);
		
		Bundle args = new Bundle();
		args.putSerializable(InfoDialog.PARAM, o);
		InfoDialog dtoTap=new InfoDialog();
		dtoTap.setArguments(args);
		dtoTap.show(getActivity().getSupportFragmentManager(), "me");
	}

	private void onBaseDTObjectsTap(List<BaseDTObject> list) {
		if (list == null || list.size() == 0)
			return;
		if (list.size() == 1) {
			onBaseDTObjectTap(list.get(0));
			return;
		}
		
		dirtyObjects = new HashSet<BaseDTObject>(list);
		
		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
		SherlockFragment fragment = null;
		Bundle args = new Bundle();
		if (list.get(0) instanceof LocalEventObject) {
			fragment = new EventsListingFragment();
			args.putSerializable(SearchFragment.ARG_LIST, new ArrayList(list));
		} else if (list.get(0) instanceof POIObject) {
			fragment = new PoisListingFragment();
			args.putSerializable(SearchFragment.ARG_LIST, new ArrayList(list));
		}
		if (fragment != null) {
			fragment.setArguments(args);
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			// fragmentTransaction.detach(this);
			fragmentTransaction.replace(R.id.fragment_container, fragment, "me");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
		}
	}

	@Override
	public void setEventCategoriesToLoad(final String... categories) {
		this.eventsCategories = categories;
		this.eventsNotTodayCategories = categories;

		/* actually only event or poi at the same time */
		this.poiCategories = null;

		// mItemizedoverlay.clearMarkers();

		new SCAsyncTask<Void, Void, Collection<? extends BaseDTObject>>(getActivity(), new MapLoadProcessor(getActivity(),
				this, getSupportMap()) {
			@Override
			protected Collection<? extends BaseDTObject> getObjects() {
				try {
					/* check if todays is checked and cat with searchTodayEvents */

					if (isTodayIncluded()) {
						List<LocalEventObject> newList = new ArrayList<LocalEventObject>();
						newList.addAll(DTHelper.searchTodayEvents(0, -1, ""));
						if (categories != null)
							newList.addAll(DTHelper.getEventsByCategories(0, -1, eventsNotTodayCategories));
						return newList;
					} else
						return DTHelper.getEventsByCategories(0, -1, categories);
				} catch (Exception e) {
					e.printStackTrace();
					return Collections.emptyList();
				}
			}

		}).execute();
	}

	private boolean isTodayIncluded() {
		List<String> categoriesNotToday = new ArrayList<String>();
		boolean istodayincluded = false;
		if (eventsCategories.length > 0)
			for (int i = 0; i < eventsCategories.length; i++) {
				if (eventsCategories[i].contains("Today")) {

					istodayincluded = true;
				} else
					categoriesNotToday.add(eventsCategories[i]);

			}
		eventsNotTodayCategories = categoriesNotToday.toArray(new String[categoriesNotToday.size()]);
		return istodayincluded;
	}

	private GoogleMap getSupportMap() {
		if (mMap == null) {
			if (getFragmentManager().findFragmentById(R.id.fragment_container) != null
					&& getFragmentManager().findFragmentById(R.id.fragment_container) instanceof SupportMapFragment)
				mMap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.fragment_container)).getMap();
			if (mMap != null)
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MapManager.DEFAULT_POINT, MapManager.ZOOM_DEFAULT));

		}
		return mMap;
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		List<BaseDTObject> list = MapManager.ClusteringHelper.getFromGridId(marker.getTitle());
		if (list == null || list.isEmpty())
			return true;

		if (list.size() == 1) {
			onBaseDTObjectTap(list.get(0));
		} else if (MapManager.maxZoom(getSupportMap())) {
			onBaseDTObjectsTap(list);
		} else {
			MapManager.fitMapWithOverlays(list, getSupportMap());
		}
		return true;
	}

	@Override
	public void onCameraChange(CameraPosition position) {
		render(objects);
	}

	@Override
	public <T extends BaseDTObject> void addObjects(Collection<? extends BaseDTObject> objects) {
		if (getSupportMap() != null) {
			this.objects = objects;
			render(objects);
			if (fitMap) {
				MapManager.fitMapWithOverlays(objects, getSupportMap());
			}
		}
	}

	private void render(Collection<? extends BaseDTObject> objects) {
		if (getSupportMap() != null) {
			getSupportMap().clear();
			if (objects != null && getSherlockActivity() != null) {
				List<MarkerOptions> cluster = MapManager.ClusteringHelper.cluster(
						getSherlockActivity().getApplicationContext(), getSupportMap(), objects);
				MapManager.ClusteringHelper.render(getSupportMap(), cluster);
			}
		}

	}

	private class LoadDataProcessor extends AbstractAsyncTaskProcessor<Void, BaseDTObject> {

		public LoadDataProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public BaseDTObject performAction(Void... params) throws SecurityException, Exception {
			String entityId = getActivity().getIntent().getStringExtra(getString(R.string.view_intent_arg_object_id));
			String type = getActivity().getIntent().getStringExtra(getString(R.string.view_intent_arg_entity_type));

			if (entityId != null && type != null) {
				if ("event".equals(type))
					return DTHelper.findEventByEntityId(entityId).getObjectForBean();
				else if ("location".equals(type))
					return DTHelper.findPOIByEntityId(entityId).getObjectForBean();
				else if ("narrative".equals(type))
					return DTHelper.findStoryByEntityId(entityId).getObjectForBean();
			}
			return null;
		}

		@Override
		public void handleResult(BaseDTObject result) {

			String key = getString(R.string.view_intent_arg_object_id);
			String entityId = getActivity().getIntent().getStringExtra(key);
			getActivity().getIntent().removeExtra(key);

			if (entityId != null) {
				if (result == null) {
					Toast.makeText(getActivity(), R.string.app_failure_obj_not_found, Toast.LENGTH_LONG).show();
					return;
				}

				SherlockFragment fragment = null;
				Bundle args = new Bundle();
				if (result instanceof POIObject) {
					fragment = new PoiDetailsFragment();
					args.putString(PoiDetailsFragment.ARG_POI_ID, result.getId());
				} else if (result instanceof EventObject) {
					fragment = new EventDetailsFragment();
					args.putString(EventDetailsFragment.ARG_EVENT_ID, (result.getId()));
				} else if (result instanceof StoryObject) {
					fragment = new StoryDetailsFragment();
					args.putString(StoryDetailsFragment.ARG_STORY_ID, result.getId());
				}
				if (fragment != null) {
					FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
					fragment.setArguments(args);

					fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					fragmentTransaction.replace(R.id.fragment_container, fragment, "me");
					fragmentTransaction.addToBackStack(fragment.getTag());
					fragmentTransaction.commit();
				}
			}
		}

	}

}
