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
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import eu.trentorise.smartcampus.dt.R;

public class ImageAdapter extends BaseAdapter {
	private Context context;
	public static Integer[] thumbnailIds = { R.drawable.event_all, R.drawable.ic_e_concerts, R.drawable.ic_e_parties,
			R.drawable.event_all, R.drawable.ic_e_concerts, R.drawable.ic_e_parties, R.drawable.event_all,
			R.drawable.ic_e_concerts, R.drawable.ic_e_parties, R.drawable.event_all, R.drawable.ic_e_concerts,
			R.drawable.ic_e_parties };

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if (convertView == null) {
			imageView = new ImageView(context);
			imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setPadding(8, 8, 8, 8);
		} else
			imageView = (ImageView) convertView;

		imageView.setImageResource(thumbnailIds[position]);
		return imageView;
	}

	public ImageAdapter(Context c) {
		this.context = c;
	}

	@Override
	public int getCount() {
		return thumbnailIds.length;
	}

	@Override
	public Object getItem(int position) {
		return this.context.getResources().getDrawable(thumbnailIds[position]);
	}

	@Override
	public long getItemId(int position) {
		return thumbnailIds[position];
	}
}
