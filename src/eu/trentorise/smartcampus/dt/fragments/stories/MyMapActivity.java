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
package eu.trentorise.smartcampus.dt.fragments.stories;

import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.RelativeLayout;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

import eu.trentorise.smartcampus.android.feedback.utils.FeedbackFragmentInflater;
import eu.trentorise.smartcampus.dt.R;

public class MyMapActivity extends MapActivity {
    
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.my_map_activity);
        /*FeedbackFragmentInflater.inflateHandleButtonInRelativeLayout(this,
        		(RelativeLayout) findViewById(R.id.mymap_relativelayout_dt));*/
    }
    
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    
    public MapView getMapView(){
    	return (MapView) findViewById(R.id.mapview);
    }
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
}
