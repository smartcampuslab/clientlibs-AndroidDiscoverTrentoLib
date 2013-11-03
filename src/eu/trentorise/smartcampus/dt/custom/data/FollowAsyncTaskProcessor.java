package eu.trentorise.smartcampus.dt.custom.data;

import android.app.Activity;
import android.content.Context;
import android.widget.CompoundButton;
import android.widget.Toast;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;

public class FollowAsyncTaskProcessor extends AbstractAsyncTaskProcessor<Object, BaseDTObject> {
	private Context mContext;
//	private String appToken;
//	private String authToken;
	// private FollowEntityObject feo;
	// private Activity activity;

	private CompoundButton buttonView;

	public FollowAsyncTaskProcessor(Activity activity, CompoundButton buttonView) {
		super(activity);
		// this.activity = activity;
		this.mContext = activity.getApplicationContext();
		this.buttonView = buttonView;
	}

	@Override
	public BaseDTObject performAction(Object... params) throws SecurityException, ConnectionException, Exception {
//		appToken = (String) params[0];
//		authToken = (String) params[1];
		BaseDTObject obj = (BaseDTObject) params[0];
		// feo = (FollowEntityObject) params[2];
		// Topic topic = FollowHelper.follow(mContext, appToken, authToken,
		// feo);
		// if (topic != null) {
		// BaseDTObject obj = findObject(feo);
		// if (obj != null) {

		// }
		// }
		return DTHelper.follow(obj);
	}

//	private BaseDTObject findObject(FollowEntityObject feo) {
//		if (feo != null) {
//			try {
//				if (feo.getType().equals(DTConstants.ENTITY_TYPE_EVENT)) {
//					return DTHelper.findEventByEntityId(feo.getEntityId()).getObjectForBean();
//				} else if (feo.getType().equals(DTConstants.ENTITY_TYPE_POI)) {
//					return DTHelper.findPOIByEntityId(feo.getEntityId()).getObjectForBean();
//				} else if (feo.getType().equals(DTConstants.ENTITY_TYPE_STORY)) {
//					return DTHelper.findStoryByEntityId(feo.getEntityId()).getObjectForBean();
//				}
//			} catch (Exception e) {
//				Log.e(FollowAsyncTaskProcessor.class.getName(),
//						String.format("Error getting BaseDTObject %s of type %s", feo.getEntityId(), feo.getType()));
//				return null;
//			}
//		}
//		return null;
//	}

	@Override
	public void handleResult(BaseDTObject result) {
		if (result != null)
			Toast.makeText(mContext, mContext.getString(R.string.toast_follow_ok, result.getTitle()),
					Toast.LENGTH_SHORT).show();
		// activity.invalidateOptionsMenu();
		if (buttonView != null) {
			buttonView.setBackgroundResource(R.drawable.ic_btn_monitor_on);
		}
	}

}
