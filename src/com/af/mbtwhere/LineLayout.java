package com.af.mbtwhere;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import com.github.droidfu.concurrent.BetterAsyncTask;
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
	String start_selection = "";
	String end_selection = "";
	String feed = "";
	Line line;

	public LineLayout(Context context, String url, Line thisLine) {
		super(context);
		feed = url;
		line = thisLine;
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
	            	new GetLineFeed(v.getContext()).execute(feed, route);
            	}
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
	
	class GetLineFeed extends BetterAsyncTask<String, Void, String> {
		HttpClient client = null;
		
    	public GetLineFeed(Context c) {
			super(c);
		}
    	
    	public String combine(String[] s, String glue) {
    	  int k=s.length;
    	  if (k==0) {
    	    return null;
    	  }
    	  StringBuilder out=new StringBuilder();
    	  out.append(s[0]);
    	  for (int x=1;x<k;++x) {
    	    out.append(glue).append(s[x]);
    	  }
    	  return out.toString();
    	}
    	
    	@Override
    	protected String doCheckedInBackground(Context c, String... vargs) {
    		HttpClient client = new DefaultHttpClient();
    		String feed_url = vargs[0];
    		String route_code = vargs[1];
    		HttpGet getMethod = new HttpGet(feed_url);
    		try {
    			ResponseHandler<String> responseHandler = new BasicResponseHandler();
    			String responseBody = client.execute(getMethod, responseHandler);
    			JSONArray records = new JSONArray(responseBody);
    			for (int i = 0; i < records.length(); ++i) {
    			    JSONObject record = records.getJSONObject(i);
    			    Log.v(TAG, "record="+record);
    			    String this_code = record.getString("PlatformKey");
    			    String this_type = record.getString("InformationType");
    			    if(this_code.equals(route_code) && "Predicted".equals(this_type)) {
    			    	String time_remaining;
    			    	String[] remaining = record.getString("TimeRemaining").split(":");
    			    	String[] display_time = new String[2];
    			    	if("00".equals(remaining[0])) { //reformat time display to get rid of hours
    			    		display_time[0] = remaining[1];
    			    		display_time[1] = remaining[2]; 
    			    	} else {
    			    		display_time = remaining;
    			    	}
    			    	time_remaining = combine(display_time, ":");
	    			    String label = getContext().getString(R.string.expecting_train)+" "+time_remaining;
	    			   	Log.v(TAG, "label="+label);
    			    	return label;
    			    }
    			}
    			return getContext().getString(R.string.shrug);
    		} catch(Throwable t) {
    			Log.v(TAG, ""+t);
    			//TODO: errors need to be reported
    			//Toast.makeText(this, "Request failed: "+t.toString(), Toast.LENGTH_LONG).show();
    		}
    		return null;
    	}
    	
    	@Override
    	//this gets executed on the UI thread, so don't clog it up
    	protected void onProgressUpdate(Void... junk) {
    		
    	}

		@Override
		protected void after(Context c, String next) {
			//client.getConnectionManager().shutdown();
    		getProgress().setVisibility(View.INVISIBLE);
    		getDisplay().setText(next);
		}

		@Override
		protected void handleError(Context c, Exception e) {
			
		}
    }
}
