package eu.trentorise.smartcampus.dt.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import eu.trentorise.smartcampus.territoryservice.model.POIObject;

public class LocalEventObject extends eu.trentorise.smartcampus.territoryservice.model.EventObject {
	private static final long serialVersionUID = 388550207183035548L;
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");



	private POIObject poi = null;
	boolean poiIdUserDefined = false;
	private String description = null;

	public boolean isPoiIdUserDefined() {
		return poiIdUserDefined;
	}

	public void setPoiIdUserDefined(boolean poiIdUserDefined) {
		this.poiIdUserDefined = poiIdUserDefined;
	}
	
	public LocalEventObject() {
		super();
	}

	public CharSequence dateTimeString() {
		return DATE_FORMAT.format(new Date(getFromTime()));
	}

	public CharSequence toDateTimeString() {
		if (getToTime()==null||getToTime()==0)
			return dateTimeString();	
		return DATE_FORMAT.format(new Date(getToTime()));
	}

	public POIObject assignedPoi() {
		return poi;
	}

	public void assignPoi(POIObject poi) {
		this.poi = poi;
	}

	public boolean createdByUser() {
		return true;
	}
	
	public LocalEventObject copy() {
		LocalEventObject o = new LocalEventObject();
		o.setAttendees(getAttendees());
		o.setAttending(getAttending());
		o.setCommunityData(getCommunityData());
		o.setCommunityData(getCommunityData());
		o.setCreatorId(getCreatorId());
		o.setCreatorName(getCreatorName());
		o.setCustomData(getCustomData());
		o.setDescription(getDescription());
		o.setDomainId(getDomainId());
		o.setDomainType(getDomainType());
		o.setEntityId(getEntityId());
		o.setFromTime(getFromTime());
//		o.setFromTimeUserDefined(isFromTimeUserDefined());
		o.setId(getId());
		o.setLocation(getLocation());
		o.setPoiId(getPoiId());
		o.setPoiIdUserDefined(isPoiIdUserDefined());
		o.setSource(getSource());
		o.setTiming(getTiming());
		o.setTitle(getTitle());
		o.setToTime(getToTime());
//		o.setToTimeUserDefined(isToTimeUserDefined());
		o.setType(getType());
//		o.setTypeUserDefined(isToTimeUserDefined());
		o.setUpdateTime(getUpdateTime());
		o.setVersion(getVersion());
		o.assignPoi(assignedPoi());
		return o;
	}
	public String getTimingFormatted() {
		if (getTiming() != null) return getTiming().replace("\\n", "\n").replace("\t", "").replaceAll("(\n)+", "\n");
		return null;
	}
	
	public String getDescription() {
		return description;
	}

	public Spanned getFormattedDescription() {
		if (description != null) {
			if (description.indexOf('<')>=0) {
				return Html.fromHtml(description);
			} else {
				return new SpannableString(description);
			}
		}
		return null;
	} 

	public void setDescription(String description) {
		this.description = description;
	}


}