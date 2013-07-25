package eu.trentorise.smartcampus.dt.custom;

import android.content.Context;
import android.content.Intent;
import eu.trentorise.smartcampus.android.common.Utils;
import eu.trentorise.smartcampus.dt.model.BaseDTObject;
import eu.trentorise.smartcampus.dt.model.EventObject;
import eu.trentorise.smartcampus.dt.model.POIObject;
import eu.trentorise.smartcampus.dt.model.StoryObject;

public class ExperienceHelper {

	public static void openExperience(Context mContext, BaseDTObject object) {
		Intent intent = new Intent("eu.trentorise.smartcampus.EDIT");
		intent.putExtra("NearMeObject", Utils.convertToJSON(object));
		String entityType = null;
		if (object instanceof EventObject) entityType = "event";
		if (object instanceof POIObject) entityType = "location";
		if (object instanceof StoryObject) entityType = "narrative";

		intent.putExtra("NearMeObjectEntityType", entityType);
		mContext.startActivity(intent);
	}

}
