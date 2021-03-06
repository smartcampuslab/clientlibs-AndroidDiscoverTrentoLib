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

import eu.trentorise.smartcampus.dt.model.EventObjectForBean;
import eu.trentorise.smartcampus.dt.model.LocalEventObject;
import eu.trentorise.smartcampus.dt.model.PoiObjectForBean;
import eu.trentorise.smartcampus.dt.model.StoryObjectForBean;
import eu.trentorise.smartcampus.storage.BasicObject;
import eu.trentorise.smartcampus.storage.StorageConfigurationException;
import eu.trentorise.smartcampus.storage.db.BeanStorageHelper;
import eu.trentorise.smartcampus.storage.db.StorageConfiguration;
import eu.trentorise.smartcampus.territoryservice.model.POIObject;
import eu.trentorise.smartcampus.territoryservice.model.StoryObject;

public class DTStorageConfiguration implements StorageConfiguration {
	private static final long serialVersionUID = 906503482979452854L;

	@SuppressWarnings("unchecked")
	private static Class<? extends BasicObject>[] classes = (Class<? extends BasicObject>[])new Class<?>[]{PoiObjectForBean.class, EventObjectForBean.class,StoryObjectForBean.class};
	private static BeanStorageHelper<PoiObjectForBean> poiHelper = new POIStorageHelper();
	private static BeanStorageHelper<EventObjectForBean> eventHelper = new EventStorageHelper();
	private static BeanStorageHelper<StoryObjectForBean> storyHelper = new StoryStorageHelper();
	
	@Override
	public Class<? extends BasicObject>[] getClasses() {
		return classes;
	}

	@Override
	public String getTableName(Class<? extends BasicObject> cls) throws StorageConfigurationException {
		if (cls.equals(PoiObjectForBean.class)) {
			return "pois";
		}
		if (cls.equals(EventObjectForBean.class)) {
			return "events";
		}
		if (cls.equals(StoryObjectForBean.class)) {
			return "stories";
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends BasicObject> BeanStorageHelper<T> getStorageHelper(Class<T> cls) throws StorageConfigurationException {
		if (cls.equals(PoiObjectForBean.class)) {
			return (BeanStorageHelper<T>) poiHelper;
		}
		if (cls.equals(EventObjectForBean.class)) {
			return (BeanStorageHelper<T>) eventHelper;
		}
		if (cls.equals(StoryObjectForBean.class)) {
			return (BeanStorageHelper<T>) storyHelper;
		}
		return null;
	}

}
