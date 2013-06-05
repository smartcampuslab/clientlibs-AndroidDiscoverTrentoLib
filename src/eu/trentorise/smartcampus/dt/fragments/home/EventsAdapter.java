package eu.trentorise.smartcampus.dt.fragments.home;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import eu.trentorise.smartcampus.dt.R;

public class EventsAdapter extends BaseAdapter {

	public Context context;
	String TAG = "CatagorySummaryAdapter";
	public LayoutInflater inflater;
	public ArrayList<String> eventDescription;
	public boolean[] checked;
	boolean Dialogue;
	AddToArray addtoarray;

	public EventsAdapter(Context ctx, String[] itemsDescriptions, boolean[] checkedItems, AddToArray addtoarray,
			boolean Dialogue) {
		super();
		this.context = ctx;
		this.inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.eventDescription = new ArrayList<String>(Arrays.asList(itemsDescriptions));
		this.Dialogue = Dialogue;
		this.checked = checkedItems;
		this.addtoarray = addtoarray;
	}

	@Override
	public int getCount() {
		return eventDescription.size();
	}

	@Override
	public Object getItem(int position) {
		return eventDescription.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	public class ViewHolder {
		RelativeLayout lsummary_row;
		TextView events_name;
		CheckBox chkinterest;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		int pos = position;
		ViewHolder holder;

		holder = new ViewHolder();

		convertView = inflater.inflate(R.layout.today_events_row, null);

		holder.events_name = (TextView) convertView.findViewById(R.id.home_events_text);
		holder.chkinterest = (CheckBox) convertView.findViewById(R.id.home_events_checked);

		holder.events_name.setText(eventDescription.get(pos));
		if (context.getResources().getString(R.string.categories_event_today).equals(eventDescription.get(pos))) {
			holder.events_name.setTextAppearance(context, R.style.today_events);
		}
		holder.chkinterest.setChecked(checked[position]);
		holder.chkinterest.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				addtoarray.addtoarray(isChecked, position);

			}
		});

		return convertView;
	}

	public interface AddToArray {
		void addtoarray(boolean isChecked, int which);
	}
}
