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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class DatePickerDialogFragment extends SherlockDialogFragment implements DatePickerDialog.OnDateSetListener {

	public final static SimpleDateFormat DATEFORMAT = new SimpleDateFormat("dd/MM/yyyy");
	private static final String DATA = "data";
	private EditText dateEditText;

	public static DatePickerDialogFragment newInstance(EditText dateEditText) {
		DatePickerDialogFragment f = new DatePickerDialogFragment();
		f.setDateEditText(dateEditText);
		return f;
	}

	public static Bundle prepareData(String date) {
		Bundle b = new Bundle();
		b.putString(DATA, date);
		return b;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Calendar c = Calendar.getInstance();
		
		if (getArguments() != null && getArguments().containsKey(DATA)) {
			try {
				Date d = DATEFORMAT.parse((String)getArguments().getString(DATA));
				c.setTime(d);
			} catch (ParseException e) {
			}
		}

		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);

		// Create a new instance of DatePickerDialog and return it
		return new DatePickerDialog(getSherlockActivity(), this, year, month, day);
	}

	public void onDateSet(DatePicker view, int year, int month, int day) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, day);
		Date date = new Date(calendar.getTimeInMillis());
		String formattedDate = DATEFORMAT.format(date);
		getDateEditText().setTag(date);
		getDateEditText().setText(formattedDate);
		getDialog().dismiss();
	}

	public EditText getDateEditText() {
		return dateEditText;
	}

	public void setDateEditText(EditText dateEditText) {
		this.dateEditText = dateEditText;
	}

}
