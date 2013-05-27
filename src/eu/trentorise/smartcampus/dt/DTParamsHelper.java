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
	private static String filename = "params_dt.js";
	private static Context mContext;
	/* json parameters in assets/params.json */
	public static final String KEY_POI_CATEGORIES = "poi_categories";
	public static final String KEY_EVENT_CATEGORIES = "events_categories";
	public static final String KEY_STORY_CATEGORIES = "story_categories";
	public static final String KEY_EXCLUDE = "exclude";
	public static final String KEY_INCLUDE = "include";
	public static final String KEY_APP_TOKEN = "app_token";
	public static final String DEFAULT_APP_TOKEN = "discovertrento";


	public static void init(Context mContext) {
		if (instance == null) {
			instance = new DTParamsHelper(mContext);
		}
	}

	protected DTParamsHelper(Context mContext) {
		paramsAsset = ParamsHelper.load(mContext, filename);
		this.mContext = mContext;
	}

	public static DTParamsHelper getInstance() {
		if ((instance == null)&&(mContext==null))
			{
			init(mContext);
			}
		return instance;
	}

	public  static CategoryDescriptor[] getFilteredArrayByParams(CategoryDescriptor[] categories, String type) {
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
	
		return null;

	}
	public static  Map<String, Object> getExcludeArray(){
		Map<String,Object> returnMap = new HashMap<String, Object>();
			returnMap = (Map<String,Object>) getInstance().getParamsAsset().get(KEY_EXCLUDE);
		return returnMap;
	}
	
	public  static Map<String, Object> getIncludeArray(){
		Map<String,Object> returnMap = new HashMap<String, Object>();
			returnMap = (Map<String,Object>) getInstance().getParamsAsset().get(KEY_INCLUDE);
		return returnMap;
	}
	
	public  static String getAppToken(){
		String returnToken = new String();
			returnToken = (String) getInstance().getParamsAsset().get(KEY_APP_TOKEN);
			if (returnToken ==null)
				return DEFAULT_APP_TOKEN;
		return returnToken;
	}
	
	private Map<Object, Object> getParamsAsset() {
		return paramsAsset;
	}

	private  static CategoryDescriptor[] orderArrayByKey(CategoryDescriptor[] pOI_CATEGORIES2, List<Integer> filter) {
		List<CategoryDescriptor> returnlist = new ArrayList<CategoryDescriptor>();
		for (Integer index : filter) {
			returnlist.add(pOI_CATEGORIES2[index - 1]);
		}
		return returnlist.toArray(new CategoryDescriptor[] {});
	}
}
