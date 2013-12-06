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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.notifications.NotificationsSherlockFragmentDT;

public class EventsMenuFragment extends NotificationsSherlockFragmentDT {

	private OnClickListener allEventsClicked, createEventClicked, myEventsClicked;
	private FragmentManager fragmentManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fragmentManager = getSherlockActivity().getSupportFragmentManager();
		createListeners();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.old_simpleeventsmenu, container, false);
	}

	// wires the listeners
	@Override
	public void onStart() {
		super.onStart();
		Button b = (Button) getView().findViewById(R.id.btn_all_events);
		b.setOnClickListener(allEventsClicked);
		b = (Button) getView().findViewById(R.id.btn_create_event);
		b.setOnClickListener(createEventClicked);
		b = (Button) getView().findViewById(R.id.btn_my_events);
		b.setOnClickListener(myEventsClicked);
	}

	private void createListeners() {
		allEventsClicked = new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				Fragment fragment = new AllEventsFragment();
				fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				fragmentTransaction.replace(R.id.fragment_container, fragment, "events");
				fragmentTransaction.addToBackStack(fragment.getTag());
				fragmentTransaction.commit();
			}
		};

		createEventClicked = new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				Fragment fragment = new CreateEventFragment();
				fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				fragmentTransaction.replace(R.id.fragment_container, fragment, "events");
				fragmentTransaction.addToBackStack(fragment.getTag());
				fragmentTransaction.commit();
			}
		};

		myEventsClicked = new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast toast = Toast.makeText(getSherlockActivity().getApplicationContext(), getSherlockActivity()
						.getApplicationContext().getResources().getString(R.string.toast_my_events_clicked), Toast.LENGTH_LONG);
				toast.show();
			}
		};

	}
}
