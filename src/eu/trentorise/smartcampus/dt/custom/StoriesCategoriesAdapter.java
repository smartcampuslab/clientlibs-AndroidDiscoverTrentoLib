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

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import eu.trentorise.smartcampus.dt.custom.CategoryHelper.CategoryDescriptor;
import eu.trentorise.smartcampus.dt.fragments.search.SearchFragment;
import eu.trentorise.smartcampus.dt.fragments.stories.AllStoriesFragment;
import eu.trentorise.smartcampus.dt.fragments.stories.StoriesListingFragment;

public class StoriesCategoriesAdapter extends ArrayAdapter<CategoryDescriptor> {
	private Context mContext;
	private int layoutResourceId;
	private FragmentManager fragmentManager;
	private Fragment fragment;
	
	public StoriesCategoriesAdapter(AllStoriesFragment allStoriesFragment, Context mContext, int layoutResourceId, List<CategoryDescriptor> list,
			FragmentManager fragmentManager) {
		super(mContext, layoutResourceId, list);
		this.mContext = mContext;
		this.layoutResourceId = layoutResourceId;
		this.fragmentManager = fragmentManager;
		this.fragment = allStoriesFragment;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CategoryDescriptor cd = getItem(position);

		LayoutInflater inflater = LayoutInflater.from(mContext);
		Button button = (Button) inflater.inflate(layoutResourceId, parent, false);
		button.setTag(cd);
		button.setText(mContext.getResources().getString(cd.description));
		button.setCompoundDrawablesWithIntrinsicBounds(null, mContext.getResources().getDrawable(cd.thumbnail), null, null);
		button.setOnClickListener(new StoriesCategoriesOnClickListener(cd));

		return button;
	}

	public class StoriesCategoriesOnClickListener implements OnClickListener {

		private CategoryDescriptor cd;

		public StoriesCategoriesOnClickListener(CategoryDescriptor cd) {
			this.cd = cd;
		}

		@Override
		public void onClick(View v) {
			String cat = ((CategoryDescriptor) v.getTag()).category;
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			StoriesListingFragment fragment = new StoriesListingFragment();
			Bundle args = new Bundle();

			if (CategoryHelper.CATEGORY_MY.equals(cd.category)) {
				args.putBoolean(SearchFragment.ARG_MY, true);
			} else {
				args.putString(SearchFragment.ARG_CATEGORY, cat);
			}

			fragment.setArguments(args);
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(StoriesCategoriesAdapter.this.fragment.getId(), fragment, "stories");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
		}

	}

}
