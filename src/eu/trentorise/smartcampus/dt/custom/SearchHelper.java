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

import android.app.Activity;
import android.content.Context;
import android.support.v4.widget.SearchViewCompat;
import android.support.v4.widget.SearchViewCompat.OnQueryTextListenerCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.devspark.collapsiblesearchmenu.CollapsibleMenuUtils;

import eu.trentorise.smartcampus.dt.R;

public class SearchHelper {

	public interface OnSearchListener {
		void onSearch(String query);
	}
	
	private OnSearchListener onSearchListener;
	private Context context;

	private SearchHelper(OnSearchListener onSearchListener, Context context) {
		super();
		this.onSearchListener = onSearchListener;
		this.context = context;
	}

	public static void createSearchMenu(Menu submenu, Activity activity, OnSearchListener onSearchListener) {
		SearchHelper helper = new SearchHelper(onSearchListener, activity);
		
		View searchView = SearchViewCompat.newSearchView(activity);
		if (searchView != null) {
			MenuItem search = submenu.add(Menu.CATEGORY_SYSTEM, R.id.search, Menu.NONE, R.string.search_txt);
			search.setActionView(searchView);
			search.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
			SearchViewCompat.setOnQueryTextListener(searchView, helper.listener);
		} else {
			MenuItem search = CollapsibleMenuUtils.addSearchMenuItem(submenu, false, helper.textWatcher);
			((AutoCompleteTextView)search.getActionView().findViewById(R.id.search_src_text)).setOnEditorActionListener(helper.searchListener);
			search.setIcon(null);
		    // other code
//			search.setActionView(R.layout.collapsible_edittext);
		}
//        search.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

	}
	
	private OnQueryTextListenerCompat listener = new OnQueryTextListenerCompat() {
		 @Override
		    public boolean onQueryTextChange(String newText) {
		        return false;
		    }

		    @Override
		    public boolean onQueryTextSubmit(String query) {            
		    	onSearchListener.onSearch(query);
		        return false;
		    }
	};

	private OnEditorActionListener searchListener = new OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if(actionId == EditorInfo.IME_ACTION_SEARCH || event.getKeyCode()== KeyEvent.KEYCODE_ENTER) 
           {
			    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		    	onSearchListener.onSearch(v.getText().toString());
           }
           return false;
		}
	};
	
	private TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		@Override
		public void afterTextChanged(Editable s) {}
	};

}
