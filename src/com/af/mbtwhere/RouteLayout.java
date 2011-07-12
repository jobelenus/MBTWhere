package com.af.mbtwhere;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
		TreeMap<Integer, String> sortedTimes = new TreeMap<Integer, String>();
		for(String time : times) {
			try {
				List<String> sArr = (List<String>) Arrays.asList(time.split(":"));
				Collections.reverse(sArr);
				Iterator it = sArr.iterator();
				int seconds = 0;
				int i = 0;
				while(it.hasNext()) {
					seconds += Math.max((60*i++),1) * new Integer((String) it.next());
				}
				sortedTimes.put(seconds, time);
			} catch(NumberFormatException e) { //we got a string message, leave it be, put it at the end.
				sortedTimes.put(999999, time);
			}
		}
		Set set = sortedTimes.entrySet();
		Iterator i = set.iterator();
		while(i.hasNext()) {
			Map.Entry aTime = (Map.Entry)i.next();
			String time = (String) aTime.getValue();
			Log.v(TAG, "time: "+time);
			LayoutInflater inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.time_text, getTimes());
			//set the *last* time, as ids are useless here
			int numChildren = getTimes().getChildCount();
			((TextView) getTimes().getChildAt(numChildren-1)).setText(time);
		}
	}
	
	public void setup() {
		Log.v(TAG, route.code);
		getTitle().setText(route.code);
	}
}
