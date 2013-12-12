package eu.trentorise.smartcampus.dt.notifications;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockListFragment;

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.feedback.utils.FeedbackFragmentInflater;
import eu.trentorise.smartcampus.communicator.model.EntityObject;
import eu.trentorise.smartcampus.communicator.model.Notification;
import eu.trentorise.smartcampus.communicator.model.NotificationFilter;
import eu.trentorise.smartcampus.communicator.model.NotificationsConstants.ORDERING;
import eu.trentorise.smartcampus.dt.DTParamsHelper;
import eu.trentorise.smartcampus.dt.DiscoverTrentoActivity;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.dt.custom.data.Constants;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
import eu.trentorise.smartcampus.dt.fragments.events.EventDetailsFragment;
import eu.trentorise.smartcampus.dt.fragments.pois.PoiDetailsFragment;
import eu.trentorise.smartcampus.dt.fragments.stories.StoryDetailsFragment;
import eu.trentorise.smartcampus.dt.model.LocalEventObject;
import eu.trentorise.smartcampus.notifications.NotificationsHelper;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;
import eu.trentorise.smartcampus.territoryservice.model.EventObject;
import eu.trentorise.smartcampus.territoryservice.model.POIObject;
import eu.trentorise.smartcampus.territoryservice.model.StoryObject;
import eu.trentorise.smartcampus.dt.DiscoverTrentoActivity;
// SherlockListFragment
public class NotificationsFragmentListDT extends SherlockListFragment {

	private NotificationsListAdapterDT adapter;
	private final static String APPID = "core.territory";
	private static final int MAX_MSG = 50;
	public static final String NOTIFICATIONS_PARAM = "Notifications";
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
		return inflater.inflate(R.layout.notifications_list_dt, container,false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		FeedbackFragmentInflater.inflateHandleButton(getSherlockActivity(), getView());

		adapter = new NotificationsListAdapterDT(getActivity(), R.layout.notifications_row_dt);
		setListAdapter(adapter);
		adapter.clear();

		// instantiate again NotificationsHelper if needed
		if (!NotificationsHelper.isInstantiated()) {
			try{
				NotificationsHelper.init(getSherlockActivity(), DTParamsHelper.getAppToken(), null, APPID, MAX_MSG);
			} catch (Exception e) {
				Log.e(getClass().getName(), e.toString());
				Toast.makeText(getActivity().getApplicationContext(),
						getString(R.string.app_failure_operation),
						Toast.LENGTH_SHORT).show();
				getActivity().finish();
			}
		}

		new SCAsyncTask<Void, Void, List<Notification>>(getSherlockActivity(), new NotificationsLoader(getSherlockActivity())).execute();
	}

	@Override
	public void onDestroy() {
		try {
			NotificationsHelper.markAllAsRead(getNotificationFilter());
		} catch (Exception e) {
			Log.e(this.getClass().getPackage().toString(), e.getMessage());
		}

		super.onDestroy();
	}

	private NotificationFilter getNotificationFilter() {
		NotificationFilter filter = new NotificationFilter();
		filter.setOrdering(ORDERING.ORDER_BY_ARRIVAL);
		return filter;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Notification notification = adapter.getItem(position);

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

		SCAsyncTask<EntityObject, Void, BaseDTObject> viewDetailsTask = new SCAsyncTask<EntityObject, Void, BaseDTObject>(
				getSherlockActivity(), new NotificationsAsyncTaskProcessorDT(getSherlockActivity()));

		try {
			if (notification.getEntities().size() == 2) {
				// new
				if (event != null && event.getId() != null && location != null && location.getId() != null) {
					viewDetailsTask.execute(event);
				} else if (location != null && location.getId() != null && story != null && story.getId() != null) {
					viewDetailsTask.execute(event);
				}
			} else if (notification.getEntities().size() == 1) {
				// modified
				if (event != null && event.getId() != null) {
					viewDetailsTask.execute(event);
				} else if (location != null && location.getId() != null) {
					viewDetailsTask.execute(location);
				} else if (story != null && story.getId() != null) {
					viewDetailsTask.execute(story);
				}
			}
		} catch (Exception e) {
			Log.e(this.getClass().getName(), e.getMessage());
		}
	}

	/*
	 * AsyncTask
	 */
	private class NotificationsAsyncTaskProcessorDT extends AbstractAsyncTaskProcessor<EntityObject, BaseDTObject> {

		public NotificationsAsyncTaskProcessorDT(Activity activity) {
			super(activity);
		}

		@Override
		public BaseDTObject performAction(EntityObject... params) throws SecurityException, ConnectionException, Exception {
			EntityObject entityObject = params[0];

			if (entityObject.getType().equalsIgnoreCase(Constants.TYPE_EVENT)) {
				LocalEventObject eo = DTHelper.findEventById(entityObject.getId());
				if (eo == null) {
					DTHelper.synchronize();
					eo = DTHelper.findEventById(entityObject.getId());
				}
				return eo == null ? null : eo;
			} else if (entityObject.getType().equalsIgnoreCase(Constants.TYPE_LOCATION)) {
				POIObject po = DTHelper.findPOIById(entityObject.getId());
				if (po == null) {
					DTHelper.synchronize();
					po = DTHelper.findPOIById(entityObject.getId());
				}
				return po == null ? null : po;
			} else if (entityObject.getType().equalsIgnoreCase(Constants.TYPE_STORY)) {
				StoryObject so = DTHelper.findStoryById((entityObject.getId()));
				if (so == null) {
					DTHelper.synchronize();
					so = DTHelper.findStoryById(entityObject.getId());
				}
				return so == null ? null : so;
			}

			return null;
		}

		@Override
		public void handleResult(BaseDTObject result) {
			if (result == null) {
				Toast.makeText(getSherlockActivity(), getString(R.string.app_failure_obj_not_found), Toast.LENGTH_LONG).show();
				return;
			}
			SherlockFragment fragment = null;
			Bundle args = new Bundle();

			if (result instanceof EventObject) {
				fragment = new EventDetailsFragment();
				args.putString(EventDetailsFragment.ARG_EVENT_ID, (result.getId()));
			} else if (result instanceof POIObject) {
				fragment = new PoiDetailsFragment();
				args.putString(PoiDetailsFragment.ARG_POI_ID, result.getId());
			} else if (result instanceof StoryObject) {
				fragment = new StoryDetailsFragment();
				args.putString(StoryDetailsFragment.ARG_STORY_ID, result.getId());
			}

			if (fragment != null) {
				//start  DiscoverTrento and pass params
				Intent i = (new Intent(getSherlockActivity(), DiscoverTrentoActivity.class));
				i.putExtra(NOTIFICATIONS_PARAM, result);
				startActivity(i);

			}
		}
	}

	private class NotificationsLoader extends AbstractAsyncTaskProcessor<Void, List<Notification>> {

		public NotificationsLoader(Activity activity) {
			super(activity);
		}

		@Override
		public List<Notification> performAction(Void... params) throws SecurityException, ConnectionException, Exception {
			return NotificationsHelper.getNotifications(getNotificationFilter(), 0, -1, 0);
		}

		@Override
		public void handleResult(List<Notification> notificationsList) {
			TextView listEmptyTextView = (TextView) getView().findViewById(R.id.list_text_empty);
			if (notificationsList != null && notificationsList.size() > 0) {
				for (Notification n : NotificationsHelper.getNotifications(getNotificationFilter(), 0, -1, 0)) {
					adapter.add(n);
				}
				listEmptyTextView.setVisibility(View.GONE);
			} else {
				listEmptyTextView.setVisibility(View.VISIBLE);
			}

			adapter.notifyDataSetChanged();
		}
	}
}
