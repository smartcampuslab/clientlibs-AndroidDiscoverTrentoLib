package eu.trentorise.smartcampus.dt;

import java.util.ArrayList;
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

	public  CategoryDescriptor[] getFilteredArrayByParams(CategoryDescriptor[] pOI_CATEGORIES, String type) {
		try {
			if (type.equalsIgnoreCase(CategoryHelper.CATEGORY_TYPE_POIS)) {
				if (getInstance().getParamsAsset().containsKey(CategoryHelper.KEY_POI_CATEGORIES)) {

					return orderArrayByKey(CategoryHelper.POI_CATEGORIES, (List<Integer>) getInstance()
							.getParamsAsset().get(CategoryHelper.KEY_POI_CATEGORIES));

				}
			}
			if (type.equalsIgnoreCase(CategoryHelper.CATEGORY_TYPE_EVENTS)) {
				if (getInstance().getParamsAsset().containsKey(CategoryHelper.KEY_EVENT_CATEGORIES)) {
					return orderArrayByKey(CategoryHelper.EVENT_CATEGORIES, (List<Integer>) getInstance()
							.getParamsAsset().get(CategoryHelper.KEY_EVENT_CATEGORIES));
				}
			}
			if (type.equalsIgnoreCase(CategoryHelper.CATEGORY_TYPE_STORIES)) {
				if (getInstance().getParamsAsset().containsKey(CategoryHelper.KEY_STORY_CATEGORIES)) {
					return orderArrayByKey(CategoryHelper.STORY_CATEGORIES, (List<Integer>) getInstance()
							.getParamsAsset().get(CategoryHelper.KEY_STORY_CATEGORIES));
				}
			}
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

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
