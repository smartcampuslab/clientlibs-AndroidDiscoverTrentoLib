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
package eu.trentorise.smartcampus.dt.custom.data;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import eu.trentorise.smartcampus.android.common.Utils;
import eu.trentorise.smartcampus.dt.model.BaseDTObject;
import eu.trentorise.smartcampus.dt.model.CommunityData;
import eu.trentorise.smartcampus.dt.model.Concept;

public class BaseDTStorageHelper {

	public static void setCommonFields(Cursor cursor, BaseDTObject o) {
		o.setId(cursor.getString(cursor.getColumnIndex("id")));
		o.setTitle(cursor.getString(cursor.getColumnIndex("title")));
		o.setDescription(cursor.getString(cursor.getColumnIndex("description")));
		o.setDomainType(cursor.getString(cursor.getColumnIndex("domainType")));
		o.setDomainId(cursor.getString(cursor.getColumnIndex("domainId")));
		o.setSource(cursor.getString(cursor.getColumnIndex("source")));
		o.setType(cursor.getString(cursor.getColumnIndex("type")));
		o.setCreatorId(cursor.getString(cursor.getColumnIndex("creatorId")));
		o.setCreatorName(cursor.getString(cursor.getColumnIndex("creatorName")));
		o.setEntityId(cursor.getLong(cursor.getColumnIndex("entityId")));
		o.setLocation(new double[]{cursor.getDouble(cursor.getColumnIndex("latitude")),cursor.getDouble(cursor.getColumnIndex("longitude"))});

		o.setTypeUserDefined(cursor.getInt(cursor.getColumnIndex("typeUserDefined")) > 0);
		o.setCommunityData(new CommunityData());
		o.getCommunityData().setAverageRating(cursor.getInt(cursor.getColumnIndex("averageRating")));
		o.getCommunityData().setNotes(cursor.getString(cursor.getColumnIndex("notes")));
		o.getCommunityData().setTags(Utils.convertJSONToObjects(cursor.getString(cursor.getColumnIndex("tags")), Concept.class));
		
		@SuppressWarnings("unchecked")
		Map<String,Object> map = Utils.convertJSONToObject(cursor.getString(cursor.getColumnIndex("customData")), Map.class);
		if (map != null && ! map.isEmpty()) o.setCustomData(map);
	}
	
	public static ContentValues toCommonContent(BaseDTObject bean) {
		ContentValues values = new ContentValues();
		values.put("id", bean.getId());
		values.put("title", bean.getTitle());
		values.put("description", bean.getDescription());
		values.put("domainType", bean.getDomainType());
		values.put("domainId", bean.getDomainId());
		values.put("type", bean.getType());
		values.put("source", bean.getSource());
		values.put("creatorId", bean.getCreatorId());
		values.put("creatorName", bean.getCreatorName());
		values.put("entityId", bean.getEntityId());
		if (bean.getLocation() != null) {
			values.put("latitude", bean.getLocation()[0]);
			values.put("longitude", bean.getLocation()[1]);
		}
		
		values.put("typeUserDefined", bean.isTypeUserDefined() ? 1 : 0);
		
		if (bean.getCommunityData() != null) {
			values.put("averageRating", bean.getCommunityData().getAverageRating());
			values.put("notes", bean.getCommunityData().getNotes());
			if (bean.getCommunityData().getTags() != null) {
				values.put("tags", Utils.convertToJSON(bean.getCommunityData().getTags()));
			}
		}
		if (bean.getCustomData() != null && ! bean.getCustomData().isEmpty()) {
			values.put("customData", Utils.convertToJSON(bean.getCustomData()));
		}
		return values;
	}

	public static Map<String,String> getCommonColumnDefinitions() {
		Map<String,String> defs = new HashMap<String, String>();
		defs.put("title", "TEXT");
		defs.put("averageRating", "TEXT");
		defs.put("description", "TEXT");
		defs.put("domainType", "TEXT");
		defs.put("domainId", "TEXT");
		defs.put("type", "TEXT");
		defs.put("source", "TEXT");
		defs.put("creatorId", "TEXT");
		defs.put("creatorName", "TEXT");
		defs.put("latitude", "DOUBLE");
		defs.put("longitude", "DOUBLE");
		defs.put("entityId", "INTEGER");
		defs.put("typeUserDefined", "INTEGER");
		defs.put("notes", "TEXT");
		defs.put("tags", "TEXT");
		defs.put("customData", "TEXT");
		return defs;
	}

}
