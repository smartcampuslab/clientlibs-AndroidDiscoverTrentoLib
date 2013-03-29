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
import eu.trentorise.smartcampus.dt.model.EventObject;
import eu.trentorise.smartcampus.storage.db.BeanStorageHelper;

public class EventStorageHelper implements BeanStorageHelper<EventObject> {

	@Override
	public EventObject toBean(Cursor cursor) {
		EventObject event = new EventObject();
		BaseDTStorageHelper.setCommonFields(cursor, event);
		
		event.setPoiId(cursor.getString(cursor.getColumnIndex("poiId")));
		event.setFromTime(cursor.getLong(cursor.getColumnIndex("fromTime")));
		event.setToTime(cursor.getLong(cursor.getColumnIndex("toTime")));
		event.setTiming(cursor.getString(cursor.getColumnIndex("timing")));
		event.setAttendees(cursor.getInt(cursor.getColumnIndex("attendees")));
		String attending = cursor.getString(cursor.getColumnIndex("attending"));
		event.setAttending(attending == null ? Collections.<String>emptyList() : Collections.singletonList(attending));

		event.setPoiIdUserDefined(cursor.getInt(cursor.getColumnIndex("poiIdUserDefined")) > 0);
		event.setFromTimeUserDefined(cursor.getInt(cursor.getColumnIndex("fromTimeUserDefined")) > 0);
		event.setToTimeUserDefined(cursor.getInt(cursor.getColumnIndex("toTimeUserDefined")) > 0);
		
		return event;
	}

	@Override
	public ContentValues toContent(EventObject bean) {
		ContentValues values = BaseDTStorageHelper.toCommonContent(bean);
		
		values.put("poiId", bean.getPoiId());
		values.put("fromTime", bean.getFromTime());
		values.put("toTime", bean.getToTime());
		values.put("timing", bean.getTimingFormatted());
		values.put("attendees", bean.getAttendees());
		values.put("attending", bean.getAttending() != null && ! bean.getAttending().isEmpty() ? bean.getAttending().get(0) : null);
		values.put("poiIdUserDefined", bean.isPoiIdUserDefined() ? 1 : 0);
		values.put("fromTimeUserDefined", bean.isFromTimeUserDefined() ? 1 : 0);
		values.put("toTimeUserDefined", bean.isToTimeUserDefined() ? 1 : 0);
		
		return values;
	}

	@Override
	public Map<String,String> getColumnDefinitions() {
		Map<String,String> defs = BaseDTStorageHelper.getCommonColumnDefinitions();

		defs.put("poiId", "TEXT");
		defs.put("fromTime", "INTEGER");
		defs.put("toTime", "INTEGER");
		defs.put("timing", "TEXT");
		defs.put("attendees", "INTEGER");
		defs.put("attending", "TEXT");

		defs.put("poiIdUserDefined", "INTEGER");
		defs.put("fromTimeUserDefined", "INTEGER");
		defs.put("toTimeUserDefined", "INTEGER");

		return defs;
	}

	@Override
	public boolean isSearchable() {
		return true;
	}

	
}
