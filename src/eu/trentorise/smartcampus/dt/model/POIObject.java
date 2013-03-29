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
package eu.trentorise.smartcampus.dt.model;

import java.util.Locale;

import android.location.Address;

public class POIObject extends BaseDTObject {
	private static final long serialVersionUID = 3377022799304541031L;
	
	private POIData poi;

	public POIObject() {
		super();
	}
	public POIObject(String id,double lat, double longit) {
		super();
		double[] a={lat, longit};
		super.setLocation(a);
		super.setId(id);
		poi = new POIData();
		
	}

	public POIObject(String id) {
		super();
		poi.setPoiId(id);
	}
	public POIData getPoi() {
		return poi;
	}

	public void setPoi(POIData poi) {
		this.poi = poi;
	}

	@Override
	public double[] getLocation() {
		if (super.getLocation() != null) return super.getLocation();
		if (getPoi() != null) return new double[]{getPoi().getLatitude(),getPoi().getLongitude()};
		return null;
	}
	
	public String shortAddress() {
		return getTitle() + (getPoi().getStreet()==null || getPoi().getStreet().length()==0? "": (", "+getPoi().getStreet()));
	}
	
	public Address asGoogleAddress() {
		Address a = new Address(Locale.getDefault());
		a.setLatitude(getLocation()[0]);
		a.setLongitude(getLocation()[1]);
		a.setAddressLine(0, getPoi().getStreet());
		a.setCountryCode(getPoi().getCountry());
		a.setCountryName(getPoi().getState());
		a.setLocality(getPoi().getCity());
		a.setPostalCode(getPoi().getPostalCode());
		a.setAdminArea(getPoi().getRegion());
		return a;
	}
}
