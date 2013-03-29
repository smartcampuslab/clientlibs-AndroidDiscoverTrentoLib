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
package eu.trentorise.smartcampus.dt.fragments.pois;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.GridView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

import eu.trentorise.smartcampus.ac.UserRegistration;
import eu.trentorise.smartcampus.ac.authenticator.AMSCAccessProvider;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.PoisCategoriesAdapter;
import eu.trentorise.smartcampus.dt.custom.SearchHelper;
import eu.trentorise.smartcampus.dt.notifications.NotificationsSherlockFragmentDT;

public class AllPoisFragment extends NotificationsSherlockFragmentDT {
	private GridView gridview;
	private FragmentManager fragmentManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fragmentManager = getSherlockActivity().getSupportFragmentManager();
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.poiscategories, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		gridview = (GridView) getView().findViewById(R.id.pois_gridview);
		gridview.setAdapter(new PoisCategoriesAdapter(getSherlockActivity().getApplicationContext(), fragmentManager));
		//hide keyboard if it is still open
		InputMethodManager imm = (InputMethodManager)getSherlockActivity().getSystemService(
			      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(gridview.getWindowToken(), 0);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		getSherlockActivity().getSupportMenuInflater().inflate(R.menu.gripmenu, menu);
		SubMenu submenu = menu.getItem(0).getSubMenu();
		submenu.clear();

		submenu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_addpoi, Menu.NONE,
				R.string.menu_item_addpoi_text);
		SearchHelper.createSearchMenu(submenu, getActivity(), new SearchHelper.OnSearchListener() {
			@Override
			public void onSearch(String query) {
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				PoisListingFragment fragment = new PoisListingFragment();
				Bundle args = new Bundle();
				args.putString(PoisListingFragment.ARG_QUERY, query);
				fragment.setArguments(args);
				fragmentTransaction
						.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
//				fragmentTransaction.detach(currentFragment);
				fragmentTransaction.replace(android.R.id.content, fragment,"pois");
				fragmentTransaction.addToBackStack(fragment.getTag());
				fragmentTransaction.commit();
			}
		});
		
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_item_addpoi) {
			if (new AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
				// show dialog box
				UserRegistration.upgradeuser(getSherlockActivity());
				return false;
			} else {
			FragmentTransaction fragmentTransaction = fragmentManager
					.beginTransaction();
			Fragment fragment = new CreatePoiFragment();
			fragmentTransaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			//			fragmentTransaction.detach(this);
			fragmentTransaction.replace(android.R.id.content, fragment, "pois");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
			return true;
			}
		} else if (item.getItemId() == R.id.search) {
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

}
