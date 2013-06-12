package eu.trentorise.smartcampus.dt.custom.data;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.dt.model.BaseDTObject;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class UnfollowAsyncTaskProcessor extends
		AbstractAsyncTaskProcessor<BaseDTObject, BaseDTObject> {
	private Context ctx;
	private Activity activity;

	public UnfollowAsyncTaskProcessor(Activity activity) {
		super(activity);
		ctx = activity.getApplicationContext();
		this.activity = activity;
	}

	@Override
	public BaseDTObject performAction(BaseDTObject... params)
			throws SecurityException, ConnectionException, Exception {
		DTHelper.unfollow(params[0]);
		return params[0];
	}

	@Override
	public void handleResult(BaseDTObject result) {
		Toast.makeText(ctx,
				ctx.getString(R.string.toast_unfollow_ok, result.getTitle()),
				Toast.LENGTH_SHORT).show();
		activity.invalidateOptionsMenu();
	}

}
