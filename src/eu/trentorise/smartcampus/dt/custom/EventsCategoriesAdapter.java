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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import eu.trentorise.smartcampus.dt.custom.CategoryHelper.CategoryDescriptor;
import eu.trentorise.smartcampus.dt.fragments.events.EventsListingFragment;
import eu.trentorise.smartcampus.dt.fragments.search.SearchFragment;

public class EventsCategoriesAdapter extends BaseAdapter {

	private Context mContext;
	private int layoutResourceId;
	private FragmentManager fragmentManager;

	public EventsCategoriesAdapter(Context mContext, int layoutResourceId) {
		this.mContext = mContext;
		this.layoutResourceId = layoutResourceId;
	}

	public EventsCategoriesAdapter(Context mContext, int layoutResourceId, FragmentManager fragmentManager) {
		this.mContext = mContext;
		this.layoutResourceId = layoutResourceId;
		this.fragmentManager = fragmentManager;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// CategoryDescriptor cd = CategoryHelper.EVENT_CATEGORIES[position];
		CategoryDescriptor cd = CategoryHelper.getEventCategoryDescriptorsFiltered()[position];

		LayoutInflater inflater = LayoutInflater.from(mContext);
		Button button = (Button) inflater.inflate(layoutResourceId, parent, false);
		// button.setText(CategoryHelper.EVENT_CATEGORIES[position].description);
		button.setTag(cd);
		button.setText(mContext.getResources().getString(cd.description));
		button.setCompoundDrawablesWithIntrinsicBounds(null, mContext.getResources().getDrawable(cd.thumbnail), null, null);
		button.setOnClickListener(new EventsCategoriesOnClickListener());

		return button;
	}

	public class EventsCategoriesOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			String cat = ((CategoryDescriptor) v.getTag()).category;
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			EventsListingFragment fragment = new EventsListingFragment();
			Bundle args = new Bundle();
			args.putString(SearchFragment.ARG_CATEGORY, cat);
			fragment.setArguments(args);
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(android.R.id.content, fragment, "events");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
		}
	}

	@Override
	public int getCount() {
		return CategoryHelper.getEventCategoryDescriptorsFiltered().length;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

}
