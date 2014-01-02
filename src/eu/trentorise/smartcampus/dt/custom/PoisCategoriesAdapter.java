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
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.CategoryHelper.CategoryDescriptor;
import eu.trentorise.smartcampus.dt.fragments.pois.PoisListingFragment;
import eu.trentorise.smartcampus.dt.fragments.search.SearchFragment;

public class PoisCategoriesAdapter extends BaseAdapter {

	private Context mContext;
	private int layoutResourceId;
	private FragmentManager fragmentManager;

	public PoisCategoriesAdapter(Context mContext, int layoutResourceId) {
		this.mContext = mContext;
		this.layoutResourceId = layoutResourceId;
	}

	public PoisCategoriesAdapter(Context mContext, int layoutResourceId, FragmentManager fragmentManager) {
		this.mContext = mContext;
		this.layoutResourceId = layoutResourceId;
		this.fragmentManager = fragmentManager;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CategoryDescriptor cd = CategoryHelper.getPOICategoryDescriptorsFiltered()[position];

		LayoutInflater inflater = LayoutInflater.from(mContext);
		Button button = (Button) inflater.inflate(layoutResourceId, parent, false);
		button.setTag(cd);
		button.setText(mContext.getResources().getString(cd.description));
		button.setCompoundDrawablesWithIntrinsicBounds(null, mContext.getResources().getDrawable(cd.thumbnail), null, null);
		button.setOnClickListener(new PoisCategoriesOnClickListener());

		return button;
	}

	public class PoisCategoriesOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			String cat = ((CategoryDescriptor) v.getTag()).category;
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			PoisListingFragment fragment = new PoisListingFragment();
			Bundle args = new Bundle();
			args.putString(SearchFragment.ARG_CATEGORY, cat);
			fragment.setArguments(args);
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(R.id.fragment_container, fragment, "pois");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
		}
	}

	@Override
	public int getCount() {
		return CategoryHelper.getPOICategoryDescriptorsFiltered().length;
	}

	@Override
	public Object getItem(int arg0) {
		return CategoryHelper.POI_CATEGORIES[arg0];
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

}
