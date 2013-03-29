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

public class MapLayerDialogHelper {

	public static Dialog createDialog(final Context ctx, final MapItemsHandler handler, String title, String... selected) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle(title);

		HashSet<String> selectedSet = new HashSet<String>();
		if (selected != null) {
			selectedSet.addAll(Arrays.asList(selected));
		}

		final CategoryDescriptor[] items = CategoryHelper.getPOICategoryDescriptors();

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
}
