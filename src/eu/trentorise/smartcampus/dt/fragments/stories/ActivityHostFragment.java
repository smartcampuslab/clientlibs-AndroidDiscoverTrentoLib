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
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;

/**
 * This is a fragment that will be used during transition from activities to fragments.
 */
public abstract class ActivityHostFragment extends LocalActivityManagerFragment {
    
    protected abstract Class<? extends Activity> getActivityClass();
    private final static String ACTIVITY_TAG = "hosted";
    private static final String TAG = "ActivityHostFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "eu.trentorise.smartcampus.dt.fragments.stories.ActivityHostFragment.onCreateView ");
        }
        Intent intent = new Intent(getActivity(), getActivityClass());
        
        final Window w = getLocalActivityManager().startActivity(ACTIVITY_TAG, intent);
        final View wd = w != null ? w.getDecorView() : null;
       
        if (wd != null) {
            ViewParent parent = wd.getParent();
            if(parent != null) {
                ViewGroup v = (ViewGroup)parent;
                v.removeView(wd);
            }
            
            wd.setVisibility(View.VISIBLE);
            wd.setFocusableInTouchMode(true);
            if(wd instanceof ViewGroup) {
                ((ViewGroup) wd).setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
            }
        }
        return wd;
    }

    /**
     * For accessing public methods of the hosted activity
     */
    public Activity getHostedActivity() {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "eu.trentorise.smartcampus.dt.fragments.stories.ActivityHostFragment.getHostedActivity ");
        }
		return getLocalActivityManager().getActivity(ACTIVITY_TAG);
	}
}
