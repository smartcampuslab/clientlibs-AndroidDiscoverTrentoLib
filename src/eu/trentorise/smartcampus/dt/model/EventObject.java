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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventObject extends BaseDTObject {
	private static final long serialVersionUID = 388550207183035548L;
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

	private String poiId;
	
	boolean fromTimeUserDefined = false;
	boolean toTimeUserDefined = false;
	boolean poiIdUserDefined = false;
	
	private List<String> attending = new ArrayList<String>();
	private Integer attendees = 0;
	
	private POIObject poi = null;
	
	public EventObject() {
		super();
	}

	public String getPoiId() {
		return poiId;
	}

	public void setPoiId(String poiId) {
		this.poiId = poiId;
	}

	public List<String> getAttending() {
		return attending;
	}

	public void setAttending(List<String> attending) {
		this.attending = attending;
	}

	public Integer getAttendees() {
		return attendees;
	}

	public void setAttendees(Integer attendees) {
		this.attendees = attendees;
	}

	public boolean isFromTimeUserDefined() {
		return fromTimeUserDefined;
	}

	public void setFromTimeUserDefined(boolean fromTimeUserDefined) {
		this.fromTimeUserDefined = fromTimeUserDefined;
	}

	public boolean isToTimeUserDefined() {
		return toTimeUserDefined;
	}

	public void setToTimeUserDefined(boolean toTimeUserDefined) {
		this.toTimeUserDefined = toTimeUserDefined;
	}

	public boolean isPoiIdUserDefined() {
		return poiIdUserDefined;
	}

	public void setPoiIdUserDefined(boolean poiIdUserDefined) {
		this.poiIdUserDefined = poiIdUserDefined;
	}

	public CharSequence dateTimeString() {
		return DATE_FORMAT.format(new Date(getFromTime()));
	}

	public POIObject assignedPoi() {
		return poi;
	}

	public void assignPoi(POIObject poi) {
		this.poi = poi;
	}
	
	
}
