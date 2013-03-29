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
package eu.trentorise.smartcampus.dt.multimedia;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockFragment;

import eu.trentorise.smartcampus.dt.R;

public class FullScreenImageFragment extends SherlockFragment {
	    
     @Override  
     public void onCreate(Bundle savedInstanceState) {  
         super.onCreate(savedInstanceState);  
     }  
       
     @Override  
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {         
         return inflater.inflate(R.layout.fullscreen, container, false);  
     }  
     
     @Override
    public void onStart() {
    	super.onStart();
    	Bundle bundle=this.getArguments();
    	int image_id=bundle.getInt("source");
    	ImageView iv=(ImageView)getView().findViewById(R.id.fs_image);
    	iv.setImageResource(image_id);
    }
     //http://android-developers.blogspot.it/2011/08/horizontal-view-swiping-with-viewpager.html
}
