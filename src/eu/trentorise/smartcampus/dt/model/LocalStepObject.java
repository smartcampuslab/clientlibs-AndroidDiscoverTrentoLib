package eu.trentorise.smartcampus.dt.model;

import eu.trentorise.smartcampus.territoryservice.model.POIObject;

public class LocalStepObject extends eu.trentorise.smartcampus.territoryservice.model.StepObject {

	private static final long serialVersionUID = 8517257945277793403L;

	private POIObject poi;
	private String note;
	private String poiId;


	public LocalStepObject(){
		super();
	}
	public LocalStepObject(POIObject poi, String note) {
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
