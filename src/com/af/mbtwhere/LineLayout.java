package com.af.mbtwhere;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

public class LineLayout extends LinearLayout {
	public static final String TAG = "LineLayout";
	private static final Integer NUM_RECORDS = 1;
	private String start_selection = "";
	private String end_selection = "";
	private String feed = "";
	private Line line;

	public LineLayout(Context context, String url, Line thisLine) {
		super(context);
		feed = url;
		line = thisLine;
	}
	
	@Override public String toString() {
		return "LineLayout: "+line.toString();
	}

	public Spinner getStart() {
		return (Spinner)findViewById(R.id.start);
	}
	
	public Spinner getEnd() {
		return (Spinner)findViewById(R.id.end);
	}
	
	public TextView getDisplay() {
		return (TextView)findViewById(R.id.display);
	}
	
	public void setDisplay(ArrayList<String> times) {
		getDisplay().setText(times.get(NUM_RECORDS-1));
	}
	
	public Button getService() {
		return (Button)findViewById(R.id.service);
	}
	
	public ProgressBar getProgress() {
		return (ProgressBar)findViewById(R.id.progress);
	}
	
	public Button getFind() {
		return (Button)findViewById(R.id.find);
	}
	
	public void setup() {
		getProgress().setVisibility(View.INVISIBLE);
		final String[] stations = line.getStationsByName();
        start_selection = stations[0];
        end_selection = stations[0];
        Button button = getFind();
        Button service = getService();
        final LineLayout that = this; //TODO this is horrible
        
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	String route = line.getCodeFor(start_selection, end_selection);
            	Log.v(TAG, "start="+start_selection+", end="+end_selection);
            	if(start_selection.equals(end_selection)) {
            		getDisplay().setText(R.string.already_there);
            	} else {
	            	Log.v(TAG, "route="+route);
	            	getProgress().setVisibility(View.VISIBLE);
	            	getDisplay().setText("");
	            	new GetLineFeed(v.getContext(), that).execute(feed, route, NUM_RECORDS.toString());
            	}
            }
        });
        
        service.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				getProgress().setVisibility(View.VISIBLE);
				getDisplay().setText("");
				new ServiceFeed(v.getContext(), that).execute(line.name);
			}
		});
        
        getStart().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
            	//apparently this runs in the context of the activity, w00t
            	start_selection = stations[position];
            }
            
            public void onNothingSelected(AdapterView<?> parent) {
            	start_selection = "";
            }   	
		});
        getEnd().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
            	//apparently this runs in the context of the activity, w00t
            	end_selection = stations[position];
            }
            
            public void onNothingSelected(AdapterView<?> parent) {
            	end_selection = "";
            }   	
		});
        ArrayAdapter<String> aa_start = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, stations);
        aa_start.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        getStart().setAdapter(aa_start);
        ArrayAdapter<String> aa_end = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, stations);
        aa_end.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        getEnd().setAdapter(aa_end);
	}
}
