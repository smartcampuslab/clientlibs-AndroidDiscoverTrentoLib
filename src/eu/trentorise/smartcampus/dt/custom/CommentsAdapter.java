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
import android.widget.TextView;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.model.TmpComment;

public class CommentsAdapter extends ArrayAdapter<TmpComment> {

	private Context context;
	private int layoutResourceId;
	private TmpComment[] data;

	public CommentsAdapter(Context context, int layoutResourceId,
			TmpComment[] data) {
		super(context, layoutResourceId, data);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
		this.data = data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		DataHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new DataHolder();
			holder.text_tv = (TextView) row.findViewById(R.id.comment_text);
			holder.author_tv = (TextView) row.findViewById(R.id.comment_author);
			holder.date_tv = (TextView) row.findViewById(R.id.comment_date);

			row.setTag(holder);
		} else
			holder = (DataHolder) row.getTag();

		holder.text_tv.setText(data[position].getText());
		holder.author_tv.setText(data[position].getAuthor());
		holder.date_tv.setText(data[position].getDate().toString());

		return row;
	}

	public static class DataHolder {
		public TextView text_tv, author_tv, date_tv;
	}

}
