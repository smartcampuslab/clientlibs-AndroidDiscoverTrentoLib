package eu.trentorise.smartcampus.dt;

import com.github.espiandev.showcaseview.ShowcaseView;
import com.github.espiandev.showcaseview.ShowcaseView.ConfigOptions;
import com.github.espiandev.showcaseview.ShowcaseView.OnShowcaseEventListener;

import eu.trentorise.smartcampus.dt.custom.data.DTHelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class TutorialActivity extends Activity implements
		OnShowcaseEventListener {

	private final static String PASSED_POSITION = "passedPos";
	private final static String PASSED_RADIUS = "passedRadius";
	private final static String PASSED_TITLE = "passedTitle";
	private final static String PASSED_DESC = "passedDesc";

	private ShowcaseView sv;
	private Button skip;

	public static void newIstance(Activity ctx, int[] position,int radius,
			String title, String description, int requestCode) {
		Intent caller = new Intent(ctx,TutorialActivity.class);
		caller.putExtra(PASSED_POSITION, position);
		caller.putExtra(PASSED_TITLE, title);
		caller.putExtra(PASSED_DESC, description);
		caller.putExtra(PASSED_RADIUS, radius);
		ctx.startActivityForResult(caller, requestCode)	;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tutorial_activity);

		sv = (ShowcaseView) findViewById(R.id.tut_sv);
		skip = (Button) findViewById(R.id.skip_tutorial_btn);
		
		setParams(getIntent());

		sv.show();
		
		sv.setOnShowcaseEventListener(this);
	}


	private void setParams(Intent intent) {
		ConfigOptions co = new ConfigOptions();
		co.buttonText = getString(R.string.next_tut);
		if (sv != null) {
			int[] position = intent.getExtras().getIntArray(PASSED_POSITION);
			sv.setShowcasePosition(position[0], position[1]);
			String title = intent.getExtras().getString(PASSED_TITLE);
			String desc = intent.getExtras().getString(PASSED_DESC);
			co.circleRadius = intent.getExtras().getInt(PASSED_RADIUS);
			sv.setText(title, desc);
		}
		sv.setConfigOptions(co);
	}
	
	public void skipTutorial(View v){
		DTHelper.setWantTour(this, false);
		sv.hide();
	}

	@Override
	public void onShowcaseViewHide(ShowcaseView showcaseView) {
		Intent returnIntent = new Intent();
		this.setResult(RESULT_CANCELED,returnIntent);
		this.finish();
	}

	@Override
	public void onShowcaseViewShow(ShowcaseView showcaseView) {
	}

}
