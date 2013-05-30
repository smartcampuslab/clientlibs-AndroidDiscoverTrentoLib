package eu.trentorise.smartcampus.dt.notifications;

import java.util.List;

import android.app.Activity;
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
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.dt.custom.data.Constants;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
import eu.trentorise.smartcampus.dt.fragments.events.EventDetailsFragment;
import eu.trentorise.smartcampus.dt.fragments.pois.PoiDetailsFragment;
import eu.trentorise.smartcampus.dt.fragments.stories.StoryDetailsFragment;
import eu.trentorise.smartcampus.dt.model.BaseDTObject;
import eu.trentorise.smartcampus.dt.model.EventObject;
import eu.trentorise.smartcampus.dt.model.POIObject;
import eu.trentorise.smartcampus.dt.model.StoryObject;
import eu.trentorise.smartcampus.notifications.NotificationsHelper;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

// SherlockListFragment
public class NotificationsFragmentListDT extends SherlockListFragment {

	private NotificationsListAdapterDT adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
		return inflater.inflate(R.layout.notifications_list_dt, container);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		FeedbackFragmentInflater.inflateHandleButton(getSherlockActivity(), getView());

		adapter = new NotificationsListAdapterDT(getActivity(), R.layout.notifications_row_dt);
		setListAdapter(adapter);
		adapter.clear();

		// instantiate again NotificationsHelper if needed
		String appToken = getSherlockActivity().getIntent().getStringExtra(NotificationsHelper.PARAM_APP_TOKEN);
		String syncDbName = getSherlockActivity().getIntent().getStringExtra(NotificationsHelper.PARAM_SYNC_DB_NAME);
		String syncService = getSherlockActivity().getIntent().getStringExtra(NotificationsHelper.PARAM_SYNC_SERVICE);
		String authority = getSherlockActivity().getIntent().getStringExtra(NotificationsHelper.PARAM_AUTHORITY);

		if (!NotificationsHelper.isInstantiated() && appToken != null && syncDbName != null && syncService != null
				&& authority != null) {
			NotificationsHelper.init(getSherlockActivity(), appToken, syncDbName, syncService, authority);
		}

		List<Notification> notificationsList = NotificationsHelper.getNotifications(getNotificationFilter(), 0, -1, 0);

		TextView listEmptyTextView = (TextView) getView().findViewById(R.id.list_text_empty);
		if (notificationsList.size() > 0) {
			for (Notification n : NotificationsHelper.getNotifications(getNotificationFilter(), 0, -1, 0)) {
				adapter.add(n);
			}
			listEmptyTextView.setVisibility(View.GONE);
		} else {
			listEmptyTextView.setVisibility(View.VISIBLE);
		}

		adapter.notifyDataSetChanged();
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
		filter.setSource("Social");
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
				if (event != null && event.getEntityId() != null && location != null && location.getEntityId() != null) {
					viewDetailsTask.execute(event);
				} else if (location != null && location.getEntityId() != null && story != null && story.getEntityId() != null) {
					viewDetailsTask.execute(event);
				}
			} else if (notification.getEntities().size() == 1) {
				// modified
				if (event != null && event.getEntityId() != null) {
					viewDetailsTask.execute(event);
				} else if (location != null && location.getEntityId() != null) {
					viewDetailsTask.execute(location);
				} else if (story != null && story.getEntityId() != null) {
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
				EventObject eo = (EventObject) DTHelper.findEventByEntityId(entityObject.getEntityId());
				if (eo == null)
					{
					DTHelper.synchronize();
					eo = (EventObject) DTHelper.findEventByEntityId(entityObject.getEntityId());
					}
				return eo;
			} else if (entityObject.getType().equalsIgnoreCase(Constants.TYPE_LOCATION)) {
				POIObject po = (POIObject) DTHelper.findEventByEntityId(entityObject.getEntityId());
				if (po == null)
					{
					DTHelper.synchronize();
					po = (POIObject) DTHelper.findEventByEntityId(entityObject.getEntityId());
					}
				return po;
			} else if (entityObject.getType().equalsIgnoreCase(Constants.TYPE_STORY)) {
				StoryObject so = (StoryObject) DTHelper.findStoryByEntityId((entityObject.getEntityId()));
				if (so == null)
					{
					DTHelper.synchronize();
					so = (StoryObject) DTHelper.findEventByEntityId(entityObject.getEntityId());
					}
				return so;
			}

			return null;
		}

		@Override
		public void handleResult(BaseDTObject result) {
			if (result == null)
				{
				Toast.makeText(getSherlockActivity(), getString(R.string.app_failure_obj_not_found), Toast.LENGTH_LONG).show();
				return;
				}
			FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager().beginTransaction();
			SherlockFragment fragment = null;
			Bundle args = new Bundle();

			if (result instanceof EventObject) {
				fragment = new EventDetailsFragment();
				args.putString(EventDetailsFragment.ARG_EVENT_OBJECT, (result.getId()));

			} else if (result instanceof POIObject) {
				fragment = new PoiDetailsFragment();
				args.putSerializable(PoiDetailsFragment.ARG_POI, result);
			} else if (result instanceof StoryObject) {
				fragment = new StoryDetailsFragment();
				args.putSerializable(StoryDetailsFragment.ARG_STORY, result);
			}

			if (fragment != null) {
				fragment.setArguments(args);
				fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				// fragmentTransaction.detach(this);
				fragmentTransaction.replace(android.R.id.content, fragment, "details");
				fragmentTransaction.addToBackStack(fragment.getTag());
				fragmentTransaction.commit();
			}
		}

	}

}
