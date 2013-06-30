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
import java.util.Locale;

import android.location.Address;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.maps.GeoPoint;

import eu.trentorise.smartcampus.android.common.SCGeocoder;
import eu.trentorise.smartcampus.android.feedback.activity.FeedbackFragmentActivity;
import eu.trentorise.smartcampus.android.feedback.utils.FeedbackFragmentInflater;
import eu.trentorise.smartcampus.android.map.InfoDialog;
import eu.trentorise.smartcampus.dt.DTParamsHelper;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
import eu.trentorise.smartcampus.dt.custom.map.MapManager;

public class AddressSelectActivity extends FeedbackFragmentActivity implements OnMapLongClickListener {

	public final static int RESULT_SELECTED = 10;
	protected static final String ARG_POINT = "point";

	private GoogleMap mMap = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mapcontainer_dt_v2);

		getSupportActionBar().setDisplayUseLogoEnabled(true); // system logo
		getSupportActionBar().setDisplayShowTitleEnabled(true); // system title
		getSupportActionBar().setDisplayShowHomeEnabled(false); // home icon bar
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		if (getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		}

		mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		mMap.setOnMapLongClickListener(this);
		mMap.setMyLocationEnabled(true);

		LatLng me = null;
		Address address = (Address) getIntent().getParcelableExtra(ARG_POINT);
		if (address != null) {
			me = new LatLng(address.getLatitude(), address.getLongitude());
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(me, DTParamsHelper.getZoomLevelMap() + 2));
		} else {
			if (DTHelper.getLocationHelper().getLocation() != null) {
				me = new LatLng(DTHelper.getLocationHelper().getLocation().getLatitudeE6() / 1e6, DTHelper.getLocationHelper()
						.getLocation().getLongitudeE6() / 1e6);
			} else {
				me = MapManager.DEFAULT_POINT;
			}
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(me, DTParamsHelper.getZoomLevelMap()));
		}

		Toast.makeText(this, getString(R.string.address_select_toast), Toast.LENGTH_LONG).show();

		FeedbackFragmentInflater.inflateHandleButtonInRelativeLayout(this,
				(RelativeLayout) findViewById(R.id.mapcontainer_relativelayout_dt));
	}

	@Override
	public void onMapLongClick(LatLng point) {
		Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		vibrator.vibrate(100);

		GeoPoint p = new GeoPoint((int) (point.latitude * 1e6), (int) (point.longitude * 1e6));
		List<Address> addresses = new SCGeocoder(getApplicationContext()).findAddressesAsync(p);

		if (addresses != null && !addresses.isEmpty()) {
			new InfoDialog(AddressSelectActivity.this, addresses.get(0)).show(getSupportFragmentManager(), "me");
		} else {
			Address address = new Address(Locale.getDefault());
			address.setLatitude(point.latitude);
			address.setLongitude(point.longitude);
			String addressLine = "LON " + Double.toString(address.getLongitude()) + ", LAT "
					+ Double.toString(address.getLatitude());
			address.setAddressLine(0, addressLine);
			new InfoDialog(AddressSelectActivity.this, addresses.get(0)).show(getSupportFragmentManager(), "me");
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
