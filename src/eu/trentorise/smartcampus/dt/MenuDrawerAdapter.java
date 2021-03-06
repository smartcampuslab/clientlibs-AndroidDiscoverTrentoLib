package eu.trentorise.smartcampus.dt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuDrawerAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final String[] values;

	public MenuDrawerAdapter(Context context, String[] values) {
		super(context, R.layout.drawer_list_item, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View rowView = inflater.inflate(R.layout.drawer_list_item, parent,
				false);
		TextView textView = (TextView) rowView.findViewById(R.id.menuitem);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.logo);
		textView.setText(values[position]);

		// Change icon based on name
		String s = values[position];

		System.out.println(s);

		if (s.equals(values[0])) {
			imageView.setImageResource(R.drawable.ic_ic_home);
		} else if (s.equals(values[1])) {
			imageView.setImageResource(R.drawable.ic_places);
		} else if (s.equals(values[2])) {
			imageView.setImageResource(R.drawable.ic_events);
		} else if (s.equals(values[3])) {
			imageView.setImageResource(R.drawable.ic_stories);
		} else if (s.equals(values[4])) {
			imageView.setImageResource(R.drawable.ic_ic_notices);
		}else if (s.equals(values[5])) {
			imageView.setImageResource(R.drawable.ic_tutorial);
		}

		return rowView;
	}
}
