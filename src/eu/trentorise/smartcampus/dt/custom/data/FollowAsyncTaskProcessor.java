package eu.trentorise.smartcampus.dt.custom.data;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import eu.trentorise.smartcampus.android.common.follow.FollowEntityObject;
import eu.trentorise.smartcampus.android.common.follow.FollowHelper;
import eu.trentorise.smartcampus.android.common.follow.model.Topic;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.dt.model.BaseDTObject;
import eu.trentorise.smartcampus.dt.model.DTConstants;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class FollowAsyncTaskProcessor extends
		AbstractAsyncTaskProcessor<Object, Topic> {

	private Context ctx;
	private String appToken;
	private String authToken;
	private FollowEntityObject feo;
	private Activity activity;

	public FollowAsyncTaskProcessor(Activity activity) {
		super(activity);
		this.activity = activity;
	}

	@Override
	public Topic performAction(Object... params) throws SecurityException,
			ConnectionException, Exception {
		ctx = (Context) params[0];
		appToken = (String) params[1];
		authToken = (String) params[2];
		feo = (FollowEntityObject) params[3];
		Topic topic = FollowHelper.follow(ctx, appToken, authToken, feo);
		if (topic != null) {
			BaseDTObject obj = findObject(feo);
			if (obj != null) {
				DTHelper.follow(obj, topic.getId());
			}
		}
		return topic;
	}

	private BaseDTObject findObject(FollowEntityObject feo) {
		if (feo != null) {
			try {
				if (feo.getType().equals(DTConstants.ENTITY_TYPE_EVENT)) {
					return DTHelper.findEventByEntityId(feo.getEntityId());
				} else if (feo.getType().equals(DTConstants.ENTITY_TYPE_POI)) {
					return DTHelper.findPOIByEntityId(feo.getEntityId());
				} else if (feo.getType().equals(DTConstants.ENTITY_TYPE_STORY)) {
					return DTHelper.findStoryByEntityId(feo.getEntityId());
				}
			} catch (Exception e) {
				Log.e(FollowAsyncTaskProcessor.class.getName(), String.format(
						"Error getting BaseDTObject %s of type %s",
						feo.getEntityId(), feo.getType()));
				return null;
			}
		}
		return null;
	}

	@Override
	public void handleResult(Topic result) {
		Toast.makeText(ctx,
				ctx.getString(R.string.toast_follow_ok, result.getName()),
				Toast.LENGTH_SHORT).show();
		activity.invalidateOptionsMenu();
	}

}
