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
/*package eu.trentorise.smartcampus.custom;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import eu.trentorise.smartcampus.dt.R;

public class ContentsAdapter extends ArrayAdapter<Content> {

	Context context;
	int layoutResourceId;
	List<Content> contentsList;

	public ContentsAdapter(Context context, int layoutResourceId, List<Content> contentsList) {
		super(context, layoutResourceId, contentsList);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
		this.contentsList = contentsList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		DataHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new DataHolder();
			holder.content_images = (LinearLayout) row.findViewById(R.id.content_images);
			holder.content_title = (TextView) row.findViewById(R.id.content_title);
			holder.content_tags = (TextView) row.findViewById(R.id.content_tags);
			holder.content_date = (TextView) row.findViewById(R.id.content_date);

			row.setTag(holder);
		} else {
			holder = (DataHolder) row.getTag();
		}

		Content content = contentsList.get(position);
//		for (String s : content.getImagesLinks()) {
//			ImageView imageView = new ImageView(context);
//			imageView.setImageURI(Uri.parse(s));
//			holder.content_images.addView(imageView);
//		}
		holder.content_title.setText(content.getTitle());
		String tags = "";
		for (String s : content.getTags()) {
			tags += s + " ";
		}
		holder.content_tags.setText(tags);
		holder.content_date.setText(content.getDate());

		return row;
	}

	static class DataHolder {
		LinearLayout content_images;
		TextView content_title;
		TextView content_tags;
		TextView content_date;
	}
}
*/
