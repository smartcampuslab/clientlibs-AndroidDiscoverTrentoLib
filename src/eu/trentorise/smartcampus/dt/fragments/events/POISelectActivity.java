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
import java.util.List;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.feedback.activity.FeedbackFragmentActivity;
import eu.trentorise.smartcampus.android.feedback.utils.FeedbackFragmentInflater;
import eu.trentorise.smartcampus.dt.DTParamsHelper;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
import eu.trentorise.smartcampus.dt.custom.map.BaseDTObjectMapItemTapListener;
import eu.trentorise.smartcampus.dt.custom.map.DTItemizedOverlay;
import eu.trentorise.smartcampus.dt.custom.map.MapItemsHandler;
import eu.trentorise.smartcampus.dt.custom.map.MapLayerDialogHelper;
import eu.trentorise.smartcampus.dt.custom.map.MapLoadProcessor;
import eu.trentorise.smartcampus.dt.custom.map.MapManager;
import eu.trentorise.smartcampus.dt.custom.map.MapObjectContainer;
import eu.trentorise.smartcampus.dt.fragments.events.ConfirmPoiDialog.OnDetailsClick;
import eu.trentorise.smartcampus.dt.model.BaseDTObject;

public class POISelectActivity extends FeedbackFragmentActivity implements MapItemsHandler, BaseDTObjectMapItemTapListener,
		OnDetailsClick, OnMarkerClickListener, MapObjectContainer, OnCameraChangeListener {

	public final static int RESULT_SELECTED = 11;

	private GoogleMap mMap = null;
	DTItemizedOverlay mItemizedoverlay = null;

	private Collection<? extends BaseDTObject> objects;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContent();
	}

	@Override
	protected void onStart() {
		super.onStart();

		FeedbackFragmentInflater.inflateHandleButtonInRelativeLayout(this,
				(RelativeLayout) findViewById(R.id.mapcontainer_relativelayout_dt));

		MapLayerDialogHelper.createPOIDialog(this, this, getString(R.string.select_poi_title), (String[]) null).show();
		// LayerDialogFragment dialogFragment = new LayerDialogFragment(this);
		// Bundle args = new Bundle();
		// args.putString(LayerDialogFragment.ARG_TITLE,
		// getString(R.string.select_poi_title));
		// dialogFragment.setArguments(args);
		// dialogFragment.show(getSupportFragmentManager(), "dialog");
	}

	private void setContent() {
		setContentView(R.layout.mapcontainer_dt_v2);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayUseLogoEnabled(true); // system logo
		actionBar.setDisplayShowTitleEnabled(true); // system title
		actionBar.setDisplayShowHomeEnabled(false); // home icon bar
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD); // tabs
																			// bar
		if (((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap() != null) {

			mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
			mMap.setOnMarkerClickListener(this);
			mMap.setMyLocationEnabled(true);

			mMap.getUiSettings().setRotateGesturesEnabled(false);
			mMap.getUiSettings().setTiltGesturesEnabled(false);

			LatLng me = null;
			if (DTHelper.getLocationHelper().getLocation() != null) {
				me = new LatLng(DTHelper.getLocationHelper().getLocation().getLatitudeE6() / 1e6, DTHelper.getLocationHelper()
						.getLocation().getLongitudeE6() / 1e6);
			} else {
				me = MapManager.DEFAULT_POINT;
			}
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(me, DTParamsHelper.getZoomLevelMap()));
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item = menu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_show_places_layers, 1,
				R.string.menu_item__places_layers_text);
		item.setIcon(R.drawable.ic_menu_layers);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		super.onPrepareOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_item_show_places_layers) {
			MapLayerDialogHelper.createPOIDialog(this, this, getString(R.string.select_poi_title), (String[]) null).show();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void OnDialogDetailsClick(BaseDTObject poi) {
		// User clicked OK button
		Intent data = new Intent();
		data.putExtra("poi", poi);
		setResult(RESULT_SELECTED, data);
		finish();
	}

	@Override
	public void onBaseDTObjectTap(BaseDTObject poiObject) {
		ConfirmPoiDialog stopInfoDialog = new ConfirmPoiDialog(this);
		Bundle args = new Bundle();
		args.putSerializable(ConfirmPoiDialog.ARG_POI, poiObject);
		stopInfoDialog.setArguments(args);
		stopInfoDialog.show(getSupportFragmentManager(), "poiselected");
	}

	@Override
	public void onBaseDTObjectsTap(List<BaseDTObject> poiObjectsList) {
		ConfirmPoiDialog poiInfoDialog = new ConfirmPoiDialog(this);
		Bundle args = new Bundle();
		args.putSerializable(ConfirmPoiDialog.ARG_POIS, (ArrayList<BaseDTObject>) poiObjectsList);
		poiInfoDialog.setArguments(args);
		poiInfoDialog.show(getSupportFragmentManager(), "poiselected");
	}

	public void setPOICategoriesToLoad(final String... categories) {
		if (mMap != null) {

			mMap.clear();
			new SCAsyncTask<Void, Void, Collection<? extends BaseDTObject>>(this, new MapLoadProcessor(this, this, mMap) {
				@Override
				protected Collection<? extends BaseDTObject> getObjects() {
					try {
						return DTHelper.getPOIByCategory(0, -1, categories); // TODO
					} catch (Exception e) {
						e.printStackTrace();
						return Collections.emptyList();
					}
				}
			}).execute();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mMap != null) {
			mMap.setMyLocationEnabled(true);
			mMap.setOnCameraChangeListener(this);
			mMap.setOnMarkerClickListener(this);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration arg0) {
		super.onConfigurationChanged(arg0);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mMap != null) {

			mMap.setMyLocationEnabled(false);
			mMap.setOnCameraChangeListener(null);
			mMap.setOnMarkerClickListener(null);
		}
	}

	@Override
	public String getAppToken() {
		return DTParamsHelper.getAppToken();
	}

	@Override
	public String getAuthToken() {
		return DTHelper.getAuthToken();
	}

	@Override
	public void setEventCategoriesToLoad(String... categories) {
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		List<BaseDTObject> list = MapManager.ClusteringHelper.getFromGridId(marker.getTitle());
		if (list == null || list.isEmpty())
			return true;

		if (list.size() == 1) {
			onBaseDTObjectTap(list.get(0));
		} else if (mMap.getCameraPosition().zoom == mMap.getMaxZoomLevel()) {
			onBaseDTObjectsTap(list);
		} else {
			MapManager.fitMapWithOverlays(list, mMap);
		}
		return true;
	}

	@Override
	public void onCameraChange(CameraPosition position) {
		render(objects);
	}

	@Override
	public <T extends BaseDTObject> void addObjects(Collection<? extends BaseDTObject> objects) {
		this.objects = objects;
		render(objects);
	}

	private void render(Collection<? extends BaseDTObject> objects) {
		if (mMap != null) {

			mMap.clear();
			if (objects != null) {
				List<MarkerOptions> cluster = MapManager.ClusteringHelper.cluster(getApplicationContext(), mMap, objects);
				MapManager.ClusteringHelper.render(mMap, cluster);
			}
		}
	}

}
