package com.af.mbtwhere;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RouteLayout extends LinearLayout {
	private final static String TAG = "RouteLayout";
	Route route;

	public RouteLayout(Context context, Route r) {
		super(context);
		route = r;
	}

	public LinearLayout getTimes() {
		return (LinearLayout)findViewById(R.id.times);
	}
	
	public TextView getTitle() {
		return (TextView)findViewById(R.id.title);
	}
	
	public void setDisplay(ArrayList<String> times) {
	
	}
	
	public void setup() {
		Log.v(TAG, route.code);
		getTitle().setText(route.code);
	}
}
