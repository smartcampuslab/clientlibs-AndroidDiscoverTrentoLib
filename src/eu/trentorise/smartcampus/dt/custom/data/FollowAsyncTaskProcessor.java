package eu.trentorise.smartcampus.dt.custom.data;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;
import eu.trentorise.smartcampus.android.common.follow.FollowEntityObject;
import eu.trentorise.smartcampus.android.common.follow.FollowHelper;
import eu.trentorise.smartcampus.android.common.follow.model.Topic;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class FollowAsyncTaskProcessor extends AbstractAsyncTaskProcessor<Object, Topic> {

	private Context ctx;
	private String appToken;
	private String authToken;
	private FollowEntityObject feo;

	public FollowAsyncTaskProcessor(Activity activity) {
		super(activity);
	}

	@Override
	public Topic performAction(Object... params) throws SecurityException, ConnectionException, Exception {
		ctx = (Context) params[0];
		appToken = (String) params[1];
		authToken = (String) params[2];
		feo = (FollowEntityObject) params[3];

		return FollowHelper.follow(ctx, appToken, authToken, feo);
	}

	@Override
	public void handleResult(Topic result) {
		Toast.makeText(ctx, ctx.getString(R.string.toast_follow_ok, result.getName()), Toast.LENGTH_SHORT).show();
	}

}
