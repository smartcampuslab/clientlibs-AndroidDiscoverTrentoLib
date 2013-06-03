package eu.trentorise.smartcampus.dt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import eu.trentorise.smartcampus.android.common.params.ParamsHelper;
import eu.trentorise.smartcampus.dt.custom.CategoryHelper;
import eu.trentorise.smartcampus.dt.custom.CategoryHelper.CategoryDescriptor;

public class DTParamsHelper {
	private static final String TAG = "DTParamsHelper";
	private static Context mContext;
	private static DTParamsHelper instance = null;

	public static final String DEFAULT_APP_TOKEN = "discovertrento";

	private static String FILENAME = "params_dt.js";

	/* json parameters in assets/params_dt.js */
	public static final String KEY_POI_CATEGORIES = "poi_categories";
	public static final String KEY_EVENT_CATEGORIES = "events_categories";
	public static final String KEY_STORY_CATEGORIES = "story_categories";
	public static final String KEY_EXCLUDE = "exclude";
	public static final String KEY_INCLUDE = "include";
	public static final String KEY_APP_TOKEN = "app_token";
	public static final String KEY_CENTER_MAP = "center_map";
	public static final String KEY_ZOOM_MAP = "zoom_map";

	private Map<Object, Object> paramsAsset;

	protected DTParamsHelper(Context mContext) {
		DTParamsHelper.mContext = mContext;
		this.paramsAsset = ParamsHelper.load(mContext, FILENAME);
	}

	public static void init(Context mContext) {
		if (instance == null) {
			instance = new DTParamsHelper(mContext);
		}
	}

	public static DTParamsHelper getInstance() {
		if (instance == null && mContext != null) {
			DTParamsHelper.init(mContext);
		}
		return instance;
	}

	public static String getAppToken() {
		String returnToken = new String();

		if (getInstance() == null || getInstance().getParamsAsset() == null
				|| getInstance().getParamsAsset().get(KEY_APP_TOKEN) == null) {
			returnToken = DEFAULT_APP_TOKEN;
		} else {
			returnToken = (String) getInstance().getParamsAsset().get(KEY_APP_TOKEN);
		}

		return returnToken;
	}

	public static CategoryDescriptor[] getFilteredArrayByParams(CategoryDescriptor[] categories, String type) {
		if (type.equalsIgnoreCase(CategoryHelper.CATEGORY_TYPE_POIS)) {
			if (getInstance().getParamsAsset().containsKey(KEY_POI_CATEGORIES)) {

				return orderArrayByKey(CategoryHelper.POI_CATEGORIES,
						(List<Integer>) getInstance().getParamsAsset().get(KEY_POI_CATEGORIES));

			}
		}
		if (type.equalsIgnoreCase(CategoryHelper.CATEGORY_TYPE_EVENTS)) {
			if (getInstance().getParamsAsset().containsKey(KEY_EVENT_CATEGORIES)) {
				return orderArrayByKey(CategoryHelper.EVENT_CATEGORIES,
						(List<Integer>) getInstance().getParamsAsset().get(KEY_EVENT_CATEGORIES));
			}
		}
		if (type.equalsIgnoreCase(CategoryHelper.CATEGORY_TYPE_STORIES)) {
			if (getInstance().getParamsAsset().containsKey(KEY_STORY_CATEGORIES)) {
				return orderArrayByKey(CategoryHelper.STORY_CATEGORIES,
						(List<Integer>) getInstance().getParamsAsset().get(KEY_STORY_CATEGORIES));
			}
		}

		return null;
	}

	public static Map<String, Object> getExcludeArray() {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap = (Map<String, Object>) getInstance().getParamsAsset().get(KEY_EXCLUDE);
		return returnMap;
	}

	public static Map<String, Object> getIncludeArray() {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap = (Map<String, Object>) getInstance().getParamsAsset().get(KEY_INCLUDE);
		return returnMap;
	}

	private Map<Object, Object> getParamsAsset() {
		return paramsAsset;
	}

	private static CategoryDescriptor[] orderArrayByKey(CategoryDescriptor[] pOI_CATEGORIES2, List<Integer> filter) {
		List<CategoryDescriptor> returnlist = new ArrayList<CategoryDescriptor>();
		for (Integer index : filter) {
			returnlist.add(pOI_CATEGORIES2[index - 1]);
		}
		return returnlist.toArray(new CategoryDescriptor[] {});
	}
	public static int getZoomLevelMap(){
		return (Integer) getInstance().getParamsAsset().get(KEY_ZOOM_MAP);
	}
	
	public static List<Double> getCenterMap(){
		return (List<Double>) getInstance().getParamsAsset().get(KEY_CENTER_MAP);
	}
}
