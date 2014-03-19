package eu.trentorise.smartcampus.dt.fragments.home;

import eu.trentorise.smartcampus.osm.android.util.GeoPoint;
import eu.trentorise.smartcampus.osm.android.views.overlay.OverlayItem;
import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;

public class OverlayObject extends OverlayItem {
	
	private final long serialVersionUID = 1L;
	private BaseDTObject data;

	public OverlayObject(String aTitle, String aSnippet, GeoPoint aGeoPoint) {
		super(aTitle, aSnippet, aGeoPoint);
		// TODO Auto-generated constructor stub
	}

	public void setData(BaseDTObject data){
		this.data=data;
	}
	
	public BaseDTObject getData(){
		return this.data;
	}
}
