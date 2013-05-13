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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.CategoryHelper.CategoryDescriptor;
import eu.trentorise.smartcampus.dt.fragments.search.SearchFragment;
import eu.trentorise.smartcampus.dt.fragments.stories.StoriesListingFragment;

public class StoriesCategoriesAdapter extends BaseAdapter {
	private Context context;
	private FragmentManager fragmentManager;

	public StoriesCategoriesAdapter(Context c) {
		this.context = c;
	}

	public StoriesCategoriesAdapter(Context applicationContext, FragmentManager fragmentManager) {
		this.fragmentManager = fragmentManager;
		this.context = applicationContext;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = new ViewHolder();
		CategoryDescriptor cd = CategoryHelper.STORY_CATEGORIES[position];

		if (convertView == null) {
			holder.button = new Button(context);
			holder.button.setTag(cd);
			holder.button.setText(context.getResources().getString(cd.description));
			holder.button.setTextSize(11);
			holder.button.setTextColor(context.getResources().getColor(R.color.sc_light_gray));
			holder.button.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
			holder.button.setCompoundDrawablesWithIntrinsicBounds(null, context.getResources().getDrawable(cd.thumbnail), null,
					null);
			holder.button.setOnClickListener(new StoriesCategoriesOnClickListener());
		} else {
			holder.button = (Button) convertView;
			holder.button.setText(((Button) convertView).getText());
			holder.button.setTextSize(11);
			holder.button.setTextColor(context.getResources().getColor(R.color.sc_light_gray));
			holder.button.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
			holder.button.setCompoundDrawablesWithIntrinsicBounds(null, context.getResources().getDrawable(cd.thumbnail), null,
					null);
			holder.button.setOnClickListener(new StoriesCategoriesOnClickListener());
		}
		return holder.button;
	}

	static class ViewHolder {
		Button button;
	}

	public class StoriesCategoriesOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			String cat = ((CategoryDescriptor) v.getTag()).category;
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			StoriesListingFragment fragment = new StoriesListingFragment();
			Bundle args = new Bundle();
			args.putString(SearchFragment.ARG_CATEGORY, cat);
			fragment.setArguments(args);
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(android.R.id.content, fragment, "stories");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
		}

	}

	@Override
	public int getCount() {
		return CategoryHelper.STORY_CATEGORIES.length;
	}

	@Override
	public Object getItem(int arg0) {
		return CategoryHelper.STORY_CATEGORIES[arg0];
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

}
