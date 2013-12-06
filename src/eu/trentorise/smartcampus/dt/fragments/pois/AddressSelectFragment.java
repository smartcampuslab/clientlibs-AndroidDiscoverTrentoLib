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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import eu.trentorise.smartcampus.dt.notifications.NotificationsSherlockFragmentDT;
import eu.trentorise.smartcampus.dt.notifications.NotificationsSherlockMapFragmentDT;

public class AddressSelectFragment extends NotificationsSherlockMapFragmentDT implements OnMapLongClickListener {

	public final static int RESULT_SELECTED = 10;
	protected static final String ARG_POINT = "point";

	private GoogleMap mMap = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);

//		View view = inflater.inflate(R.layout.address_select, container, false);
//		return view;
	}

	@Override
	public void onStart() {

	//		getSherlockActivity().getSupportActionBar().setDisplayUseLogoEnabled(true); // system
	//																					// logo
	//		getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(true); // system
	//																						// title
	//		getSherlockActivity().getSupportActionBar().setDisplayShowHomeEnabled(false); // home
	//																						// icon
	//																						// bar
	//		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	//		if (getSherlockActivity().getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
	//			getSherlockActivity().getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	//		}
		if (getSupportMap() != null) {

			getSupportMap().setOnMapLongClickListener(this);
			getSupportMap().setMyLocationEnabled(true);

			LatLng me = null;
			Address address = (Address) getSherlockActivity().getIntent().getParcelableExtra(ARG_POINT);
			if (address != null) {
				me = new LatLng(address.getLatitude(), address.getLongitude());
				getSupportMap().moveCamera(CameraUpdateFactory.newLatLngZoom(me, DTParamsHelper.getZoomLevelMap() + 2));
			} else {
				if (DTHelper.getLocationHelper().getLocation() != null) {
					me = new LatLng(DTHelper.getLocationHelper().getLocation().getLatitudeE6() / 1e6, DTHelper
							.getLocationHelper().getLocation().getLongitudeE6() / 1e6);
				} else {
					me = MapManager.DEFAULT_POINT;
				}
				getSupportMap().moveCamera(CameraUpdateFactory.newLatLngZoom(me, DTParamsHelper.getZoomLevelMap()));
			}

			Toast.makeText(getSherlockActivity(), getString(R.string.address_select_toast), Toast.LENGTH_LONG).show();

		}
		super.onStart();
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
	public void onMapLongClick(LatLng point) {
		Vibrator vibrator = (Vibrator) getSherlockActivity().getSystemService(getSherlockActivity().VIBRATOR_SERVICE);
		vibrator.vibrate(100);

		GeoPoint p = new GeoPoint((int) (point.latitude * 1e6), (int) (point.longitude * 1e6));
		List<Address> addresses = new SCGeocoder(getSherlockActivity().getApplicationContext()).findAddressesAsync(p);

		if (addresses != null && !addresses.isEmpty()) {
			new InfoDialog(getSherlockActivity(), addresses.get(0)).show(getSherlockActivity()
					.getSupportFragmentManager(), "me");
		} else {
			Address address = new Address(Locale.getDefault());
			address.setLatitude(point.latitude);
			address.setLongitude(point.longitude);
			String addressLine = "LON " + Double.toString(address.getLongitude()) + ", LAT "
					+ Double.toString(address.getLatitude());
			address.setAddressLine(0, addressLine);

			if (addresses != null && !addresses.isEmpty()) // Fixed bug if you
															// select a place
															// without addresses
															// (happens when you
															// are without
															// connection)
															// must to manage
															// the dialog if we
															// have only lat
															// long
				new InfoDialog(getSherlockActivity(), addresses.get(0)).show(getSherlockActivity()
						.getSupportFragmentManager(), "me");
			else
				Toast.makeText(getSherlockActivity(), getString(R.string.app_failure_localization), Toast.LENGTH_LONG)
						.show();
		}
	}

	// @Override
	// public String getAppToken() {
	// return DTParamsHelper.getAppToken();
	// }
	//
	// @Override
	// public String getAuthToken() {
	// return DTHelper.getAuthToken();
	// }

}
