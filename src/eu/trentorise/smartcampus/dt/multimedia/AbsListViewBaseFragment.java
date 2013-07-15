/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package eu.trentorise.smartcampus.dt.multimedia;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.AbsListView;

import com.actionbarsherlock.view.Menu;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;

import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.notifications.NotificationsSherlockFragmentDT;

/**
 * 
 * 
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class AbsListViewBaseFragment extends BaseFragment {

	protected static final String STATE_PAUSE_ON_SCROLL = "STATE_PAUSE_ON_SCROLL";
	protected static final String STATE_PAUSE_ON_FLING = "STATE_PAUSE_ON_FLING";

	protected AbsListView listView;

	protected boolean pauseOnScroll = false;
	protected boolean pauseOnFling = true;

//	@Override
//	public void onRestoreInstanceState(Bundle savedInstanceState) {
//		pauseOnScroll = savedInstanceState.getBoolean(STATE_PAUSE_ON_SCROLL, false);
//		pauseOnFling = savedInstanceState.getBoolean(STATE_PAUSE_ON_FLING, true);
//	}

	@Override
	public void onResume() {
		super.onResume();
		applyScrollListener();
	}

	private void applyScrollListener() {
		listView.setOnScrollListener(new PauseOnScrollListener(imageLoader, pauseOnScroll, pauseOnFling));
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(STATE_PAUSE_ON_SCROLL, pauseOnScroll);
		outState.putBoolean(STATE_PAUSE_ON_FLING, pauseOnFling);
	}


	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		MenuItem pauseOnScrollItem = (MenuItem) menu.findItem(R.id.item_pause_on_scroll);
		pauseOnScrollItem.setVisible(true);
		pauseOnScrollItem.setChecked(pauseOnScroll);

		MenuItem pauseOnFlingItem = (MenuItem) menu.findItem(R.id.item_pause_on_fling);
		pauseOnFlingItem.setVisible(true);
		pauseOnFlingItem.setChecked(pauseOnFling);
	}

}
