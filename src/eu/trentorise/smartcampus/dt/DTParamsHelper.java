package eu.trentorise.smartcampus.dt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import eu.trentorise.smartcampus.android.common.params.ParamsHelper;
import eu.trentorise.smartcampus.dt.custom.CategoryHelper;
import eu.trentorise.smartcampus.dt.custom.CategoryHelper.CategoryDescriptor;
import eu.trentorise.smartcampus.storage.DataException;

public class DTParamsHelper {
	private Map<Object, Object> paramsAsset;
	private static DTParamsHelper instance = null;
	private static String filename = "params.json";
	
	/* json parameters in assets/params.json */
	public static final String KEY_POI_CATEGORIES = "poi_categories";
	public static final String KEY_EVENT_CATEGORIES = "events_categories";
	public static final String KEY_STORY_CATEGORIES = "story_categories";
	public static final String KEY_EXCLUDE = "exclude";
	public static final String KEY_INCLUDE = "include";

	public static void init(Context mContext, String file_name) {
		if (instance == null) {
			instance = new DTParamsHelper(mContext, file_name);
		}
	}

	protected DTParamsHelper(Context mContext, String file_name) {
		paramsAsset = ParamsHelper.load(mContext, filename);
	}

	public static DTParamsHelper getInstance() throws DataException {
		if (instance == null)
			throw new DataException("ParamsHelper is not initialized");
		return instance;
	}

	public  CategoryDescriptor[] getFilteredArrayByParams(CategoryDescriptor[] categories, String type) {
		try {
			if (type.equalsIgnoreCase(CategoryHelper.CATEGORY_TYPE_POIS)) {
				if (getInstance().getParamsAsset().containsKey(KEY_POI_CATEGORIES)) {

					return orderArrayByKey(CategoryHelper.POI_CATEGORIES, (List<Integer>) getInstance()
							.getParamsAsset().get(KEY_POI_CATEGORIES));

				}
			}
			if (type.equalsIgnoreCase(CategoryHelper.CATEGORY_TYPE_EVENTS)) {
				if (getInstance().getParamsAsset().containsKey(KEY_EVENT_CATEGORIES)) {
					return orderArrayByKey(CategoryHelper.EVENT_CATEGORIES, (List<Integer>) getInstance()
							.getParamsAsset().get(KEY_EVENT_CATEGORIES));
				}
			}
			if (type.equalsIgnoreCase(CategoryHelper.CATEGORY_TYPE_STORIES)) {
				if (getInstance().getParamsAsset().containsKey(KEY_STORY_CATEGORIES)) {
					return orderArrayByKey(CategoryHelper.STORY_CATEGORIES, (List<Integer>) getInstance()
							.getParamsAsset().get(KEY_STORY_CATEGORIES));
				}
			}
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}
	public  Map<String, Object> getExcludeArray(){
		Map<String,Object> returnMap = new HashMap<String, Object>();
		try {
			returnMap = (Map<String,Object>) getInstance().getParamsAsset().get(KEY_EXCLUDE);
		} catch (DataException e) {
			e.printStackTrace();
		}
		return returnMap;
	}
	private Map<Object, Object> getParamsAsset() {
		return paramsAsset;
	}

	private  CategoryDescriptor[] orderArrayByKey(CategoryDescriptor[] pOI_CATEGORIES2, List<Integer> filter) {
		List<CategoryDescriptor> returnlist = new ArrayList<CategoryDescriptor>();
		for (Integer index : filter) {
			returnlist.add(pOI_CATEGORIES2[index - 1]);
		}
		return returnlist.toArray(new CategoryDescriptor[] {});
	}
}
