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
package eu.trentorise.smartcampus.dt.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.trentorise.smartcampus.dt.R;

public class CategoryHelper {

	private static final String POI_NONCATEGORIZED = "Other place";
	private static final String EVENT_NONCATEGORIZED = "Other event";
	private static final String STORY_NONCATEGORIZED = "Other story";
	
	public static final String CATEGORY_TYPE_POIS = "pois";
	public static final String CATEGORY_TYPE_EVENTS = "events";
	public static final String CATEGORY_TYPE_STORIES = "stories";

	public static CategoryDescriptor TODAY_EVENTS = 
			new CategoryDescriptor(R.drawable.marker_event_generic, R.drawable.ic_other_event, "Today",
				R.string.categories_event_today) ;
	public static CategoryDescriptor[] EVENT_CATEGORIES = new CategoryDescriptor[] {
			new CategoryDescriptor(R.drawable.marker_event_concert, R.drawable.ic_event_concerts, "Concerts",
					R.string.categories_event_concert),
			new CategoryDescriptor(R.drawable.marker_event_happy, R.drawable.ic_event_happy, "Happy hours",
					R.string.categories_event_happyhour),
			new CategoryDescriptor(R.drawable.marker_event_movie, R.drawable.ic_event_movies, "Movies",
					R.string.categories_event_movie),
			new CategoryDescriptor(R.drawable.marker_event_party, R.drawable.ic_event_parties, "Parties",
					R.string.categories_event_party),
			new CategoryDescriptor(R.drawable.marker_event_seminar, R.drawable.ic_event_seminars, "Seminars",
					R.string.categories_event_seminar),
			new CategoryDescriptor(R.drawable.marker_event_theater, R.drawable.ic_event_theaters, "Theaters",
					R.string.categories_event_theater),
			new CategoryDescriptor(R.drawable.marker_event_exhibition, R.drawable.ic_event_exhibition, "Exhibitions",
					R.string.categories_event_exhibition),
			new CategoryDescriptor(R.drawable.marker_event_generic, R.drawable.ic_other_event, EVENT_NONCATEGORIZED,
					R.string.categories_event_generic), };

	public static CategoryDescriptor[] POI_CATEGORIES = new CategoryDescriptor[] {
			new CategoryDescriptor(R.drawable.marker_poi_museum, R.drawable.ic_museums, "Museums",
					R.string.categories_poi_museum),
			new CategoryDescriptor(R.drawable.marker_poi_mobility, R.drawable.ic_mobility, "Mobility",
					R.string.categories_poi_mobility),
			new CategoryDescriptor(R.drawable.marker_poi_parking, R.drawable.ic_parking, "Parking",
					R.string.categories_poi_parking),
			new CategoryDescriptor(R.drawable.marker_poi_office, R.drawable.ic_offices, "Offices",
					R.string.categories_poi_office),
			new CategoryDescriptor(R.drawable.marker_poi_theater, R.drawable.ic_event_theaters, "Theater",
					R.string.categories_poi_theater),
			new CategoryDescriptor(R.drawable.marker_poi_university, R.drawable.ic_university, "University",
					R.string.categories_poi_university),
			new CategoryDescriptor(R.drawable.marker_poi_accomodation, R.drawable.ic_accomodation, "Accomodation",
					R.string.categories_poi_accommodation),
			new CategoryDescriptor(R.drawable.marker_poi_library, R.drawable.ic_libraries, "Libraries",
					R.string.categories_poi_library),
			new CategoryDescriptor(R.drawable.marker_poi_food, R.drawable.ic_food, "Food", R.string.categories_poi_food),
			new CategoryDescriptor(R.drawable.marker_poi_drink, R.drawable.ic_event_happy, "Drink",
					R.string.categories_poi_drink),
			new CategoryDescriptor(R.drawable.marker_poi_cinema, R.drawable.ic_event_movies, "Cinemas",
					R.string.categories_poi_cinema),
			new CategoryDescriptor(R.drawable.marker_poi_generic, R.drawable.ic_other_poi, POI_NONCATEGORIZED,
					R.string.categories_poi_generic), };

	public static CategoryDescriptor[] STORY_CATEGORIES = new CategoryDescriptor[] {
			new CategoryDescriptor(R.drawable.marker_story_leisure, R.drawable.ic_story_leisure, "Leisure",
					R.string.categories_story_leisure),
			new CategoryDescriptor(R.drawable.marker_story_offices_and_services, R.drawable.ic_story_offices_and_services,
					"Offices and Services", R.string.categories_story_offices_and_services),
			new CategoryDescriptor(R.drawable.marker_story_univerisity, R.drawable.ic_story_university, "University",
					R.string.categories_story_university),
			new CategoryDescriptor(R.drawable.marker_story_culture, R.drawable.ic_story_culture, "Culture",
					R.string.categories_story_culture),
			new CategoryDescriptor(R.drawable.marker_story_generic, R.drawable.ic_other_story, STORY_NONCATEGORIZED,
					R.string.categories_story_generic), };

	private static Map<String, String> categoryMapping = new HashMap<String, String>();

	private static Map<String, CategoryDescriptor> descriptorMap = new LinkedHashMap<String, CategoryHelper.CategoryDescriptor>();
	static {
		for (CategoryDescriptor event : EVENT_CATEGORIES) {
			descriptorMap.put(event.category, event);
		}

		for (CategoryDescriptor poi : POI_CATEGORIES) {
			descriptorMap.put(poi.category, poi);
		}

		for (CategoryDescriptor story : STORY_CATEGORIES) {
			descriptorMap.put(story.category, story);
		}

		for (String s : descriptorMap.keySet()) {
			categoryMapping.put(s, s);
		}
		// custom categories for events
		categoryMapping.put("Dances", "Theaters");
		// custom categories for POIs
		categoryMapping.put("biblioteca", "Libraries");
		categoryMapping.put("museo", "Museums");
		categoryMapping.put("esposizione", "Museums");
		categoryMapping.put("arte", "Museums");
		categoryMapping.put("luogo", POI_NONCATEGORIZED);
		categoryMapping.put("ufficio", "Offices");
		categoryMapping.put("sala", POI_NONCATEGORIZED);
		categoryMapping.put("teatro", "Theater");
		categoryMapping.put("musica", "Theater");
		categoryMapping.put("universita", "University");
		categoryMapping.put("bar", "Drink");
		categoryMapping.put("ristorante", "Food");
		categoryMapping.put("Lodging", "Accomodation");
		categoryMapping.put("Other", POI_NONCATEGORIZED);
		categoryMapping.put("ou", POI_NONCATEGORIZED);
	}

	public static String[] getAllCategories(Set<String> set) {
		List<String> result = new ArrayList<String>();
		for (String key : categoryMapping.keySet()) {
			if (set.contains(categoryMapping.get(key))) {
				if (key.equals(EVENT_NONCATEGORIZED) || key.equals(POI_NONCATEGORIZED) || key.equals(STORY_NONCATEGORIZED)) {

					result.add(null);
				}
				result.add(key);
				// set.remove(categoryMapping.get(key));
			}
		}
		return result.toArray(new String[result.size()]);
	}

	public static String getMainCategory(String category) {
		return categoryMapping.get(category);
	}
	
	public static int getMapIconByType(String type) {
		if (categoryMapping.containsKey(type))
			return descriptorMap.get(categoryMapping.get(type)).map_icon;
		return R.drawable.marker_poi_generic;
	}
	
	public static int getIconByType(String type) {
		if (categoryMapping.containsKey(type))
			return descriptorMap.get(categoryMapping.get(type)).thumbnail;
		return R.drawable.ic_other_event;
	}

	public static class CategoryDescriptor {
		public int map_icon;
		public int thumbnail;
		public String category;
		public int description;

		public CategoryDescriptor(int map_icon, int thumbnail, String category, int description) {
			super();
			this.map_icon = map_icon;
			this.thumbnail = thumbnail;
			this.category = category;
			this.description = description;
		}
	}

	public static CategoryDescriptor[] getPOICategoryDescriptors() {
		return POI_CATEGORIES;
	}

	public static CategoryDescriptor[] getEventCategoryDescriptors() {
		return EVENT_CATEGORIES;
	}

	public static CategoryDescriptor[] getStoryCategoryDescriptors() {
		return STORY_CATEGORIES;
	}

	public static String[] getPOICategories() {
		String[] res = new String[POI_CATEGORIES.length];
		for (int i = 0; i < POI_CATEGORIES.length; i++) {
			res[i] = POI_CATEGORIES[i].category;
		}
		return res;
	}

	public static String[] getEventCategories() {
		String[] res = new String[EVENT_CATEGORIES.length];
		for (int i = 0; i < EVENT_CATEGORIES.length; i++) {
			res[i] = EVENT_CATEGORIES[i].category;
		}
		return res;
	}

	public static String[] getStoryCategories() {
		String[] res = new String[STORY_CATEGORIES.length];
		for (int i = 0; i < STORY_CATEGORIES.length; i++) {
			res[i] = STORY_CATEGORIES[i].category;
		}
		return res;
	}

	public static CategoryDescriptor getCategoryDescriptorByCategory(String type, String cat) {
		CategoryDescriptor[] cdarray = null;

		if (type.equalsIgnoreCase(CATEGORY_TYPE_POIS)) {
			cdarray = POI_CATEGORIES;
		} else if (type.equalsIgnoreCase(CATEGORY_TYPE_EVENTS)) {
			cdarray = EVENT_CATEGORIES;
		} else if (type.equalsIgnoreCase(CATEGORY_TYPE_STORIES)) {
			cdarray = STORY_CATEGORIES;
		}

		if (cdarray != null) {
			for (int i = 0; i < cdarray.length; i++) {
				CategoryDescriptor cd = cdarray[i];
				if (cd.category.equalsIgnoreCase(cat)) {
					return cd;
				}
			}
		}


		return null;
	}

}
