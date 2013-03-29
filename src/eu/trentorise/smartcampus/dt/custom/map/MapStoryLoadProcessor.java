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
package eu.trentorise.smartcampus.dt.custom.map;

import java.util.Collection;

import android.app.Activity;

import com.google.android.maps.MapView;

import eu.trentorise.smartcampus.dt.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.dt.model.BaseDTObject;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public abstract class MapStoryLoadProcessor extends AbstractAsyncTaskProcessor<Void, Collection<? extends BaseDTObject>> {
	
	protected DTStoryItemizedOverlay overlay = null;
	protected MapView mapView = null;

	public MapStoryLoadProcessor(Activity activity, DTStoryItemizedOverlay overlay, MapView mapView) {
		super(activity);
		this.overlay = overlay;
		this.mapView = mapView;
	}

	
	@Override
	public Collection<? extends BaseDTObject> performAction(Void... params) throws SecurityException, Exception {
		return getObjects();
	}

	@Override
	public void handleResult(Collection<? extends BaseDTObject> objects) {
		if (objects != null) {
			Integer index = 0;
			for (BaseDTObject o : objects) {
				index++;
				overlay.addOverlay(o,index.toString());
			}
			overlay.populateAll();
			mapView.invalidate();
			overlay.fithMaptOnTheStory();
		}
	}

	protected abstract Collection<? extends BaseDTObject> getObjects()  throws SecurityException, Exception;
	
}
