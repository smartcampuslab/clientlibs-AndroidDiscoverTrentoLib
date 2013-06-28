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

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import eu.trentorise.smartcampus.dt.DTParamsHelper;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.android.common.SCGeocoder;
import eu.trentorise.smartcampus.android.feedback.activity.FeedbackFragmentActivity;
import eu.trentorise.smartcampus.android.feedback.utils.FeedbackFragmentInflater;
import eu.trentorise.smartcampus.android.map.InfoDialog;
import eu.trentorise.smartcampus.dt.custom.data.Constants;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
import eu.trentorise.smartcampus.dt.custom.map.MapManager;

public class AddressSelectActivity extends FeedbackFragmentActivity {
	
	public final static int RESULT_SELECTED = 10;

	protected static final String ARG_POINT = "point";
	
	private MapView mapView = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContent();
		FeedbackFragmentInflater.inflateHandleButtonInRelativeLayout(this,
				(RelativeLayout) findViewById(R.id.mapcontainer_relativelayout_dt));
	}

//	@Override
//	public boolean onPrepareOptionsMenu(Menu menu) {
//		MenuItem item = menu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_addpoi,
//				1, R.string.menu_item_addpoi_text);
//		item.setIcon(R.drawable.ic_addp);
//		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//		return true;
//	}

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

		GeoPoint me = null;
		Address address = (Address) getIntent().getParcelableExtra(ARG_POINT);
		if (address != null) {
			me = new GeoPoint((int)(address.getLatitude()*1E6), (int)(address.getLongitude() * 1E6));
			mapView.getController().setZoom(18);
		} else {
			// TODO uncomment for final version
			//me = MapManager.requestMyLocation(this);
//			me = MapManager.DEFAULT_POINT;
		}
		if (me == null) {
			me = new GeoPoint((int) (46.0696727540531 * 1E6), (int) (11.1212700605392 * 1E6));
		}
		mapView.getController().animateTo(me);

		TapOverlay mapOverlay = new TapOverlay();
		List<Overlay> listOfOverlays = mapView.getOverlays();
		listOfOverlays.add(mapOverlay);
		
		Toast.makeText(this, getString(R.string.address_select_toast), Toast.LENGTH_LONG).show();
	}

//	@Override
//	protected boolean isRouteDisplayed() {
//		return false;
//	}

	private Timer timer = new Timer();
	private TimerTask task = null;
	
	private class TapOverlay extends Overlay {

		@Override
		public boolean onTouchEvent(MotionEvent event, MapView mapView) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (task != null) task.cancel();
				task = new TimerTask() {
					
					@Override
					public void run() {
						Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
						vibrator.vibrate(100);
					}
				};
				timer.schedule(task, 1000);
			}
			if (event.getAction() == MotionEvent.ACTION_UP) {
				if (task != null) task.cancel();
				long duration = event.getEventTime() - event.getDownTime();
				if (duration > 1000) {
					GeoPoint p = mapView.getProjection().fromPixels(
							(int) event.getX(), (int) event.getY());
					List<Address> addresses = new SCGeocoder(mapView.getContext()).findAddressesAsync(p);
					if (addresses != null && !addresses.isEmpty()) {
						Intent data = new Intent();
						new InfoDialog(AddressSelectActivity.this,addresses.get(0)).show(getSupportFragmentManager(), "me");

					}
				}
			}

			return false;
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

}
