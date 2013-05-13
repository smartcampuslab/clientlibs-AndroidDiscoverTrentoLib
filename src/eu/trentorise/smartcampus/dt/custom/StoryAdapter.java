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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.model.StoryObject;

public class StoryAdapter extends ArrayAdapter<StoryObject> {

	private Context context;
	private int layoutResourceId;

	public StoryAdapter(Context context, int layoutResourceId) {
		super(context, layoutResourceId);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		StoryPlaceholder s = null;
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(layoutResourceId, parent, false);
			s = new StoryPlaceholder();
			s.title = (TextView) row.findViewById(R.id.story_placeholder_title);
			s.descritpion = (TextView) row.findViewById(R.id.story_placeholder_description);
			s.rating=(RatingBar) row.findViewById(R.id.story_rating);
			s.icon = (ImageView) row.findViewById(R.id.story_placeholder_icon);

			row.setTag(s);

		} else
			s = (StoryPlaceholder) row.getTag();
		
		s.story = getItem(position);
		s.title.setText(s.story.getTitle());
		s.rating.setRating(s.story.getCommunityData().getAverageRating());
		s.descritpion.setText(s.story.getDescription());
		s.icon.setImageDrawable(context.getResources().getDrawable(CategoryHelper.getIconByType(s.story.getType())));

		return row;
	}

}
