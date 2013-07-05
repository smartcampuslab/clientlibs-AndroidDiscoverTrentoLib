package eu.trentorise.smartcampus.dt.custom;

import android.content.Context;
import android.content.Intent;
import eu.trentorise.smartcampus.android.common.Utils;
import eu.trentorise.smartcampus.dt.model.BaseDTObject;

public class ExperienceHelper {

	public static void openExperience(Context mContext, BaseDTObject object) {
		Intent intent = new Intent("eu.trentorise.smartcampus.EDIT");
		intent.putExtra("NearMeObject", Utils.convertToJSON(object));
		mContext.startActivity(intent);
	}

}
