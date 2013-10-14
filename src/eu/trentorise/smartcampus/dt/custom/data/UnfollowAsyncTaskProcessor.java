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

public class UnfollowAsyncTaskProcessor extends AbstractAsyncTaskProcessor<BaseDTObject, BaseDTObject> {
	private Context mContext;
	// private Activity activity;

	private CompoundButton buttonView;

	public UnfollowAsyncTaskProcessor(Activity activity, CompoundButton buttonView) {
		super(activity);
		// this.activity = activity;
		this.mContext = activity.getApplicationContext();
		this.buttonView = buttonView;
	}

	@Override
	public BaseDTObject performAction(BaseDTObject... params) throws SecurityException, ConnectionException, Exception {
		DTHelper.unfollow(params[0]);
		return params[0];
	}

	@Override
	public void handleResult(BaseDTObject result) {
		Toast.makeText(mContext, mContext.getString(R.string.toast_unfollow_ok, result.getTitle()), Toast.LENGTH_SHORT).show();
		// activity.invalidateOptionsMenu();
		if (buttonView != null) {
			buttonView.setBackgroundResource(R.drawable.ic_btn_monitor_off);
		}
	}

}
