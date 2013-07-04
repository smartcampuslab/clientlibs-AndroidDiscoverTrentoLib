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
package eu.trentorise.smartcampus.dt.fragments.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import eu.trentorise.smartcampus.dt.custom.CategoryHelper;
import eu.trentorise.smartcampus.dt.custom.CategoryHelper.CategoryDescriptor;
import eu.trentorise.smartcampus.dt.custom.EventsCategoriesAdapter;
import eu.trentorise.smartcampus.dt.fragments.search.SearchFragment;
import eu.trentorise.smartcampus.dt.notifications.NotificationsSherlockFragmentDT;

public class AllEventsFragment extends NotificationsSherlockFragmentDT {
	private FragmentManager fragmentManager;
	private GridView gridview;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fragmentManager = getSherlockActivity().getSupportFragmentManager();
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.eventscategories, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		List<CategoryDescriptor> list = new ArrayList<CategoryDescriptor>();
		list.add(CategoryHelper.EVENTS_MY);
		list.add(CategoryHelper.EVENTS_TODAY);
		list.addAll(Arrays.asList(CategoryHelper.getEventCategoryDescriptorsFiltered()));

		gridview = (GridView) getView().findViewById(R.id.events_gridview);
		gridview.setAdapter(new EventsCategoriesAdapter(getSherlockActivity().getApplicationContext(), R.layout.grid_item,
				list, fragmentManager));
		// hide keyboard if it is still open
		InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(gridview.getWindowToken(), 0);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		getSherlockActivity().getSupportMenuInflater().inflate(R.menu.gripmenu, menu);
		SubMenu submenu = menu.getItem(0).getSubMenu();
		submenu.clear();

		submenu.add(Menu.CATEGORY_SYSTEM, R.id.submenu_search, Menu.NONE, R.string.search_txt);
		submenu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_addevent, Menu.NONE, R.string.menu_item_addevent_text);

		// submenu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_todayevent,
		// Menu.NONE, R.string.menu_item_todayevent_text);
		// submenu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_myevents, Menu.NONE,
		// R.string.menu_item_myevents_text);

		// SearchHelper.createSearchMenu(submenu, getActivity(), new
		// SearchHelper.OnSearchListener() {
		// @Override
		// public void onSearch(String query) {
		// FragmentTransaction fragmentTransaction =
		// fragmentManager.beginTransaction();
		// EventsListingFragment fragment = new EventsListingFragment();
		// Bundle args = new Bundle();
		// args.putString(EventsListingFragment.ARG_QUERY, query);
		// fragment.setArguments(args);
		// fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		// // fragmentTransaction.detach(currentFragment);
		// fragmentTransaction.replace(android.R.id.content, fragment,
		// "events");
		// fragmentTransaction.addToBackStack(fragment.getTag());
		// fragmentTransaction.commit();
		// }
		// });

		super.onPrepareOptionsMenu(menu);
	}

	/*
	 * @Override public void onPrepareOptionsMenu(Menu menu) { menu.clear();
	 * MenuItem item = menu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_addevent,
	 * 1, R.string.menu_item_addevent_text);
	 * item.setIcon(R.drawable.ic_event_add);
	 * item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	 * 
	 * item = menu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_myevents, 2,
	 * R.string.menu_item_myevents_text); item.setIcon(R.drawable.ic_myevents);
	 * item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	 * 
	 * super.onPrepareOptionsMenu(menu); }
	 */

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		FragmentTransaction fragmentTransaction;
		Fragment fragment;
		Bundle args;
		if (item.getItemId() == R.id.menu_item_addevent) {
			if (new AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
				// show dialog box
				UserRegistration.upgradeuser(getSherlockActivity());
				return false;
			} else {
				fragmentTransaction = fragmentManager.beginTransaction();
				fragment = new CreateEventFragment();
				fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				// fragmentTransaction.detach(this);
				fragmentTransaction.replace(android.R.id.content, fragment, "events");
				fragmentTransaction.addToBackStack(fragment.getTag());
				fragmentTransaction.commit();
				return true;
			}
			// } else if (item.getItemId() == R.id.menu_item_todayevent) {
			// fragmentTransaction = fragmentManager.beginTransaction();
			// fragment = new EventsListingFragment();
			// args = new Bundle();
			// args.putString(EventsListingFragment.ARG_QUERY_TODAY, "");
			// fragment.setArguments(args);
			// fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			// // fragmentTransaction.detach(currentFragment);
			// fragmentTransaction.replace(android.R.id.content, fragment,
			// "events");
			// fragmentTransaction.addToBackStack(fragment.getTag());
			// fragmentTransaction.commit();
			// return true;
			// } else if (item.getItemId() == R.id.menu_item_myevents) {
			// fragmentTransaction = fragmentManager.beginTransaction();
			// fragment = new EventsListingFragment();
			// args = new Bundle();
			// args.putBoolean(SearchFragment.ARG_MY, true);
			// fragment.setArguments(args);
			// fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			// // fragmentTransaction.detach(this);
			// fragmentTransaction.replace(android.R.id.content, fragment,
			// "events");
			// fragmentTransaction.addToBackStack(fragment.getTag());
			// fragmentTransaction.commit();
			// return true;
		} else if (item.getItemId() == R.id.submenu_search) {
			fragmentTransaction = fragmentManager.beginTransaction();
			fragment = new SearchFragment();
			args = new Bundle();
			args.putString(CategoryHelper.CATEGORY_TYPE_EVENTS, CategoryHelper.CATEGORY_TYPE_EVENTS);
			fragment.setArguments(args);
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			// fragmentTransaction.detach(this);
			fragmentTransaction.replace(android.R.id.content, fragment, "events");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
}
