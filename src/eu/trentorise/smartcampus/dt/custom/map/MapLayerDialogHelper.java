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
package eu.trentorise.smartcampus.dt.custom.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import eu.trentorise.smartcampus.dt.custom.CategoryHelper;
import eu.trentorise.smartcampus.dt.custom.CategoryHelper.CategoryDescriptor;
import eu.trentorise.smartcampus.dt.fragments.home.EventsAdapter;
import eu.trentorise.smartcampus.dt.fragments.home.EventsAdapter.AddToArray;

public class MapLayerDialogHelper {

	public static Dialog createPOIDialog(final Context ctx, final MapItemsHandler handler, String title, String... selected) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle(title);

		HashSet<String> selectedSet = new HashSet<String>();
		if (selected != null) {
			selectedSet.addAll(Arrays.asList(selected));
		}

		final CategoryDescriptor[] items = CategoryHelper.getPOICategoryDescriptorsFiltered();

		final String[] itemsDescriptions = new String[items.length];
		for (int i = 0; i < items.length; i++) {
			itemsDescriptions[i] = ctx.getResources().getString(items[i].description);
		}

		boolean[] checkedItems = new boolean[items.length];
		for (int i = 0; i < items.length; i++) {
			checkedItems[i] = selectedSet.contains(items[i].category);
		}

		final ArrayList<String> newSelected = new ArrayList<String>(selectedSet);

		builder.setMultiChoiceItems(itemsDescriptions, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				if (isChecked) {
					// If the user checked the item, add it to the selected
					// items
					newSelected.add(items[which].category);
				} else if (newSelected.contains(items[which].category)) {
					// Else, if the item is already in the array, remove it
					newSelected.remove(items[which].category);
				}
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				handler.setPOICategoriesToLoad(newSelected.toArray(new String[newSelected.size()]));
				dialog.dismiss();
			}
		});

		return builder.create();
	}

	public static Dialog createEventsDialog(final Context ctx, final MapItemsHandler handler, String title, String... selected) {

		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle(title);
		HashSet<String> selectedSet = new HashSet<String>();
		if (selected != null) {
			selectedSet.addAll(Arrays.asList(selected));
		}

		final CategoryDescriptor[] items = getAllItems();
		final String[] itemsDescriptions = new String[items.length];

		for (int i = 0; i < items.length; i++) {
			itemsDescriptions[i] = ctx.getResources().getString(items[i].description);
		}

		boolean[] checkedItems = new boolean[items.length];
		for (int i = 0; i < items.length; i++) {
			checkedItems[i] = selectedSet.contains(items[i].category);
		}

		final ArrayList<String> newSelected = new ArrayList<String>(selectedSet);

		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				handler.setEventCategoriesToLoad(newSelected.toArray(new String[newSelected.size()]));
				dialog.dismiss();
			}
		});
		createAdapterAndInterface(newSelected,items, ctx,  itemsDescriptions, checkedItems,  builder);
		return builder.create();

	}

	private static void createAdapterAndInterface(final ArrayList<String> newSelected, final CategoryDescriptor[] items,Context ctx, String[] itemsDescriptions, boolean[] checkedItems, AlertDialog.Builder builder ) {
		EventsAdapter adapter;
		class AddToArrayImpl implements AddToArray {

			@Override
			public void addtoarray(boolean isChecked, int which) {
				if (isChecked) {
					// If the user checked the item, add it to the selected
					// items
					newSelected.add(items[which].category);
				} else if (newSelected.contains(items[which].category)) {
					// Else, if the item is already in the array, remove it
					newSelected.remove(items[which].category);
				}
			}

		}
		final AddToArrayImpl addToArray = new AddToArrayImpl();

		adapter = new EventsAdapter(ctx, itemsDescriptions, checkedItems, addToArray, true);
		builder.setAdapter(adapter, null);
	}

	private static CategoryDescriptor[] getAllItems() {
		CategoryDescriptor[] itemsNotToday = CategoryHelper.getEventCategoryDescriptorsFiltered();
		CategoryDescriptor todaysEvents = CategoryHelper.TODAY_EVENTS;

		CategoryDescriptor[] copy = new CategoryDescriptor[itemsNotToday.length + 1];

		for (int index = 0; index < itemsNotToday.length + 1; index++) {

			if (index == 0) {

				copy[index] = todaysEvents;

			} else {

				copy[index] = itemsNotToday[index - 1];

			}

		}
		return copy;
	}

	// public static void changeColor(AlertDialog eventsDialog, Context
	// mContext) {
	// if
	// (eventsDialog.getListView().getAdapter().getItem(0).toString().compareTo(mContext.getString(R.string.menu_item_todayevent_text))==0)
	// {
	// /*change the adapter*/
	// CheckedTextView view = (CheckedTextView)
	// eventsDialog.getListView().getAdapter().getItem(0);
	// view.setText("ijdfiasdf");
	// view.setTextColor(mContext.getResources().getColor(android.R.color.holo_orange_dark));
	// // ((ArrayAdapter)
	// eventsDialog.getListView().getAdapter()).notifyDataSetChanged();
	// view.invalidate();
	//
	// }
	// }

}
