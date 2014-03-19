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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.GetChars;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.feedback.utils.FeedbackFragmentInflater;
import eu.trentorise.smartcampus.dt.DTParamsHelper;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.dt.custom.CategoryHelper;
import eu.trentorise.smartcampus.dt.custom.CategoryHelper.CategoryDescriptor;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
import eu.trentorise.smartcampus.dt.custom.map.MapItemsHandler;
import eu.trentorise.smartcampus.dt.custom.map.MapLayerDialogHelper;
import eu.trentorise.smartcampus.dt.custom.map.MapLoadProcessor;
import eu.trentorise.smartcampus.dt.custom.map.MapManager;
import eu.trentorise.smartcampus.dt.custom.map.MapObjectContainer;
import eu.trentorise.smartcampus.dt.fragments.events.EventDetailsFragment;
import eu.trentorise.smartcampus.dt.fragments.pois.PoiDetailsFragment;
import eu.trentorise.smartcampus.dt.fragments.stories.StoryDetailsFragment;
import eu.trentorise.smartcampus.dt.model.LocalEventObject;
import eu.trentorise.smartcampus.dt.notifications.NotificationsSherlockFragmentDT;
import eu.trentorise.smartcampus.osm.android.events.DelayedMapListener;
import eu.trentorise.smartcampus.osm.android.events.MapListener;
import eu.trentorise.smartcampus.osm.android.events.ScrollEvent;
import eu.trentorise.smartcampus.osm.android.events.ZoomEvent;
import eu.trentorise.smartcampus.osm.android.tileprovider.tilesource.TileSourceFactory;
import eu.trentorise.smartcampus.osm.android.util.BoundingBoxE6;
import eu.trentorise.smartcampus.osm.android.util.GeoPoint;
import eu.trentorise.smartcampus.osm.android.views.MapController;
import eu.trentorise.smartcampus.osm.android.views.MapView;
import eu.trentorise.smartcampus.osm.android.views.MapView.Projection;
import eu.trentorise.smartcampus.osm.android.views.overlay.MyLocationOverlay;
import eu.trentorise.smartcampus.osm.android.views.overlay.Overlay;
import eu.trentorise.smartcampus.osm.android.views.overlay.OverlayItem;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;
import eu.trentorise.smartcampus.territoryservice.model.EventObject;
import eu.trentorise.smartcampus.territoryservice.model.POIObject;
import eu.trentorise.smartcampus.territoryservice.model.StoryObject;

@SuppressLint("NewApi")
public class HomeFragment extends NotificationsSherlockFragmentDT implements
		MapItemsHandler, // OnCameraChangeListener, //OnMarkerClickListener,
		MapObjectContainer {

	public static final String ARG_OBJECTS = "objects";
	public static final String ARG_POI_CATEGORY = "poi category";
	public static final String ARG_EVENT_CATEGORY = "event category";

	// metodi istanza mappa google
	// protected GoogleMap mMap;
	// OSM MAP
	// inizializzo view e controller
	public static MapView mapView;
	public static MapController mapController;
	public static MyLocationOverlay myLoc;
	public static Context contextPublic;

	// // dichiarazione array overlayitem
	// Collection<eu.trentorise.smartcampus.osm.android.util.GeoPoint>
	// listGeoPoint = new
	// ArrayList<eu.trentorise.smartcampus.osm.android.util.GeoPoint>();
	// public static ItemizedOverlayWithBubble<ExtendedOverlayItem>
	// markersFindByGeocodingOverlay;

	private String[] poiCategories = null;
	private String[] eventsCategories = null;
	private String[] eventsNotTodayCategories = null;

	// array di overlay per essere printati sulla mappa
	protected ArrayList<OverlayObject> objects;

	protected Drawable mMarker;

	private boolean loaded = false;
	private int zoom = 12;
	// private int[] location;
	// private double lat = 46.0793;
	// private double lon = 11.1302;

	private int z;

	@SuppressWarnings("deprecation")
	public void addLocationAndCompass() {
		myLoc = new MyLocationOverlay(getActivity(), mapView);
		myLoc.enableMyLocation();
		myLoc.enableCompass();
		mapView.getOverlays().add(myLoc);
	}

	@Override
	public void onStart() {
		super.onStart();

		// hide keyboard if it is still open
		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(
				getActivity().findViewById(android.R.id.content)
						.getWindowToken(), 0);

		FeedbackFragmentInflater.inflateHandleButton(getSherlockActivity(),
				getView());
		if (!loaded) {
			String key = getString(R.string.view_intent_arg_object_id);
			if (getActivity().getIntent() != null
					&& getActivity().getIntent().hasExtra(key)) {
				new SCAsyncTask<Void, Void, BaseDTObject>(getActivity(),
						new LoadDataProcessor(getActivity())).execute();
				eventsCategories = null;
				poiCategories = null;
			} else {
				initView();
			}
			loaded = true;
		}
		Bundle bundle = this.getArguments();

		
		if (bundle != null) {
			// se l'elemento dell'oggetto è diverso da null lo leggo
			List<BaseDTObject> lista_oggetti = (List<BaseDTObject>) bundle
					.getSerializable(ARG_OBJECTS);

			Iterator<? extends BaseDTObject> iter = lista_oggetti.iterator();
			ArrayList<OverlayObject> markerResult = new ArrayList<OverlayObject>();
			while (iter.hasNext()) {
				BaseDTObject marker = iter.next();
				eu.trentorise.smartcampus.osm.android.util.GeoPoint p = new eu.trentorise.smartcampus.osm.android.util.GeoPoint(
						marker.getLocation()[0], marker.getLocation()[1]);
				OverlayObject o = new OverlayObject(marker.getTitle(),
						marker.getDescription(), p);
				o.setData(marker);
				markerResult.add(o);
			}
			this.objects = markerResult;
			BoundingBoxE6 boundingBox = new BoundingBoxE6(460793, 111302, 460793, 111302);
			mapView.setBoundingBox(boundingBox);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.my_map_activity, container, false);
		mapView = (MapView) v.findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.setMultiTouchControls(true);
		mapView.setFocusable(true);
		mapView.setFocusableInTouchMode(true);
		mapView.setTileSource(TileSourceFactory.MAPQUESTOSM);
		mapController = mapView.getController();

		addLocationAndCompass();

		int lat = loadcoordinate("coordinatelat");
		int lon = loadcoordinate("coordinatelon");

		Double latitude = (double) lat / 1000000;
		Double longitude = (double) lon / 1000000;
		// Toast toast = Toast.makeText(getActivity(),"lat: " +latitude
		// +"lon: "+ longitude, 100);
		// toast.show();

		int zoomlevel = loadzoom("zoomlevel");
		mapController.setZoom(zoomlevel);
		mapController.animateTo(latitude, longitude);

		// mapView.setBoundingBox(loadBound());

		mapView.setMapListener(new DelayedMapListener(new MapListener() {
			public boolean onZoom(final ZoomEvent e) {
				render(objects);
				return true;
			}

			public boolean onScroll(final ScrollEvent e) {

				return true;
			}
		}, 300));
		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("loaded", loaded);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CategoryDescriptor[] eventsDefault = DTParamsHelper
				.getDefaultArrayByParams(CategoryHelper.CATEGORY_TYPE_EVENTS);
		if (eventsDefault != null) {
			List<String> eventCategory = new ArrayList<String>();
			for (CategoryDescriptor event : eventsDefault)
				eventCategory.add(event.category);
			eventsCategories = Arrays.asList(eventCategory.toArray()).toArray(
					new String[eventCategory.toArray().length]);
		}
		CategoryDescriptor[] poisDefault = DTParamsHelper
				.getDefaultArrayByParams(CategoryHelper.CATEGORY_TYPE_POIS);
		if (poisDefault != null) {
			List<String> poisCategory = new ArrayList<String>();
			for (CategoryDescriptor poi : poisDefault)
				poisCategory.add(poi.category);
			poiCategories = Arrays.asList(poisCategory.toArray()).toArray(
					new String[poisCategory.toArray().length]);
		}
		setHasOptionsMenu(true);

		GeoPoint p = new GeoPoint(46.0793, 11.1302);
		savecoordinate(p);
		savezoom(12);
		contextPublic = getActivity();
	}

	/**
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected void initView() {
		if (getSupportMap() != null) {
			// getSupportMap().getOverlayManager().clear();
			// trovare metodi all'interno delle librerie simili
			// getSupportMap().clear();
			// getSupportMap().getUiSettings().setRotateGesturesEnabled(false);
			// getSupportMap().getUiSettings().setTiltGesturesEnabled(false);
		}

		if (getArguments() != null && getArguments().containsKey(ARG_OBJECTS)) {
			poiCategories = null;
			eventsCategories = null;
			List<BaseDTObject> list = (List<BaseDTObject>) getArguments()
					.getSerializable(ARG_OBJECTS);
			new AsyncTask<List<BaseDTObject>, Void, List<BaseDTObject>>() {
				@Override
				protected List<BaseDTObject> doInBackground(
						List<BaseDTObject>... params) {
					return params[0];
				}

				@Override
				protected void onPostExecute(List<BaseDTObject> result) {
					addObjects(result);
				}
			}.execute(list);
		} else if (getArguments() != null
				&& getArguments().containsKey(ARG_POI_CATEGORY)) {
			eventsCategories = null;
			setPOICategoriesToLoad(getArguments().getString(ARG_POI_CATEGORY));
		} else if (getArguments() != null
				&& getArguments().containsKey(ARG_EVENT_CATEGORY)) {
			poiCategories = null;
			setEventCategoriesToLoad(getArguments().getString(
					ARG_EVENT_CATEGORY));
		} else {
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

		if (getSupportMap() != null && objects != null) {

			// render(objects);
			// eu.trentorise.smartcampus.osm.android.util.ClusteringHelper.render(mapView,objects);

			// mapView.addMarkers(objects);
			// mapView.getOverlayManager().clear();
			// ArrayList<OverlayItem> over = objects;
			// MyOwnItemizedOverlay overlay = new
			// MyOwnItemizedOverlay(getSupportMap().getContext(),objects,
			// getActivity().getSupportFragmentManager());
			// mapView.getOverlays().add(overlay);

			// initView();

			// addLocationAndCompass();
			// render(objects);
			// mapView.setBuiltInZoomControls(true);
			// mapView.setMultiTouchControls(true);
			// mapView.setFocusable(true);
			// mapView.setFocusableInTouchMode(true);

			List<BaseDTObject> oggetti = new ArrayList<BaseDTObject>();
			for (int i = 0; i < objects.size(); i++) {
				oggetti.add(objects.get(i).getData());
			}

			if (MyOwnItemizedOverlay.dirtyObjects != null) {

				boolean found = false;
				Set<String> deleted = DTHelper
						.findDeleted(MyOwnItemizedOverlay.dirtyObjects);
				for (Iterator<? extends BaseDTObject> iterator = oggetti
						.iterator(); iterator.hasNext();) {
					BaseDTObject o = iterator.next();
					if (deleted.contains(o.getId())) {
						found = true;
						iterator.remove();
					}
				}
				if (found || objects != null) {
					// mapView.getOverlayManager().clear();
					BoundingBoxE6 boundingBox = loadBound();
					mapView.setBoundingBox(boundingBox);
					render(objects);
					MyOwnItemizedOverlay.dirtyObjects = null;
				}
			}
			if (objects != null) {
				BoundingBoxE6 boundingBox = loadBound();
				mapView.setBoundingBox(boundingBox);

				render(objects);
				// MapManager.fitMapWithOverlay(objects);
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

			// salvo lo stato dello zoom in Share Preference
			savezoom(mapView.getZoomLevel());

			GeoPoint point = mapView.getProjection().getBoundingBox()
					.getCenter();
			int y = mapView.getProjection().getScreenRect().centerY();
			savecoordinate(point);

			mapView.setBuiltInZoomControls(false);
			mapView.setMultiTouchControls(false);
			mapView.setFocusable(false);
			mapView.setFocusableInTouchMode(false);
			BoundingBoxE6 bound = mapView.getBoundingBox();
			saveBound(bound.getLatNorthE6(), bound.getLonEastE6(),
					bound.getLatSouthE6(), bound.getLonWestE6());
			
			
			//HOTFIX per fixaggio maptile per il caricamento della mappe e outofmemory
			mapView.getTileProvider().clearTileCache();
			System.gc();
			
			// Toast toast = Toast.makeText(getActivity(),
			// point.getLatitudeE6()+ " "+ point.getLongitudeE6() , 100);
			// toast.show();

			// savecoordinate(mapView.getProjection().
			// trovare metodi all'interno delle librerie simili
			// getSupportMap().setMyLocationEnabled(false);
			// getSupportMap().setOnCameraChangeListener(null);
			// getSupportMap().setOnMarkerClickListener(null);
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {

		menu.clear();
		MenuItem item = menu.add(Menu.CATEGORY_SYSTEM,
				R.id.menu_item_show_places_layers, 1,
				R.string.menu_item__places_layers_text);
		item.setIcon(R.drawable.ic_menu_pois);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		item = menu.add(Menu.CATEGORY_SYSTEM,
				R.id.menu_item_show_events_layers, 1,
				R.string.menu_item__events_layers_text);
		item.setIcon(R.drawable.ic_menu_events);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_item_show_places_layers) {
			MapLayerDialogHelper.createPOIDialog(getActivity(), this,
					getString(R.string.layers_title_places), poiCategories)
					.show();
			return true;
		} else if (item.getItemId() == R.id.menu_item_show_events_layers) {
			Dialog eventsDialog = MapLayerDialogHelper.createEventsDialog(
					getActivity(), this,
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

		new SCAsyncTask<Void, Void, Collection<? extends BaseDTObject>>(
				getActivity(), new MapLoadProcessor(getActivity(), this,
						getSupportMap()) {
					@Override
					protected Collection<? extends BaseDTObject> getObjects() {
						try {
							/*
							 * check if todays is checked and cat with
							 * searchTodayEvents
							 */
							return DTHelper.getPOIByCategory(0, -1, categories);
						} catch (Exception e) {
							e.printStackTrace();
							return Collections.emptyList();
						}
					}

				}).execute();
	}

	// private void onBaseDTObjectTap(BaseDTObject o) {
	// Bundle args = new Bundle();
	// args.putSerializable(InfoDialog.PARAM, o);
	// InfoDialog dtoTap = new InfoDialog();
	// dtoTap.setArguments(args);
	// dtoTap.show(getActivity().getSupportFragmentManager(), "me");
	// }
	//
	// private void onBaseDTObjectsTap(List<BaseDTObject> list) {
	// if (list == null || list.size() == 0)
	// return;
	// if (list.size() == 1) {
	// onBaseDTObjectTap(list.get(0));
	// return;
	// }
	// FragmentTransaction fragmentTransaction = getFragmentManager()
	// .beginTransaction();
	// SherlockFragment fragment = null;
	// Bundle args = new Bundle();
	// if (list.get(0) instanceof LocalEventObject) {
	// fragment = new EventsListingFragment();
	// args.putSerializable(SearchFragment.ARG_LIST, new ArrayList(list));
	// } else if (list.get(0) instanceof POIObject) {
	// fragment = new PoisListingFragment();
	// args.putSerializable(SearchFragment.ARG_LIST, new ArrayList(list));
	// }
	// if (fragment != null) {
	// fragment.setArguments(args);
	// fragmentTransaction
	// .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
	// fragmentTransaction.detach(this);
	// fragmentTransaction.replace(android.R.id.content, fragment, "me");
	// fragmentTransaction.addToBackStack(fragment.getTag());
	// fragmentTransaction.commit();
	// }
	// }

	@Override
	public void setEventCategoriesToLoad(final String... categories) {
		this.eventsCategories = categories;
		this.eventsNotTodayCategories = categories;

		/* actually only event or poi at the same time */
		this.poiCategories = null;

		// mItemizedoverlay.clearMarkers();

		new SCAsyncTask<Void, Void, Collection<? extends BaseDTObject>>(
				getActivity(), new MapLoadProcessor(getActivity(), this,
						getSupportMap()) {
					@Override
					protected Collection<? extends BaseDTObject> getObjects() {
						try {
							/*
							 * check if todays is checked and cat with
							 * searchTodayEvents
							 */

							if (isTodayIncluded()) {
								List<LocalEventObject> newList = new ArrayList<LocalEventObject>();
								newList.addAll(DTHelper.searchTodayEvents(0,
										-1, ""));
								if (categories != null)
									newList.addAll(DTHelper
											.getEventsByCategories(0, -1,
													eventsNotTodayCategories));
								return newList;
							} else
								return DTHelper.getEventsByCategories(0, -1,
										categories);
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
		eventsNotTodayCategories = categoriesNotToday
				.toArray(new String[categoriesNotToday.size()]);
		return istodayincluded;
	}

	private MapView getSupportMap() {
		if (mapView == null) {
			if (getFragmentManager().findFragmentById(android.R.id.content) != null)
				// capire implemetazione SupportMapFragment per OSM MAP
				// && getFragmentManager().findFragmentById(
				// android.R.id.content) instanceof SupportMapFragment)

				mapView = (MapView) (getFragmentManager()
						.findFragmentById(android.R.id.content)).getView();

			// fix
			// if (mapView != null)
			// mapView.moveCamera(CameraUpdateFactory.newLatLngZoom(
			// MapManager.DEFAULT_POINT, MapManager.ZOOM_DEFAULT));
			if (mapView != null) {
				mapController = mapView.getController();
				mapController.setZoom(MapManager.ZOOM_DEFAULT);
				mapController.animateTo(MapManager.DEFAULT_POINT);
			}

		}
		return mapView;
	}

	// public boolean onMarkerClick(OverlayItem marker) {
	// List<OverlayItem> list = MapManager.ClusteringHelper
	// .getFromGridId(marker.getTitle());
	// if (list == null || list.isEmpty())
	// return true;
	//
	// List<BaseDTObject> listDT= new ArrayList<BaseDTObject>();
	//
	// for (int i = 0; i < list.size(); i++) {
	// BaseDTObject object = new BaseDTObject();
	// object.setDescription(list.get(i).getSnippet());
	// object.setTitle(list.get(i).getTitle());
	// double[] point = new double[2];
	// point[0]=list.get(i).getPoint().getLatitudeE6();
	// point[1]=list.get(i).getPoint().getLongitudeE6();
	// object.setLocation(point);
	// listDT.add(object);
	// }
	//
	//
	// if (list.size() == 1) {
	// onBaseDTObjectTap(listDT.get(0));
	// } else if (getSupportMap().getZoomLevel() ==
	// getSupportMap().getMaxZoomLevel()) {
	// onBaseDTObjectsTap(listDT);
	// } else {
	// MapManager.fitMapWithOverlays(listDT, getSupportMap());
	// }
	//
	// return true;
	// }

	// @Override
	// public void onCameraChange(CameraPosition position) {
	// //Sistemare REnder in MapManager
	// render(objects);
	// }

	@Override
	public <T extends BaseDTObject> void addObjects(
			Collection<? extends BaseDTObject> objects) {

		// data = (ArrayList<BaseDTObject>) objects;
		// sistemare metodi all'interno di MapManager
		// Iterator<BaseDTObject> iter = objects.iterator();

		Iterator<? extends BaseDTObject> iter = objects.iterator();
		List<OverlayObject> markerResult = new ArrayList<OverlayObject>();
		while (iter.hasNext()) {
			BaseDTObject marker = iter.next();
			eu.trentorise.smartcampus.osm.android.util.GeoPoint p = new eu.trentorise.smartcampus.osm.android.util.GeoPoint(
					marker.getLocation()[0], marker.getLocation()[1]);
			OverlayObject o = new OverlayObject(marker.getTitle(),
					marker.getDescription(), p);
			o.setData(marker);
			markerResult.add(o);
		}

		if (getSupportMap() != null) {
			this.objects = (ArrayList<OverlayObject>) markerResult;
			render(markerResult);

			// mapView.addMarkers(this.objects);
			// sistemare metodi fit in MapManager
			// MapManager.fitMapWithOverlays(objects, getSupportMap());
		}
	}

	private void render(Collection<? extends OverlayItem> objects) {

		if (getSupportMap() != null) {

			if (objects != null && getSherlockActivity() != null) {
				List<OverlayItem> cluster = MapManager.ClusteringHelper
						.cluster(getSherlockActivity().getApplicationContext(),
								getSupportMap(), objects);
				MapManager.ClusteringHelper.render(getSupportMap(), cluster,
						getActivity().getSupportFragmentManager(),
						getActivity());
			}
		}
		// getSupportMap().getOverlays().clear();
		//
		//
		//
		//
		// if (objects != null && getSherlockActivity() != null) {
		// List<OverlayItem> cluster = MapManager.ClusteringHelper
		// .cluster(getSherlockActivity().getApplicationContext(),
		// getSupportMap(), objects);
		// MapManager.ClusteringHelper.render(getSupportMap(), cluster);
		// }
	}

	private class LoadDataProcessor extends
			AbstractAsyncTaskProcessor<Void, BaseDTObject> {

		public LoadDataProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public BaseDTObject performAction(Void... params)
				throws SecurityException, Exception {
			String entityId = getActivity().getIntent().getStringExtra(
					getString(R.string.view_intent_arg_object_id));
			String type = getActivity().getIntent().getStringExtra(
					getString(R.string.view_intent_arg_entity_type));

			if (entityId != null && type != null) {
				if ("event".equals(type))
					return DTHelper.findEventByEntityId(entityId)
							.getObjectForBean();
				else if ("location".equals(type))
					return DTHelper.findPOIByEntityId(entityId)
							.getObjectForBean();
				else if ("narrative".equals(type))
					return DTHelper.findStoryByEntityId(entityId)
							.getObjectForBean();
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
					Toast.makeText(getActivity(),
							R.string.app_failure_obj_not_found,
							Toast.LENGTH_LONG).show();
					return;
				}

				SherlockFragment fragment = null;
				Bundle args = new Bundle();
				if (result instanceof POIObject) {
					fragment = new PoiDetailsFragment();
					args.putString(PoiDetailsFragment.ARG_POI_ID,
							result.getId());
				} else if (result instanceof EventObject) {
					fragment = new EventDetailsFragment();
					args.putString(EventDetailsFragment.ARG_EVENT_ID,
							(result.getId()));
				} else if (result instanceof StoryObject) {
					fragment = new StoryDetailsFragment();
					args.putString(StoryDetailsFragment.ARG_STORY_ID,
							result.getId());
				}
				if (fragment != null) {
					FragmentTransaction fragmentTransaction = getActivity()
							.getSupportFragmentManager().beginTransaction();
					fragment.setArguments(args);

					fragmentTransaction
							.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					fragmentTransaction.replace(android.R.id.content, fragment,
							"me");
					fragmentTransaction.addToBackStack(fragment.getTag());
					fragmentTransaction.commit();
				}
			}
		}

	}

	private void savezoom(final int zoomlevel) {
		SharedPreferences sharedPreferences = getSherlockActivity()
				.getSharedPreferences("zoomlevel", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt("zoomlevel", zoomlevel);
		editor.commit();
	}

	public int loadzoom(final String zoomlevel) {
		SharedPreferences sharedPreferences = getSherlockActivity()
				.getSharedPreferences("zoomlevel", Context.MODE_PRIVATE);
		return sharedPreferences.getInt(zoomlevel, 12);
	}

	private void savecoordinate(final GeoPoint coordinate) {
		SharedPreferences sharedPreferences = getSherlockActivity()
				.getSharedPreferences("coordinate", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putInt("coordinatelat", coordinate.getLatitudeE6());
		editor.putInt("coordinatelon", coordinate.getLongitudeE6());
		editor.commit();
	}

	public int loadcoordinate(final String coordinate) {
		SharedPreferences sharedPreferences = getSherlockActivity()
				.getSharedPreferences("coordinate", Context.MODE_PRIVATE);

		if (coordinate == "coordinatelat") {
			return sharedPreferences.getInt(coordinate, 46069672);
		} else if (coordinate == "coordinatelon") {
			return sharedPreferences.getInt(coordinate, 11121270);
		}
		return 1;
	}

	private void saveBound(final int n, final int e, final int s, final int w) {
		SharedPreferences sharedPreferences = getSherlockActivity()
				.getSharedPreferences("bound", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putInt("0", n);
		editor.putInt("1", e);
		editor.putInt("2", s);
		editor.putInt("3", w);

		editor.commit();
	}

	private BoundingBoxE6 loadBound() {
		SharedPreferences sharedPreferences = getSherlockActivity()
				.getSharedPreferences("bound", Context.MODE_PRIVATE);

		int n, e, s, w;

		n = sharedPreferences.getInt("0", 460793);
		e = sharedPreferences.getInt("1", 111302);
		s = sharedPreferences.getInt("2", 460793);
		w = sharedPreferences.getInt("3", 111302);

		BoundingBoxE6 bound = new BoundingBoxE6(n, e, s, w);
		return bound;
	}

}
