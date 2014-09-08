package org.theboar.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class MySimpleArrayAdapter extends ArrayAdapter<String>
{
	private final Context context;
	private final String[] values;

	public MySimpleArrayAdapter(Context context, String[] values) {
		super(context,R.layout.menu_element,values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.menu_element,parent,false);

		if (position == Category.HOME) {
			rowView.findViewById(R.id.menu_item_home).setVisibility(View.VISIBLE);
			rowView.findViewById(R.id.menu_item).setVisibility(View.GONE);

			rowView.findViewById(R.id.menu_item_search).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v)
				{
					Intent i = new Intent(context,BoarActivity.class);
					i.putExtra("forSearch",true);
					context.startActivity(i);
				}
			});
		}
		else if (position == Category.FAVOURITES) {
			rowView.findViewById(R.id.menu_item_home).setVisibility(View.GONE);

			TextView tv = (TextView) rowView.findViewById(R.id.menu_item_text);
			tv.setText(values[position]);

			FrameLayout ll = (FrameLayout) rowView.findViewById(R.id.menu_item_colour);
//			ll.setBackgroundColor(Category.getCategoryColourText(Category.getCategoryIDFromName(values[position]),
//					context.getResources()));
			ll.setBackgroundColor(context.getResources().getColor(R.color.black_30));
			ll.getLayoutParams().width = CNS.getPXfromDP(30,context);
			ll.setPadding(10,10,10,10);

			ImageView img = (ImageView) new ImageView(context);
			img.setImageDrawable(context.getResources().getDrawable(R.drawable.fav_true));
			FrameLayout.LayoutParams fll = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.WRAP_CONTENT,
					Gravity.CENTER);

			ll.addView(img);
			img.setLayoutParams(fll);

			rowView.findViewById(R.id.menu_divider).setVisibility(View.VISIBLE);

		}
		else {
			rowView.findViewById(R.id.menu_item_home).setVisibility(View.GONE);
			TextView tv = (TextView) rowView.findViewById(R.id.menu_item_text);
			tv.setText(values[position]);
			FrameLayout ll = (FrameLayout) rowView.findViewById(R.id.menu_item_colour);
			ll.setBackgroundColor(Category.getCategoryColourText(Category.getCategoryIDFromName(values[position]),
					context.getResources()));
		}

		return rowView;
	}
}
