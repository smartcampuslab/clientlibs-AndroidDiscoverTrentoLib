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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.feedback.activity.FeedbackFragmentActivity;
import eu.trentorise.smartcampus.android.feedback.utils.FeedbackFragmentInflater;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.data.Constants;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
import eu.trentorise.smartcampus.dt.custom.map.BaseDTObjectMapItemTapListener;
import eu.trentorise.smartcampus.dt.custom.map.DTItemizedOverlay;
import eu.trentorise.smartcampus.dt.custom.map.MapItemsHandler;
import eu.trentorise.smartcampus.dt.custom.map.MapLayerDialogHelper;
import eu.trentorise.smartcampus.dt.custom.map.MapLoadProcessor;
import eu.trentorise.smartcampus.dt.custom.map.MapManager;
import eu.trentorise.smartcampus.dt.model.BaseDTObject;
import eu.trentorise.smartcampus.dt.model.EventObject;
import eu.trentorise.smartcampus.dt.model.POIObject;

public class POISelectActivity extends FeedbackFragmentActivity
implements MapItemsHandler, BaseDTObjectMapItemTapListener {
	
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
		MapLayerDialogHelper.createPOIDialog(this, this, getString(R.string.select_poi_title), (String[])null).show();
//		LayerDialogFragment dialogFragment = new LayerDialogFragment(this);
//		Bundle args = new Bundle();
//		args.putString(LayerDialogFragment.ARG_TITLE, getString(R.string.select_poi_title));
//		dialogFragment.setArguments(args);
//		dialogFragment.show(getSupportFragmentManager(), "dialog");
		EasyTracker.getInstance().activityStart(this); // Add this method.
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this); // Add this method.
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item = menu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_show_places_layers,
				1, R.string.menu_item__places_layers_text);
		item.setIcon(R.drawable.ic_layers);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		super.onPrepareOptionsMenu(menu);
		return true;
	}

	private void setContent() {
		mapView = new MapView(this, getResources().getString(R.string.maps_api_key));
		setContentView(R.layout.mapcontainer_dt);

		ViewGroup view = (ViewGroup)findViewById(R.id.mapcontainer);
		view.addView(mapView);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayUseLogoEnabled(true); // system logo
		actionBar.setDisplayShowTitleEnabled(true); // system title
		actionBar.setDisplayShowHomeEnabled(false); // home icon bar
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD); // tabs bar

		
		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(true);
		mapView.getController().setZoom(15);
		// TODO correct for final version
//		GeoPoint me = MapManager.requestMyLocation(this);
//		if (me == null) {
//			me = new GeoPoint((int) (46.0696727540531 * 1E6), (int) (11.1212700605392 * 1E6));
//		}
		mapView.getController().animateTo(MapManager.trento());
		List<Overlay> listOfOverlays = mapView.getOverlays();

		mItemizedoverlay = new DTItemizedOverlay(this, mapView);
		mItemizedoverlay.setMapItemTapListener(this);
		listOfOverlays.add(mItemizedoverlay);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onBaseDTObjectTap(final BaseDTObject o) {
//		//create dialog box for confirm
//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		// Add the buttons
//		builder.setMessage(o.getTitle());
//		builder.setPositiveButton(R.string.ok,
//				new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int id) {
//						// User clicked OK button
//						Intent data = new Intent();
//						data.putExtra("poi", o);
//						setResult(RESULT_SELECTED, data);
//						finish();
//
//					}
//				});
//		builder.setNegativeButton(R.string.cancel,
//				new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int id) {
//						// User cancelled the dialog
//						dialog.dismiss();
//					}
//				});
//
//		// Create the AlertDialog
//		AlertDialog dialog = builder.create();
//		dialog.show();
		new ConfirmPoiDialog(o).show(getSupportFragmentManager(), "me");
	}
	
	@Override
	public void onBaseDTObjectsTap(List<BaseDTObject> list) {
		new ConfirmPoiDialog(list.get(0)).show(getSupportFragmentManager(), "me");
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_item_show_places_layers) {
			MapLayerDialogHelper.createPOIDialog(this, this, getString(R.string.select_poi_title), (String[])null).show();
			//			LayerDialogFragment dialogFragment = new LayerDialogFragment(this);
//			dialogFragment.show(getSupportFragmentManager(), "dialog");
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	
	public void setPOICategoriesToLoad(final String ... categories) {
		mItemizedoverlay.clearMarkers();

		new SCAsyncTask<Void, Void, Collection<? extends BaseDTObject>>(this,
				new MapLoadProcessor(this, mItemizedoverlay, mapView) {
			@Override
			protected Collection<? extends BaseDTObject> getObjects() {
				try {
					return DTHelper.getPOIByCategory(0, -1, categories); //TODO
				} catch (Exception e) {
					e.printStackTrace();
					return Collections.emptyList();
				}
			}
		}).execute();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	@SuppressLint("ValidFragment")
	private class ConfirmPoiDialog extends SherlockDialogFragment {
		private BaseDTObject data;

		
		public ConfirmPoiDialog(BaseDTObject o) {
			this.data = o;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			getDialog().setTitle(data.getTitle());
			return inflater.inflate(R.layout.mapconfirmdialog, container, false);
		}
		
		public BaseDTObject getData(){
			return data;
		}
		@Override
		public void onStart() {
			super.onStart();
			TextView msg = (TextView) getDialog().findViewById(R.id.mapdialog_msg);
			if (data.getDescription() != null)
				msg.setText(data.getDescription());
			else {
				if (data instanceof POIObject)
					msg.setText(((POIObject) data).shortAddress());
				else {
					POIObject poi = DTHelper.findPOIById(((EventObject) data)
							.getPoiId());
					msg.setText(poi.shortAddress());
				}
			}
			Button b = (Button) getDialog().findViewById(R.id.mapdialog_cancel);
			b.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					getDialog().dismiss();
				}
			});
			b = (Button) getDialog().findViewById(R.id.mapdialog_ok);
			b.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// User clicked OK button
					Intent data = new Intent();
					data.putExtra("poi",getData());
					setResult(RESULT_SELECTED, data);
					finish();
					getDialog().dismiss();
				}
			});

		}
	}

	@Override
	public String getAppToken() {
		return Constants.APP_TOKEN;
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
