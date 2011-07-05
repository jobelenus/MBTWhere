package com.af.mbtwhere;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class StationLayout extends LineLayout {

	public static final String TAG = "StationLayout";
	private static final Integer NUM_RECORDS = 3;
	private String start_selection = "";
	private String feed = "";
	private final HashMap<String, RouteLayout> routes = new HashMap<String, RouteLayout>();
	public static final int xmlFile = R.layout.station_picker;

	public StationLayout(Context context) {
		super(context);
		c = context;
		// TODO Auto-generated constructor stub
	}
	
	public StationLayout(Context context,  AttributeSet attrs) { 
        super(context, attrs);
        c = context;
	}
	
	public Spinner getStart() {
		return (Spinner)findViewById(R.id.start);
	}
	
	public LinearLayout getContainer() {
		return (LinearLayout)findViewById(R.id.route_container);
	}
	
	public void checkFeed(ArrayList<JSONObject> feed) {
		Log.v(TAG, "json feed: "+feed);
		if(feed.size() == 0) {
			stopProgress();
			Toast.makeText(c, R.string.no_updates, Toast.LENGTH_SHORT).show();
		}
	}
	
	public void setDisplay(ArrayList<JSONObject> feed) {
		checkFeed(feed);
		try {
			for(String route_code : routes.keySet()) {
				ArrayList<String> times = new ArrayList<String>();
				int count = 0;
				for(JSONObject record : feed) {
					if(count >= NUM_RECORDS) {
						break;
					}
					String this_code = record.getString("PlatformKey");
				    String this_type = record.getString("InformationType");
				    if(this_code.equals(route_code) && "Predicted".equals(this_type)) {
				    	String time_remaining;
				    	String[] remaining = record.getString("TimeRemaining").split(":");
				    	time_remaining = reformatTime(remaining);
					    String label = ""+time_remaining;
					   	Log.v(TAG, "route="+route_code+"label="+label);
					   	times.add(label);
					   	count++;
				    }
			    }
				while(times.size() < NUM_RECORDS) {
					times.add(c.getString(R.string.shrug));
				}
				Log.v(TAG, "route_code: "+route_code+"route: "+routes.get(route_code));
				routes.get(route_code).setDisplay(times);
			}
		}  catch(JSONException e) {
			Log.v(TAG, "JSONException: "+e);
		}
		stopProgress();
	}
	
	public Button getService() {
		return (Button)findViewById(R.id.service);
	}
	
	public void stopProgress() {
		if(progress != null) {
			progress.dismiss();
		}
	}
	
	public void startProgress() {
		Context c = getContext();
		progress = ProgressDialog.show(c, "", c.getString(R.string.getting_train), true);
	}
	
	public Button getFind() {
		return (Button)findViewById(R.id.find);
	}
	
	public void setFeed(String url) {
		feed = url;
		Log.v(TAG, "setting feed: "+feed);
	}
	
	public void setLine(Line thisLine) {
		line = thisLine;
		Log.v(TAG, "setting line: "+line);
	}
	
	public void draw() {
		stopProgress();
		Log.v(TAG, "reading line: "+line);
		final String[] stations = line.getStationsByName();
        start_selection = stations[0];
        Button button = getFind();
        Button service = getService();
        final StationLayout that = this; //TODO this is horrible
        
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	startProgress();
            	Log.v(TAG, "get the feed: "+feed);
            	new GetLineFeed(v.getContext(), that).execute(feed);
            }
        });
        
        service.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startProgress();
				Log.v(TAG, "get the service");
				new ServiceFeed(v.getContext(), that).execute(line.name);
			}
		});
        
        getStart().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
            	start_selection = stations[position];
            	Log.v(TAG, "selected: "+start_selection);
            
            	LayoutInflater inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            	Station station = line.getStation(start_selection);
            	
            	//wtf why is this misbehaving?!
            	routes.clear();
            	Log.v(TAG, "container: "+getContainer());
            	getContainer().removeAllViews();
            	for(Route route : station.getInbound()) {
            		Log.v(TAG, "1code: "+route.code+", contains: "+routes.containsKey(route.code)+", get: "+routes.get(route.code));
            		if(!routes.containsKey(route.code)) {
            			routes.put(route.code, new RouteLayout(c, route));
            		}
            		Log.v(TAG, "2code: "+route.code+", contains: "+routes.containsKey(route.code)+", get: "+routes.get(route.code));
            		Log.v(TAG, "7keys: "+routes.keySet());
            	}
            	Log.v(TAG, "3contains: "+routes.containsKey("RALEN")+", get: "+routes.get("RALEN"));
            	for(Route route : station.getOutbound()) {
            		Log.v(TAG, "4code: "+route.code+", contains: "+routes.containsKey(route.code)+", get: "+routes.get(route.code));
            		if(!routes.containsKey(route.code)) {
            			routes.put(route.code, new RouteLayout(c, route));
            		}
            		Log.v(TAG, "5code: "+route.code+", contains: "+routes.containsKey(route.code)+", get: "+routes.get(route.code));
            		Log.v(TAG, "7keys: "+routes.keySet());
            	}
            	Log.v(TAG, "6contains: "+routes.containsKey("RALEN")+", get: "+routes.get("RALEN"));
            	Log.v(TAG, "7keys: "+routes.keySet());
            	
            	for(RouteLayout rl : routes.values()) {
            		inflater.inflate(R.layout.route_layout, rl);
            		getContainer().addView(rl);
            		rl.setParamsAndGo();
            	}
            }
            
            public void onNothingSelected(AdapterView<?> parent) {
            	start_selection = "";
            }   	
		});
        ArrayAdapter<String> aa_start = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, stations);
        aa_start.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        getStart().setAdapter(aa_start);
	}
}