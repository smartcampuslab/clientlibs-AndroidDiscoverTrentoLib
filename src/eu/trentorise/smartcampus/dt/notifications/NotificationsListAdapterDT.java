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
package eu.trentorise.smartcampus.dt.notifications;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import eu.trentorise.smartcampus.communicator.model.EntityObject;
import eu.trentorise.smartcampus.communicator.model.Notification;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.data.Constants;

public class NotificationsListAdapterDT extends ArrayAdapter<Notification> {

	private static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
	private Context mContext;
	private int layoutResourceId;

	public NotificationsListAdapterDT(Context context, int layoutResourceId) {
		super(context, layoutResourceId);
		this.mContext = context;
		this.layoutResourceId = layoutResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		Holder holder = null;
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new Holder();
			holder.desc = (TextView) row.findViewById(R.id.notification_desc);
			holder.date = (TextView) row.findViewById(R.id.notification_date);
			holder.read = (ImageView) row.findViewById(R.id.notification_read);
			row.setTag(holder);
		} else {
			holder = (Holder) row.getTag();
		}

		Notification notification = getItem(position);

		if (notification.isReaded()) {
			holder.read.setVisibility(View.INVISIBLE);
		} else {
			holder.read.setVisibility(View.VISIBLE);
		}

		buildHolder(holder, notification);

		return row;
	}

	private class Holder {
		public TextView desc;
		public TextView date;
		public ImageView read;
	}

	/*
	 * Builders
	 */
	private void buildHolder(Holder holder, Notification notification) {
		// missing custom data
		if (notification.getEntities() == null || notification.getEntities().isEmpty() || notification.getEntities().size() > 2) {
			holder.desc.setText(notification.getTitle() + "\n" + notification.getDescription());
		}

		EntityObject event = null;
		EntityObject location = null;
		EntityObject story = null;

		for (EntityObject eb : notification.getEntities()) {
			String type = eb.getType();

			if (type.equalsIgnoreCase(Constants.TYPE_EVENT)) {
				event = eb;
			} else if (type.equalsIgnoreCase(Constants.TYPE_LOCATION)) {
				location = eb;
			} else if (type.equalsIgnoreCase(Constants.TYPE_STORY)) {
				story = eb;
			}
		}

		if (notification.getEntities().size() == 2) {
			// new
			if (event != null && location != null) {
				holder.desc
						.setText(mContext.getString(R.string.notifications_event_new, event.getTitle(), location.getTitle()));
			} else if (location != null && story != null) {
				holder.desc
						.setText(mContext.getString(R.string.notifications_story_new, story.getTitle(), location.getTitle()));
			}
		} else {
			if (event != null) {
				holder.desc.setText(mContext.getString(R.string.notifications_event_updated, event.getTitle()));
			} else if (location != null) {
				holder.desc.setText(mContext.getString(R.string.notifications_location_updated, location.getTitle()));
			} else if (story != null) {
				holder.desc.setText(mContext.getString(R.string.notifications_story_updated, story.getTitle()));
			}
		}

		Calendar notificationDateTime = Calendar.getInstance();
		notificationDateTime.setTimeInMillis(notification.getTimestamp());
		holder.date.setText(dateFormat.format(notificationDateTime.getTime()));
	}

}
