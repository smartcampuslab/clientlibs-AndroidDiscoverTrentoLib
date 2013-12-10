package eu.trentorise.smartcampus.dt.notifications;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockMapFragment;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.dt.R;

public class NotificationsSherlockMapFragmentDT extends SherlockMapFragment {

	private static boolean mHiddenNotification;

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setHiddenNotification();
	}
	
//	@Override
//	public void onPrepareOptionsMenu(Menu menu) {
//		onPrepareOptionsMenuNotifications(menu);
//		super.onPrepareOptionsMenu(menu);
//	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return onOptionsItemSelectedNotifications((SherlockFragmentActivity)getActivity(), item);
	}

//	public static void onPrepareOptionsMenuNotifications(Menu menu) {
//		MenuItem item = menu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_notifications, 0, R.string.notifications_unread);
//		item.setIcon(R.drawable.ic_menu_notifications_w);
//		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//		if (mHiddenNotification) {
//
//			if (item != null) {
//				item.setVisible(false);
//			}
//		}
//	}

	public static boolean onOptionsItemSelectedNotifications(SherlockFragmentActivity activity, MenuItem item) {
		if (item.getItemId() == R.id.menu_item_notifications) {
			Intent intent = new Intent(activity.getApplicationContext(), NotificationsFragmentActivityDT.class);
			// intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			activity.startActivity(intent);
			// getSherlockActivity().overridePendingTransition(android.R.anim.slide_in_left,
			// android.R.anim.slide_out_right);
		} else {
			return activity.onOptionsItemSelected(item);
		}

		return true;
	}

	private void setHiddenNotification() {
		try {
			ApplicationInfo ai = getActivity().getPackageManager().getApplicationInfo(
					getActivity().getPackageName(), PackageManager.GET_META_DATA);
			Bundle aBundle = ai.metaData;
			mHiddenNotification = aBundle.getBoolean("hidden-notification");
		} catch (NameNotFoundException e) {
			mHiddenNotification = false;
			Log.e(NotificationsFragmentListDT.class.getName(),
					"you should set the hidden-notification metadata in app manifest");
		}

	}

}
