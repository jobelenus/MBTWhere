package com.af.mbtwhere;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class StationLayout extends LinearLayout {
	public static final String TAG = "StationLayout";
	private static final Integer NUM_RECORDS = 3;
	private String start_selection = "";
	private String feed = "";
	private Line line;
	private Context c;
	private ProgressDialog progress = null;
	private final HashMap<String, RouteLayout> routes = new HashMap<String, RouteLayout>();
	public static final int xmlFile = R.layout.station_picker;
	
	public StationLayout(Context context, String url, Line thisLine) {
		super(context);
		c = context;
		feed = url;
		line = thisLine;
	}

	public Spinner getStart() {
		return (Spinner)findViewById(R.id.start);
	}
	
	public LinearLayout getContainer() {
		return (LinearLayout)findViewById(R.id.route_container);
	}
	
	public void setDisplay(ArrayList<JSONObject> feed) {
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
	
	public void setup() {
		stopProgress();
		final String[] stations = line.getStationsByName();
        start_selection = stations[0];
        Button button = getFind();
        Button service = getService();
        final StationLayout that = this; //TODO this is horrible
        
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	startProgress();
            	new GetLineFeed(v.getContext(), that).execute(feed);
            }
        });
        
        service.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startProgress();
				new ServiceFeed(v.getContext(), that).execute(line.name);
			}
		});
        
        getStart().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
            	//apparently this runs in the context of the activity, w00t
            	start_selection = stations[position];
            	Log.v(TAG, "selected: "+start_selection);
            
            	LayoutInflater inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            	Station station = line.getStation(start_selection);
            	
            	for(Route route : station.getInbound()) {
            		routes.put(route.code, new RouteLayout(c, route));
            	}
            	for(Route route : station.getOutbound()) {
            		routes.put(route.code, new RouteLayout(c, route));
            	}
            	
            	for(RouteLayout rl : routes.values()) {
            		inflater.inflate(R.layout.route_layout, rl);
            		getContainer().addView(rl);
            		rl.setup();
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