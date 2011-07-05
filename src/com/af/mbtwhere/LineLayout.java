package com.af.mbtwhere;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.AttributeSet;
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
	protected String route_code = null;
	protected String feed = "";
	protected Line line;
	protected Context c;
	protected ProgressDialog progress = null;
	public static final int xmlFile = R.layout.picker;

	public LineLayout(Context context) {
		super(context);
	}
	
	public LineLayout(Context context,  AttributeSet attrs) { 
        super(context, attrs);
	}
	
	public LineLayout(Context context, String url, Line thisLine) {
		super(context);
		c = context;
		feed = url;
		line = thisLine;
	}
	
	public String combine(String[] s, String glue) {
	  int k = s.length;
	  if (k == 0) {
		  return null;
	  }
	  StringBuilder out=new StringBuilder();
	  out.append(s[0]);
	  for (int x = 1; x < k; ++x) {
		  out.append(glue).append(s[x]);
	  }
	  return out.toString();
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
	
	public String reformatTime(String[] time) {
    	String[] display_time = new String[2];
		if("00".equals(time[0])) { //reformat time display to get rid of hours
    		display_time[0] = time[1];
    		display_time[1] = time[2]; 
    	} else {
    		display_time = time;
    	}
		String time_remaining = combine(display_time, ":");
		return time_remaining;
	}
	
	public void setDisplay(ArrayList<JSONObject> feed) {
		boolean found = false;
		try {
			for(JSONObject record : feed) {
				String this_code = record.getString("PlatformKey");
			    String this_type = record.getString("InformationType");
			    if(this_code.equals(route_code) && "Predicted".equals(this_type)) {
			    	String time_remaining;
			    	String[] remaining = record.getString("TimeRemaining").split(":");
			    	time_remaining = reformatTime(remaining);
				    String label = c.getString(R.string.expecting_train)+" "+time_remaining;
				   	Log.v(TAG, "label="+label);
				   	getDisplay().setText(label);
				   	found = true;
			    }
		    }
		}  catch(JSONException e) {
			Log.v(TAG, "JSONException: "+e);
		}
		if(!found) {
			getDisplay().setText(R.string.shrug);
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
	
	public void setup() {
		stopProgress();
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
	            	route_code = route;
	            	startProgress();
	            	getDisplay().setText("");
	            	new GetLineFeed(v.getContext(), that).execute(feed);
            	}
            }
        });
        
        service.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startProgress();
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
