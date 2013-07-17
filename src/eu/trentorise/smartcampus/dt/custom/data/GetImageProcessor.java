package eu.trentorise.smartcampus.dt.custom.data;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.dt.model.POIObject;
import eu.trentorise.smartcampus.dt.multimedia.Constants;
import eu.trentorise.smartcampus.dt.multimedia.Constants.Extra;
import eu.trentorise.smartcampus.dt.multimedia.ImageGridFragment;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class GetImageProcessor extends AbstractAsyncTaskProcessor<POIObject, String[]> {

	private SherlockFragmentActivity mActivity;
	private Integer mFragmentId = null;

	public GetImageProcessor(Activity activity, int fragmentId) {
		super(activity);
		mActivity = (SherlockFragmentActivity) activity;
		this.mFragmentId = fragmentId;
	}

	@Override
	public String[] performAction(POIObject... params) throws SecurityException, Exception {
		return DTHelper.getImageURLs(params[0].getEntityId());
	}

	@Override
	public void handleResult(String[] result) {
		// getSherlockActivity().invalidateOptionsMenu();
		if (result != null) {
			FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
			Fragment f = new ImageGridFragment();
			Bundle args = new Bundle();
			args.putStringArray(Extra.IMAGES, Constants.IMAGES);
			f.setArguments(args);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.replace(mFragmentId, f);
			ft.addToBackStack(f.getTag());
			ft.commit();
		} else {
			// no images
			Toast.makeText(mActivity, mActivity.getString(R.string.app_failure_no_gallery_found), Toast.LENGTH_LONG).show();
		}

	}
}