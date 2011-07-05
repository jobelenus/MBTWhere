package com.af.mbtwhere;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RouteLayout extends LinearLayout {
	private final static String TAG = "RouteLayout";
	Route route;
	Context c;

	public RouteLayout(Context context, Route r) {
		super(context);
		c = context;
		route = r;
	}
	
	public void setParamsAndGo() {
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)getTimes().getLayoutParams();
		params.height = LinearLayout.LayoutParams.FILL_PARENT;
		params.width = LinearLayout.LayoutParams.FILL_PARENT;
		getTimes().setLayoutParams(params);
		setOrientation(VERTICAL);
		setup();
	}

	public LinearLayout getTimes() {
		return (LinearLayout)findViewById(R.id.times);
	}
	
	public TextView getTitle() {
		return (TextView)findViewById(R.id.title);
	}
	
	public void setDisplay(ArrayList<String> times) {
		for(String time : times) {
			Log.v(TAG, "time: "+time);
			LayoutInflater inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.time_text, getTimes());
			((TextView) getTimes().findViewById(R.id.time_text)).setText(time);
		}
	}
	
	public void setup() {
		Log.v(TAG, route.code);
		getTitle().setText(route.code);
	}
}
