package eu.trentorise.smartcampus.dt.fragments.events;

import java.util.List;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;

import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;

public class ConfirmPoiDialog extends SherlockDialogFragment {

	public interface OnDetailsClick {
		public void OnDialogDetailsClick(BaseDTObject stop);
	}

	public static final String ARG_POI = "poi";
	public static final String ARG_POIS = "pois";
	private BaseDTObject poiObject;
	private List<BaseDTObject> poiObjectsList;
	private RadioGroup poisRadioGroup;
	private OnDetailsClick listener;

	public ConfirmPoiDialog(OnDetailsClick listener) {
		this.listener = listener;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.poiObject = (BaseDTObject) this.getArguments().getSerializable(ARG_POI);
		this.poiObjectsList = (List<BaseDTObject>) this.getArguments().getSerializable(ARG_POIS);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getDialog().setTitle(getString(R.string.choose_poi));
		View view = null;

		if (poiObjectsList != null) {
			view = inflater.inflate(R.layout.mapdialogmulti, container, false);
		} else {
			view = inflater.inflate(R.layout.mapdialog, container, false);
		}

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		if (poiObjectsList != null) {
			// multiple stops
			poisRadioGroup = (RadioGroup) getDialog().findViewById(R.id.mapdialogmulti_rg);
			poisRadioGroup.removeAllViews();
			for (BaseDTObject stop : poiObjectsList) {
				RadioButton rb = new RadioButton(getSherlockActivity());
				rb.setTag(stop);
				rb.setText(stop.getTitle());
				poisRadioGroup.addView(rb);
			}
			poisRadioGroup.getChildAt(0).setSelected(true);
		} else if (poiObject != null) {
			// single stop
			TextView msg = (TextView) getDialog().findViewById(R.id.mapdialog_msg);
			msg.setText(poiObject.getTitle());
			msg.setMovementMethod(new ScrollingMovementMethod());
		}

		Button btn_cancel = (Button) getDialog().findViewById(R.id.mapdialog_cancel);
		btn_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getDialog().dismiss();
			}
		});

		Button btn_ok = (Button) getDialog().findViewById(R.id.mapdialog_ok);
		btn_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BaseDTObject stop = null;

				if (poiObjectsList != null) {
					RadioButton selectedRb = (RadioButton) poisRadioGroup.findViewById(poisRadioGroup
							.getCheckedRadioButtonId());
					stop = (BaseDTObject) selectedRb.getTag();
				} else if (poiObject != null) {
					stop = poiObject;
				}

				listener.OnDialogDetailsClick(stop);

				getDialog().dismiss();
			}
		});

	}
}