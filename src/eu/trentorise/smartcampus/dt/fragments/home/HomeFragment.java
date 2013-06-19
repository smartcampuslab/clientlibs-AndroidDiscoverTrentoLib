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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.dt.DTParamsHelper;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.CategoryHelper;
import eu.trentorise.smartcampus.dt.custom.CategoryHelper.CategoryDescriptor;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
import eu.trentorise.smartcampus.dt.custom.map.BaseDTObjectMapItemTapListener;
import eu.trentorise.smartcampus.dt.custom.map.DTItemizedOverlay;
import eu.trentorise.smartcampus.dt.custom.map.MapItemsHandler;
import eu.trentorise.smartcampus.dt.custom.map.MapLayerDialogHelper;
import eu.trentorise.smartcampus.dt.custom.map.MapLoadProcessor;
import eu.trentorise.smartcampus.dt.custom.map.MapManager;
import eu.trentorise.smartcampus.dt.fragments.events.EventsListingFragment;
import eu.trentorise.smartcampus.dt.fragments.pois.PoisListingFragment;
import eu.trentorise.smartcampus.dt.fragments.search.SearchFragment;
import eu.trentorise.smartcampus.dt.model.BaseDTObject;
import eu.trentorise.smartcampus.dt.model.EventObject;
import eu.trentorise.smartcampus.dt.model.POIObject;
import eu.trentorise.smartcampus.dt.notifications.NotificationsSherlockFragmentDT;

public class HomeFragment extends NotificationsSherlockFragmentDT implements MapItemsHandler,
		BaseDTObjectMapItemTapListener {

	public static final String ARG_OBJECTS = "objects";
	public static final String ARG_POI_CATEGORY = "poi category";
	public static final String ARG_EVENT_CATEGORY = "event category";

	protected ViewGroup mapContainer;
	protected MapView mapView;
	DTItemizedOverlay mItemizedoverlay = null;
	DTItemizedOverlay mEventsItemizedoverlay = null;
	DTItemizedOverlay mPoisItemizedoverlay = null;
	
	MyLocationOverlay mMyLocationOverlay = null;

	private Context context;
	private String[] poiCategories = null;
	private String[] eventsCategories = null;
	private String[] eventsNotTodayCategories = null;

	@Override
	public void onStart() {
		super.onStart();
		// hide keyboard if it is still open
		InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(
				Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mapView.getWindowToken(), 0);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.context = this.getSherlockActivity();
		CategoryDescriptor[] eventsDefault = DTParamsHelper
				.getDefaultArrayByParams(CategoryHelper.CATEGORY_TYPE_EVENTS);
		if (eventsDefault != null) {
			List<String> eventCategory = new ArrayList<String>();
			for (CategoryDescriptor event: eventsDefault)
					eventCategory.add(event.category);
			eventsCategories=  Arrays.asList(eventCategory.toArray()).toArray(new String[eventCategory.toArray().length]);
		}
		CategoryDescriptor[] poisDefault = DTParamsHelper.getDefaultArrayByParams(CategoryHelper.CATEGORY_TYPE_POIS);
		if (poisDefault != null) {
			List<String> poisCategory= new ArrayList<String>();
			for (CategoryDescriptor poi: poisDefault)
				poisCategory.add(poi.category);
			poiCategories= Arrays.asList(poisCategory.toArray()).toArray(new String[poisCategory.toArray().length]);

		}

		setHasOptionsMenu(true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		boolean initialized = mapView != null;
		mapContainer = new RelativeLayout(getActivity());
		mapView = MapManager.getMapView();
		final ViewGroup parent = (ViewGroup) mapView.getParent();
		if (parent != null) {
			parent.removeView(mapView);
		}
		mapContainer.addView(mapView);

		List<Overlay> listOfOverlays = mapView.getOverlays();
		mapView.getOverlays().clear();

		mItemizedoverlay = new DTItemizedOverlay(getActivity(), mapView);
		mItemizedoverlay.setMapItemTapListener(this);
		listOfOverlays.add(mItemizedoverlay);
		// setEventCategoriesToLoad("Family");

		mMyLocationOverlay = new MyLocationOverlay(getSherlockActivity(), mapView) {
			@Override
			protected void drawMyLocation(Canvas canvas, MapView mapView, Location lastFix, GeoPoint myLocation,
					long when) {
				Projection p = mapView.getProjection();
				float accuracy = p.metersToEquatorPixels(lastFix.getAccuracy());
				Point loc = p.toPixels(myLocation, null);
				Paint paint = new Paint();
				paint.setAntiAlias(true);
				// paint.setColor(Color.BLUE);
				paint.setColor(Color.parseColor(context.getResources().getString(R.color.dtappcolor)));

				if (accuracy > 10.0f) {
					paint.setAlpha(50);
					canvas.drawCircle(loc.x, loc.y, accuracy, paint);
					// border
					paint.setAlpha(200);
					paint.setStyle(Paint.Style.STROKE);
					canvas.drawCircle(loc.x, loc.y, accuracy, paint);
				}

				Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.me).copy(
						Bitmap.Config.ARGB_8888, true);
				canvas.drawBitmap(bitmap, loc.x - (bitmap.getWidth() / 2), loc.y - bitmap.getHeight(), null);
			}
		};
		listOfOverlays.add(mMyLocationOverlay);

		if (!initialized) {
			// TODO correct for final version
			mapView.getController().animateTo(MapManager.trento());
			mapView.getController().setZoom(MapManager.ZOOM_DEFAULT);
		}

		if (getArguments() != null && getArguments().containsKey(ARG_OBJECTS)) {
			final List<BaseDTObject> list = (List<BaseDTObject>) getArguments().getSerializable(ARG_OBJECTS);
			MapManager.fitMap(list, mapView);
			new SCAsyncTask<Void, Void, Collection<? extends BaseDTObject>>(getActivity(), new MapLoadProcessor(
					getActivity(), mItemizedoverlay, mapView) {
				@Override
				protected Collection<? extends BaseDTObject> getObjects() {
					try {
						return list;
					} catch (Exception e) {
						e.printStackTrace();
						return Collections.emptyList();
					}
				}
			}).execute();
		} else if (getArguments() != null && getArguments().containsKey(ARG_POI_CATEGORY)) {
			setPOICategoriesToLoad(getArguments().getString(ARG_POI_CATEGORY));
		} else if (getArguments() != null && getArguments().containsKey(ARG_EVENT_CATEGORY)) {
			setEventCategoriesToLoad(getArguments().getString(ARG_EVENT_CATEGORY));
		} else {
			if (poiCategories != null) {
				setPOICategoriesToLoad(poiCategories);
			}
			if (eventsCategories != null) {
				setEventCategoriesToLoad(eventsCategories);
			}
		}
		return mapContainer;
	}

	@Override
	public void onResume() {
		mMyLocationOverlay.enableMyLocation();
		super.onResume();
	}

	@Override
	public void onPause() {
		mMyLocationOverlay.disableMyLocation();
		super.onPause();
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		MenuItem item = menu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_show_places_layers, 1,
				R.string.menu_item__places_layers_text);
		item.setIcon(R.drawable.ic_menu_pois);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		item = menu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_show_events_layers, 1,
				R.string.menu_item__events_layers_text);
		item.setIcon(R.drawable.ic_menu_events);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_item_show_places_layers) {
			MapLayerDialogHelper.createPOIDialog(getActivity(), this, getString(R.string.layers_title_places),
					poiCategories).show();
			return true;
		} else if (item.getItemId() == R.id.menu_item_show_events_layers) {
			Dialog eventsDialog = MapLayerDialogHelper.createEventsDialog(getActivity(), this,
					getString(R.string.layers_title_events), eventsCategories);
			eventsDialog.show();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

//	public void setEventsCategoriesToLoad(final String... categories) {
//		this.eventsCategories = categories;
//		
//		mItemizedoverlay.clearMarkers();
//
//		loadEventsCategory(categories);
//		
//		if (this.poiCategories.length>0)
//		{
//			loadPoisCategory(this.poiCategories);
//		}
//	}

	private void loadPoisCategory(final String... categories) {
		new SCAsyncTask<Void, Void, Collection<? extends BaseDTObject>>(getActivity(), new MapLoadProcessor(
				getActivity(), mItemizedoverlay, mapView) {
			@Override
			protected Collection<? extends BaseDTObject> getObjects() {
				try {
					// TODO
					return DTHelper.getPOIByCategory(0, -1, categories);
				} catch (Exception e) {
					e.printStackTrace();
					return Collections.emptyList();
				}
			}
		}).execute();
		
	}

	public void setPOICategoriesToLoad(final String... categories) {
		this.poiCategories = categories;
		mItemizedoverlay.clearMarkers();

		loadPoisCategory(categories);
		if (this.eventsCategories.length>0)
		{
			loadEventsCategory(this.poiCategories);
		}
	}

	private void loadEventsCategory(final String... categories) {
		new SCAsyncTask<Void, Void, Collection<? extends BaseDTObject>>(getActivity(), new MapLoadProcessor(
				getActivity(), mItemizedoverlay, mapView) {
			@Override
			protected Collection<? extends BaseDTObject> getObjects() {
				try {
					// TODO
					return DTHelper.getEventsByCategories(0, -1, categories);
				} catch (Exception e) {
					e.printStackTrace();
					return Collections.emptyList();
				}
			}
		}).execute();
		
	}

	@Override
	public void onBaseDTObjectTap(BaseDTObject o) {
		new InfoDialog(o).show(getSherlockActivity().getSupportFragmentManager(), "me");
	}

	@Override
	public void onBaseDTObjectsTap(List<BaseDTObject> list) {
		if (list == null || list.size() == 0)
			return;
		if (list.size() == 1) {
			onBaseDTObjectTap(list.get(0));
			return;
		}
		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
		SherlockFragment fragment = null;
		Bundle args = new Bundle();
		if (list.get(0) instanceof EventObject) {
			fragment = new EventsListingFragment();
			args.putSerializable(SearchFragment.ARG_LIST, new ArrayList<EventObject>((List) list));
		} else if (list.get(0) instanceof POIObject) {
			fragment = new PoisListingFragment();
			args.putSerializable(SearchFragment.ARG_LIST, new ArrayList<POIObject>((List) list));
		}
		if (fragment != null) {
			fragment.setArguments(args);
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			// fragmentTransaction.detach(this);
			fragmentTransaction.replace(android.R.id.content, fragment, "home");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
		}
	}

	@Override
	public void setEventCategoriesToLoad(final String... categories) {
		this.eventsCategories = categories;
		this.eventsNotTodayCategories = categories;

		mItemizedoverlay.clearMarkers();

		new SCAsyncTask<Void, Void, Collection<? extends BaseDTObject>>(getActivity(), new MapLoadProcessor(
				getActivity(), mItemizedoverlay, mapView) {
			@Override
			protected Collection<? extends BaseDTObject> getObjects() {
				try {
					/* check if todays is checked and cat with searchTodayEvents */

					if (isTodayIncluded()) {
						List<EventObject> newList = new ArrayList<EventObject>();
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
		
		if (this.poiCategories.length>0)
		{
			loadPoisCategory(this.poiCategories);
		}
	}

	private boolean isTodayIncluded() {
		List<String> categoriesNotToday = new ArrayList<String>();
		boolean istodayincluded = false;
		if (eventsNotTodayCategories.length > 0)
			for (int i = 0; i < eventsNotTodayCategories.length; i++) {
				if (eventsNotTodayCategories[i].contains("Today")) {

					istodayincluded = true;
				} else
					categoriesNotToday.add(eventsNotTodayCategories[i]);

			}
		eventsNotTodayCategories = categoriesNotToday.toArray(new String[categoriesNotToday.size()]);
		return istodayincluded;
	}
}
