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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
import eu.trentorise.smartcampus.dt.model.LocalEventObject;

// in EventsListingFragment
public class EventAdapter extends ArrayAdapter<LocalEventObject> {

	private static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	private static final DateFormat extDateFormat = new SimpleDateFormat("EEEEEE dd/MM/yyyy");
	private Context context;
	private int layoutResourceId;
	private int elementSelected = -1;
	// private EventObject[] data;

	public EventAdapter(Context context, int layoutResourceId) {
		super(context, layoutResourceId);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
		// this.data = data;
	}

	// @Override
	// public void remove(EventObject object) {
	// EventObject[] newData = new EventObject[data.length - 1];
	// int i = 0;
	// for (EventObject o : data) {
	// if (i == newData.length)
	// return;
	// if (!o.getId().equals(object.getId())) {
	// newData[i] = o;
	// }
	// i++;
	// }
	// data = newData;
	// }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		EventPlaceholder e = null;
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(layoutResourceId, parent, false);
			e = new EventPlaceholder();
			e.title = (TextView) row.findViewById(R.id.event_placeholder_title);
			// e.description = (TextView)
			// row.findViewById(R.id.event_placeholder_descr);
			e.location = (TextView) row.findViewById(R.id.event_placeholder_loc);
			e.hour = (TextView) row.findViewById(R.id.event_placeholder_hour);
			e.icon = (ImageView) row.findViewById(R.id.event_placeholder_icon);
			e.vs = (ViewSwitcher) row.findViewById(R.id.event_viewswitecher);
			e.dateSeparator = (TextView) row.findViewById(R.id.date_separator);
			row.setTag(e);
		} else
		{
			e = (EventPlaceholder) row.getTag();
		}
		
		e.event = getItem(position);// data[position];
		e.title.setText(e.event.getTitle());
		// e.description.setText(data[position].getDescription());
		if (e.event.assignedPoi() != null) {
			e.location.setText(DTHelper.poiGetShortAddress(e.event.assignedPoi()));
		} else {
			e.location.setText(null);
		}
		e.hour.setText(e.event.getTimingFormatted());
		Drawable drawable = context.getResources().getDrawable(CategoryHelper.getIconByType(e.event.getType()));

		e.icon.setImageDrawable(drawable);
		// Choose if show the separator or not
		LocalEventObject event = getItem(position);

		Calendar previousEvent = null;
		Calendar currentEvent = Calendar.getInstance();
		;
		currentEvent.setTimeInMillis(event.getFromTime());

		if (position - 1 >= 0) {
			previousEvent = Calendar.getInstance();
			previousEvent.setTimeInMillis(getItem(position - 1).getFromTime());
		}

		if (previousEvent == null || previousEvent.get(Calendar.DATE) != currentEvent.get(Calendar.DATE)) {
			e.dateSeparator.setVisibility(View.VISIBLE);
			// create date
			e.dateSeparator.setText(setDateString(e));
		} else {
			e.dateSeparator.setVisibility(View.GONE);
		}

		return row;
	}

	private String setDateString(EventPlaceholder e) {
		String newdateformatted = new String("");

		Date dateToday = new Date();
		String stringToday = (dateFormat.format(dateToday));
		String stringEvent = (dateFormat.format(new Date(e.event.getFromTime())));

		Calendar cal = Calendar.getInstance();
		cal.setTime(dateToday);
		cal.add(Calendar.DAY_OF_YEAR, 1); // <--
		Date tomorrow = cal.getTime();
		String stringTomorrow = (dateFormat.format(tomorrow));
		// check actual date
		if (stringToday.equals(stringEvent)) {
			// if equal put the Today string
			newdateformatted = stringToday;
			newdateformatted = this.context.getString(R.string.list_event_today) + " " + newdateformatted;
		} else if (stringTomorrow.equals(stringEvent)) {
			// else if it's tomorrow, cat that string
			newdateformatted = stringTomorrow;
			newdateformatted = this.context.getString(R.string.list_event_tomorrow) + " " + newdateformatted;
		}
		// else put the day's name
		else
			newdateformatted = extDateFormat.format(new Date(e.event.getFromTime()));
		return newdateformatted;
	}

	public int getElementSelected() {
		return elementSelected;
	}

	public void setElementSelected(int elementSelected) {
		this.elementSelected = elementSelected;
	}

}
