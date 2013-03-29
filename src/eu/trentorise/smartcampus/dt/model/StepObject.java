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

import eu.trentorise.smartcampus.storage.BasicObject;

public class StepObject extends BasicObject {

	private static final long serialVersionUID = 8517257945277793403L;

	private POIObject poi;
	private String note;
	private String poiId;
	
	
	public StepObject(){
		super();
	}
	public StepObject(POIObject poi, String note) {
		assignPoi(poi);
		this.note=note;
		this.poiId = poi.getId();
	}

	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}

	public POIObject assignedPoi() {
		return poi;
	}

	public void assignPoi(POIObject poi) {
		this.poi = poi;
	}

	public String getPoiId() {
		return poiId;
	}

	public void setPoiId(String poiId) {
		this.poiId = poiId;
	}
}
