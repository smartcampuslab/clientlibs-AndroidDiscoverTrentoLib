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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

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
import eu.trentorise.smartcampus.dt.fragments.events.ConfirmPoiDialog.OnDetailsClick;
import eu.trentorise.smartcampus.dt.model.BaseDTObject;
import eu.trentorise.smartcampus.dt.model.EventObject;
import eu.trentorise.smartcampus.dt.model.POIObject;




public class POISelectActivity extends FeedbackFragmentActivity implements MapItemsHandler, BaseDTObjectMapItemTapListener,OnDetailsClick {

	public final static int RESULT_SELECTED = 11;

	private MapView mapView = null;
	DTItemizedOverlay mItemizedoverlay = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContent();
		FeedbackFragmentInflater.inflateHandleButtonInRelativeLayout(this,
				(RelativeLayout) findViewById(R.id.mapcontainer_relativelayout_dt));
	}

	@Override
	protected void onStart() {
		super.onStart();
		MapLayerDialogHelper.createPOIDialog(this, this, getString(R.string.select_poi_title), (String[]) null).show();
		// LayerDialogFragment dialogFragment = new LayerDialogFragment(this);
		// Bundle args = new Bundle();
		// args.putString(LayerDialogFragment.ARG_TITLE,
		// getString(R.string.select_poi_title));
		// dialogFragment.setArguments(args);
		// dialogFragment.show(getSupportFragmentManager(), "dialog");
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
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item = menu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_show_places_layers, 1,
				R.string.menu_item__places_layers_text);
		item.setIcon(R.drawable.ic_menu_layers);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		super.onPrepareOptionsMenu(menu);
		return true;
	}

	private void setContent() {
		mapView = new MapView(this, getResources().getString(R.string.maps_api_key));
		setContentView(R.layout.mapcontainer_dt);

		ViewGroup view = (ViewGroup) findViewById(R.id.mapcontainer);
		view.addView(mapView);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayUseLogoEnabled(true); // system logo
		actionBar.setDisplayShowTitleEnabled(true); // system title
		actionBar.setDisplayShowHomeEnabled(false); // home icon bar
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD); // tabs
																			// bar

		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(true);
		mapView.getController().setZoom(15);
		// TODO correct for final version
		// GeoPoint me = MapManager.requestMyLocation(this);
		// if (me == null) {
		// me = new GeoPoint((int) (46.0696727540531 * 1E6), (int)
		// (11.1212700605392 * 1E6));
		// }
//		mapView.getController().animateTo(MapManager.DEFAULT_POINT);
		List<Overlay> listOfOverlays = mapView.getOverlays();

		mItemizedoverlay = new DTItemizedOverlay(this, mapView);
		mItemizedoverlay.setMapItemTapListener(this);
		listOfOverlays.add(mItemizedoverlay);
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
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_item_show_places_layers) {
			MapLayerDialogHelper.createPOIDialog(this, this, getString(R.string.select_poi_title), (String[]) null).show();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	public void setPOICategoriesToLoad(final String... categories) {
		mItemizedoverlay.clearMarkers();

//		new SCAsyncTask<Void, Void, Collection<? extends BaseDTObject>>(this, new MapLoadProcessor(this, mItemizedoverlay,
//				mapView) {
//			@Override
//			protected Collection<? extends BaseDTObject> getObjects() {
//				try {
//					return DTHelper.getPOIByCategory(0, -1, categories); // TODO
//				} catch (Exception e) {
//					e.printStackTrace();
//					return Collections.emptyList();
//				}
//			}
//		}).execute();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
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
		// TODO Auto-generated method stub

	}
}
