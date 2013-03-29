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

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.android.feedback.utils.FeedbackFragmentInflater;

public class MyMapFragment extends ActivityHostFragment {
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View returnMapView=super.onCreateView(inflater, container, savedInstanceState);
		StoryDetailsFragment sf = (StoryDetailsFragment)
				((SherlockFragmentActivity) getActivity()).
				getSupportFragmentManager().findFragmentByTag("stories");
		sf.setMap (((MyMapActivity)super.getHostedActivity()).getMapView());
		return returnMapView;
	}
	
	
	
    @Override
	public void onStart() {
		super.onStart();
	}



	@Override
    protected Class<? extends Activity> getActivityClass() {
        return MyMapActivity.class;
    }
}
