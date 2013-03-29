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

import java.util.Collections;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import eu.trentorise.smartcampus.android.common.Utils;
import eu.trentorise.smartcampus.dt.model.Concept;
import eu.trentorise.smartcampus.dt.model.POIData;
import eu.trentorise.smartcampus.dt.model.POIObject;
import eu.trentorise.smartcampus.dt.model.StepObject;
import eu.trentorise.smartcampus.dt.model.StoryObject;
import eu.trentorise.smartcampus.storage.db.BeanStorageHelper;

public class StoryStorageHelper implements BeanStorageHelper<StoryObject> {

	@Override
	public StoryObject toBean(Cursor cursor) {
		StoryObject story = new StoryObject();
		BaseDTStorageHelper.setCommonFields(cursor, story);
		
		
		story.setAttendees(cursor.getInt(cursor.getColumnIndex("attendees")));
		String attending = cursor.getString(cursor.getColumnIndex("attending"));
		story.setAttending(attending == null ? Collections.<String>emptyList() : Collections.singletonList(attending));
		story.setSteps(Utils.convertJSONToObjects(cursor.getString(cursor.getColumnIndex("steps")), StepObject.class));

	
		return story;
	}

	@Override
	public ContentValues toContent(StoryObject bean) {
		ContentValues values = BaseDTStorageHelper.toCommonContent(bean);
		values.put("attendees", bean.getAttendees());
		values.put("attending", bean.getAttending() != null && ! bean.getAttending().isEmpty() ? bean.getAttending().get(0) : null);
		if (bean.getSteps() != null) {
			values.put("steps", Utils.convertToJSON(bean.getSteps()));
		}
		return values;
	}

	@Override
	public Map<String,String> getColumnDefinitions() {
		Map<String,String> defs = BaseDTStorageHelper.getCommonColumnDefinitions();

		defs.put("attendees", "INTEGER");
		defs.put("attending", "TEXT");
		defs.put("steps", "TEXT");

		return defs;
	}

	@Override
	public boolean isSearchable() {
		return true;
	}


}
