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

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
import eu.trentorise.smartcampus.dt.fragments.stories.AddStepToStoryFragment;
import eu.trentorise.smartcampus.dt.fragments.stories.AddStepToStoryFragment.StepHandler;
import eu.trentorise.smartcampus.dt.model.POIObject;
import eu.trentorise.smartcampus.dt.model.StepObject;
import eu.trentorise.smartcampus.dt.model.StoryObject;

/*
 * The adapter composed by number, name and button for deleting
 */
public class StepAdapter extends ArrayAdapter<StepObject> {

	private Context context;
	private int layoutResourceId;
	private List<StepObject> data;
	private FragmentManager fragmentManager;
	private AddStep stepHandler = new AddStep();
	private StoryObject storyObject = null;
	private Activity activity;

	public StepAdapter(Context context, int layoutResourceId,
			List<StepObject> data, StoryObject story,
			FragmentManager fragmentManager, Activity activity) {
		super(context, layoutResourceId, data);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
		this.data = data;
		this.storyObject = story;
		this.fragmentManager = fragmentManager;
		this.activity = activity;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View row = convertView;
		StepPlaceholder s = null;
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(layoutResourceId, parent, false);
			s = new StepPlaceholder();
			s.title = (TextView) row.findViewById(R.id.step_placeholder_title);
			row.setTag(s);
			s.delete = (ImageButton) row
					.findViewById(R.id.step_placeholder_delete);
		} else
			s = (StepPlaceholder) row.getTag();
		s.step = getItem(position);
		if (s.title == null)
			s.title = (TextView) row.findViewById(R.id.step_placeholder_title);
		if (s.step.assignedPoi() == null) {
			StepObject step = storyObject.getSteps().get(position);
			if (step != null) {
				POIObject poi = DTHelper.findPOIById(step.getPoiId());
				if (poi != null) {
					step.assignPoi(poi);
				}
			}
		}
		if (s.step.assignedPoi() != null) {
			s.title.setText((position + 1) + " - " + s.step.assignedPoi().getTitle());
		}
		if (s.delete == null)
			s.delete = (ImageButton) row
					.findViewById(R.id.step_placeholder_delete);
		if (DTHelper.isOwnedObject(storyObject)) {
			s.title.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// edit the step
					FragmentTransaction fragmentTransaction = fragmentManager
							.beginTransaction();
					AddStepToStoryFragment fragment = new AddStepToStoryFragment();
					Bundle args = new Bundle();
					args.putParcelable(AddStepToStoryFragment.ARG_STEP_HANDLER,
							stepHandler);
					args.putSerializable(AddStepToStoryFragment.ARG_STORY_OBJECT,
							storyObject);
					args.putInt(AddStepToStoryFragment.ARG_STEP_POSITION, position);
					fragment.setArguments(args);
					fragmentTransaction
							.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					fragmentTransaction.replace(android.R.id.content, fragment,
							"stories");
					fragmentTransaction.addToBackStack(fragment.getTag());
					fragmentTransaction.commit();

				}
			});
			s.delete.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// create a dialog alarm that ask if you are sure
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					// Add the buttons
					builder.setMessage(context.getString(R.string.sure_delete_step));
					builder.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									// User clicked OK button
									data.remove(position);
									StepAdapter.this.notifyDataSetChanged();
								}
							});
					builder.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									// User cancelled the dialog
									dialog.dismiss();
								}
							});

					// Create the AlertDialog
					AlertDialog dialog = builder.create();
					// remove the element from the array
					dialog.show();

				}
			});
		} else {
			s.delete.setVisibility(View.GONE);
		}

		return row;
	}

	private class AddStep implements StepHandler, Parcelable {

		@Override
		public void addStep(StepObject step) {
			// TODO Auto-generated method stub
			// aggiungi lo step al mio gruppo e segnala che e' modificato
			storyObject.getSteps().add(step);
			StepAdapter.this.notifyDataSetChanged();
			fragmentManager.popBackStack();

		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
		}

		@Override
		public void updateStep(StepObject step, Integer position) {
			storyObject.getSteps().set(position, step);
			StepAdapter.this.notifyDataSetChanged();
			fragmentManager.popBackStack();

		}

	}
}
