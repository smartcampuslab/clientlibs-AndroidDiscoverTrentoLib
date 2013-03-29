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
package eu.trentorise.smartcampus.dt.fragments.events;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.dt.custom.CategoryHelper;
import eu.trentorise.smartcampus.dt.custom.CategoryHelper.CategoryDescriptor;
import eu.trentorise.smartcampus.dt.custom.DatePickerDialogFragment;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
import eu.trentorise.smartcampus.dt.fragments.pois.CreatePoiFragment;
import eu.trentorise.smartcampus.dt.fragments.pois.CreatePoiFragment.PoiHandler;
import eu.trentorise.smartcampus.dt.fragments.stories.AddStepToStoryFragment;
import eu.trentorise.smartcampus.dt.model.CommunityData;
import eu.trentorise.smartcampus.dt.model.Concept;
import eu.trentorise.smartcampus.dt.model.EventObject;
import eu.trentorise.smartcampus.dt.model.POIObject;
import eu.trentorise.smartcampus.dt.model.UserEventObject;
import eu.trentorise.smartcampus.dt.notifications.NotificationsSherlockFragmentDT;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class CreateEventFragment extends NotificationsSherlockFragmentDT implements TaggingDialog.OnTagsSelectedListener,
		TaggingDialog.TagProvider {

	private POIObject poi = null;
	private View view = null;
	private CreatePoiFromEvent poiHandler = new CreatePoiFromEvent();
	private AutoCompleteTextView poiField;
	public static String ARG_EVENT = "event";

	private CategoryDescriptor[] categoryDescriptors;

	private EventObject eventObject;

	@Override
	public void onTagsSelected(Collection<SemanticSuggestion> suggestions) {
		eventObject.getCommunityData().setTags(Concept.convertSS(suggestions));
		((EditText) getView().findViewById(R.id.event_tags)).setText(Concept.toSimpleString(eventObject.getCommunityData()
				.getTags()));
	}

	@Override
	public void onSaveInstanceState(Bundle arg0) {
		super.onSaveInstanceState(arg0);
		arg0.putSerializable(ARG_EVENT, eventObject);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(false);

		if (savedInstanceState != null && savedInstanceState.containsKey(ARG_EVENT)
				&& savedInstanceState.getSerializable(ARG_EVENT) != null) {
			eventObject = (EventObject) savedInstanceState.get(ARG_EVENT);
		} else if (getArguments() != null && getArguments().containsKey(ARG_EVENT)
				&& getArguments().getSerializable(ARG_EVENT) != null) {
			eventObject = (EventObject) getArguments().getSerializable(ARG_EVENT);
		} else {
			eventObject = new UserEventObject();
			if (getArguments() != null && getArguments().containsKey(EventsListingFragment.ARG_CATEGORY)) {
				eventObject.setType(getArguments().getString(EventsListingFragment.ARG_CATEGORY));
			}
		}
		if (eventObject.getPoiId() != null) {
			poi = DTHelper.findPOIById(eventObject.getPoiId());

		}
		if (eventObject.getCommunityData() == null)
			eventObject.setCommunityData(new CommunityData());
	}

	@Override
	public void onStart() {
		super.onStart();

		// date and time will be returned as tags
		final EditText dateEditText = (EditText) getView().findViewById(R.id.event_date);
		dateEditText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment f = DatePickerDialogFragment.newInstance((EditText) v);
				if (dateEditText.getText() != null)
					f.setArguments(DatePickerDialogFragment.prepareData(dateEditText.getText().toString()));
				f.show(getSherlockActivity().getSupportFragmentManager(), "datePicker");
			}
		});
		if (poi != null) {
			poiField.setText(poi.getTitle());
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.createeventform, container, false);

		categoryDescriptors = CategoryHelper.EVENT_CATEGORIES;

		Spinner categories = (Spinner) view.findViewById(R.id.event_category);
		int selected = -1;
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.dd_list, R.id.dd_textview);
		categories.setAdapter(adapter);
		String mainCat = CategoryHelper.getMainCategory(eventObject.getType());
		for (int i = 0; i < categoryDescriptors.length; i++) {
			adapter.add(getSherlockActivity().getApplicationContext().getResources()
					.getString(categoryDescriptors[i].description));
			if (categoryDescriptors[i].category.equals(mainCat)) {
				selected = i;
			}
		}
		if (selected >= 0) {
			categories.setSelection(selected);
		} else {
			categories.setSelection(categoryDescriptors.length -1);
		}

		EditText title = (EditText) view.findViewById(R.id.event_title);
		title.setText(eventObject.getTitle());

		EditText dateEdit = (EditText) view.findViewById(R.id.event_date);

		EditText timing = (EditText) view.findViewById(R.id.event_timing_et);
		// timing is editable only in own UserEvent
		if (eventObject.createdByUser() && DTHelper.isOwnedObject(eventObject)) {
			// set edit visible, set textview gone
			if (eventObject.getTiming() != null) {
				timing.setText(eventObject.getTimingFormatted());
			}
			timing.setEnabled(true);
		} else {
			// set edit gone, set textview visible
			timing.setText(eventObject.getTimingFormatted());
			timing.setEnabled(false);
		}

		if (eventObject.getFromTime() != null && eventObject.getFromTime() > 0) {
			dateEdit.setText(DatePickerDialogFragment.DATEFORMAT.format(new Date(eventObject.getFromTime())));
		} else {
			Calendar c = Calendar.getInstance();
			c.set(Calendar.MINUTE, 0);
			dateEdit.setText(DatePickerDialogFragment.DATEFORMAT.format(c.getTime()));
		}
		poiField = (AutoCompleteTextView) view.findViewById(R.id.event_place);
		ArrayAdapter<String> poiAdapter = new ArrayAdapter<String>(getSherlockActivity(), R.layout.dd_list, R.id.dd_textview,
				DTHelper.getAllPOITitles());
		poiField.setAdapter(poiAdapter);
		if (poi != null) {
			poiField.setText(poi.getTitle());
		}

		ImageButton addPoiBtn = (ImageButton) view.findViewById(R.id.btn_event_add_poi);
		addPoiBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentTransaction fragmentTransaction;
				Fragment fragment;

				fragmentTransaction = getSherlockActivity().getSupportFragmentManager().beginTransaction();
				fragment = new CreatePoiFragment();
				Bundle args = new Bundle();
				args.putParcelable(AddStepToStoryFragment.ARG_STEP_HANDLER, poiHandler);
				fragment.setArguments(args);
				fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				fragmentTransaction.replace(android.R.id.content, fragment, "pois");
				fragmentTransaction.addToBackStack(fragment.getTag());
				fragmentTransaction.commit();
			}
		});

		ImageButton locationBtn = (ImageButton) view.findViewById(R.id.btn_event_locate);
		locationBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(getActivity(), POISelectActivity.class), POISelectActivity.RESULT_SELECTED);
			}
		});

		EditText notes = (EditText) view.findViewById(R.id.event_notes);
		notes.setText(eventObject.getCommunityData().getNotes());

		// Cannot edit title, date, poi, category, and notes for ServiceEvent
		// and non-owned UserEvent
		if (!eventObject.createdByUser() || !DTHelper.isOwnedObject(eventObject)) {
			title.setEnabled(false);
			// if (eventObject.getType() != null &&
			// !eventObject.isTypeUserDefined()) {
			categories.setEnabled(false);
			// }
			// if (eventObject.getFromTime() != null &&
			// eventObject.getFromTime() > 0 &&
			// !eventObject.isFromTimeUserDefined()) {
			dateEdit.setEnabled(false);
			// }
			// if (poi != null && !eventObject.isPoiIdUserDefined()) {
			poiField.setEnabled(false);
			locationBtn.setEnabled(false);
			addPoiBtn.setEnabled(false);
			// }
			notes.setEnabled(false);
		}

		EditText tagsEdit = (EditText) view.findViewById(R.id.event_tags);
		tagsEdit.setText(Concept.toSimpleString(eventObject.getCommunityData().getTags()));
		tagsEdit.setClickable(true);
		tagsEdit.setFocusableInTouchMode(false);
		tagsEdit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TaggingDialog taggingDialog = new TaggingDialog(getActivity(), CreateEventFragment.this,
						CreateEventFragment.this, Concept.convertToSS(eventObject.getCommunityData().getTags()));
				taggingDialog.show();
			}
		});

		Button cancel = (Button) view.findViewById(R.id.btn_createevent_cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getSherlockActivity().getSupportFragmentManager().popBackStack();
			}

		});

		Button save = (Button) view.findViewById(R.id.btn_createevent_ok);
		save.setOnClickListener(new SaveEvent());

		return view;
	}

	private Integer validate(EventObject data) {
		Integer result = null;
		if (data.getTitle() == null || data.getTitle().length() == 0)
			return R.string.create_title;
		// if (data.getFromTime() == null)
		// return R.string.createevent_timestart;
		// if (data.getToTime() == null)
		// return R.string.createevent_timeend;
		// if (data.getToTime() <= data.getFromTime())
		// return R.string.createevent_timeend;
		if (data.getPoiId() == null)
			return R.string.create_place;
		if (data.getType() == null || data.getType().length() == 0)
			return R.string.create_cat;
		return result;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent result) {
		super.onActivityResult(requestCode, resultCode, result);
		if (resultCode == POISelectActivity.RESULT_SELECTED) {
			poi = (POIObject) result.getSerializableExtra("poi");
			AutoCompleteTextView text = (AutoCompleteTextView) view.findViewById(R.id.event_place);
			text.setText(poi.getTitle());
			for (int i = 0; i < text.getAdapter().getCount(); i++) {
				if (poi.getTitle().equals((text.getAdapter().getItem(i)))) {
					text.setListSelection(i);
				}
			}
		}
	}

	@Override
	public List<SemanticSuggestion> getTags(CharSequence text) {
		try {
			return DTHelper.getSuggestions(text);
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	private class CreateEventProcessor extends AbstractAsyncTaskProcessor<EventObject, Boolean> {

		public CreateEventProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public Boolean performAction(EventObject... params) throws SecurityException, Exception {
			return DTHelper.saveEvent(params[0]);
		}

		@Override
		public void handleResult(Boolean result) {
			getSherlockActivity().getSupportFragmentManager().popBackStack();
			if (result) {
				Toast.makeText(getSherlockActivity(), R.string.event_create_success, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getSherlockActivity(), R.string.update_success, Toast.LENGTH_SHORT).show();
			}
		}

	}

	private class SaveEvent implements OnClickListener {
		@Override
		public void onClick(View v) {
			CharSequence desc = ((EditText) view.findViewById(R.id.event_notes)).getText();
			if (desc != null) {
				eventObject.getCommunityData().setNotes(desc.toString());
			}
			CharSequence title = ((EditText) view.findViewById(R.id.event_title)).getText();
			if (title != null) {
				eventObject.setTitle(title.toString());
			}

			String catString = ((Spinner) view.findViewById(R.id.event_category)).getSelectedItem().toString();
			String cat = getCategoryDescriptorByDescription(catString).category;

			AutoCompleteTextView eventPlace = (AutoCompleteTextView) view.findViewById(R.id.event_place);
			if ((poi == null || !poi.getTitle().equals(eventPlace.getText().toString())) && eventPlace.getText() != null
					&& eventPlace.getText().length() > 0) {
				poi = DTHelper.findPOIByTitle(eventPlace.getText().toString());
			}

			CharSequence datestr = ((EditText) view.findViewById(R.id.event_date)).getText();

			if (datestr == null || datestr.length() == 0) {
				Toast.makeText(
						getActivity(),
						getActivity().getResources().getString(R.string.createevent_date) + " "
								+ getActivity().getResources().getString(R.string.msg_field_required), Toast.LENGTH_SHORT)
						.show();
				return;
			}
			// if (eventObject.getTiming() == null ||
			// eventObject.isFromTimeUserDefined() ||
			// DTHelper.isOwnedObject(eventObject)) {
			if (DTHelper.isOwnedObject(eventObject)) {
				CharSequence timingstr = ((EditText) view.findViewById(R.id.event_timing_et)).getText();
				if (timingstr == null || timingstr.length() == 0) {
					Toast.makeText(
							getActivity(),
							getActivity().getResources().getString(R.string.createevent_timing) + " "
									+ getActivity().getResources().getString(R.string.msg_field_required), Toast.LENGTH_SHORT)
							.show();
					return;
				}
				eventObject.setTiming(timingstr.toString());
			}

			Calendar cal = Calendar.getInstance();
			try {
				Date d = DatePickerDialogFragment.DATEFORMAT.parse(datestr.toString());
				cal.setTime(d);
			} catch (ParseException e) {
				Toast.makeText(
						getActivity(),
						getResources().getString(R.string.toast_incorrect) + " "
								+ getResources().getString(R.string.createevent_date), Toast.LENGTH_SHORT).show();
				return;
			}
			eventObject.setFromTime(cal.getTimeInMillis());

			eventObject.setType(cat);
			if (poi != null) {
				eventObject.setPoiId(poi.getId());
			}
			Integer missing = validate(eventObject);
			if (missing != null) {
				Toast.makeText(
						getActivity(),
						getActivity().getResources().getString(missing)
								+ " "
								+ getSherlockActivity().getApplicationContext().getResources()
										.getString(R.string.toast_is_required), Toast.LENGTH_SHORT).show();
				return;
			}

			new SCAsyncTask<EventObject, Void, Boolean>(getActivity(), new CreateEventProcessor(getActivity()))
					.execute(eventObject);
		}

	}

	private CategoryDescriptor getCategoryDescriptorByDescription(String desc) {
		for (CategoryDescriptor cd : categoryDescriptors) {
			String catDesc = getSherlockActivity().getApplicationContext().getResources().getString(cd.description);
			if (catDesc.equalsIgnoreCase(desc)) {
				return cd;
			}
		}

		return null;
	}

	private class CreatePoiFromEvent implements PoiHandler, Parcelable {
		@Override
		public void addPoi(POIObject poi) {

			// add the step, notify to the adapter and go back to this fragment
			CreateEventFragment.this.poi = poi;
			CreateEventFragment.this.eventObject.assignPoi(poi);
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {

		}

	}
}
