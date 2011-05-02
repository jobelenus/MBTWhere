package com.af.mbtwhere;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.ArrayList;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;
import android.widget.ViewFlipper;
import android.os.AsyncTask;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class TrainFind extends Activity implements OnGestureListener {
	private static final String TAG = "MBTWhere";
	private static final String red_feed = "http://developer.mbta.com/Data/red.json";
	private static final String orange_feed = "http://developer.mbta.com/Data/orange.json";
	protected HashMap<String, Line> lines = new HashMap<String, Line>();
    protected Spinner spin_start;
    protected Spinner spin_end;
    protected TextView time_display;
    protected ViewFlipper flipper;
    protected Button button;
    protected String[] red_stations;
    protected String red_start = "";
    protected String red_end = "";
    protected ProgressBar progress;
    private GestureDetector gestureScanner;
	
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        
        XmlPullParser xpp = getResources().getXml(R.xml.stations);
        try {
        	lines = parseLine(xpp);
        } catch(XmlPullParserException e) {
        	Log.v(TAG, "parsing exception" + e);
        } catch(IOException e) {
        	Log.v(TAG, "io exception" + e);
        }
        
        gestureScanner = new GestureDetector(this);
        flipper = (ViewFlipper)findViewById(R.id.lines);
        spin_start = (Spinner)findViewById(R.id.spinner_red_start);
        spin_end = (Spinner)findViewById(R.id.spinner_red_end);
        button = (Button)findViewById(R.id.find_red);
        time_display = (TextView)findViewById(R.id.red_display);
        progress = (ProgressBar)findViewById(R.id.red_progress);
        progress.setVisibility(View.INVISIBLE);
        
        red_stations = ((Line)lines.get("red")).getStationsByName();
        //init for unchanging spinners
        red_start = red_stations[0];
        red_end = red_stations[0];
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	String route = ((Line) lines.get("red")).getCodeFor(red_start, red_end);
            	Log.v(TAG, "start="+red_start+", end="+red_end);
            	if(red_start.equals(red_end)) {
            		time_display.setText(R.string.already_there);
            	} else {
	            	Log.v(TAG, "route="+route);
	            	progress.setVisibility(View.VISIBLE);
	            	time_display.setText("");
	            	new GetLineFeed().execute(red_feed, route);
            	}
            }
        });
        
        spin_start.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
            	//apparently this runs in the context of the activity, w00t
            	red_start = red_stations[position];
            }
            
            public void onNothingSelected(AdapterView<?> parent) {
            	red_start = "";
            }   	
		});
        spin_end.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
            	//apparently this runs in the context of the activity, w00t
            	red_end = red_stations[position];
            }
            
            public void onNothingSelected(AdapterView<?> parent) {
            	red_end = "";
            }   	
		});
        ArrayAdapter<String> aa_start = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, red_stations);
        aa_start.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_start.setAdapter(aa_start);
        ArrayAdapter<String> aa_end = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, red_stations);
        aa_end.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_end.setAdapter(aa_start);
    }
    
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    	if (velocityX > 0) {
    		flipper.showNext();
    	} else {
    		flipper.showPrevious();
    	}
        return true;
    }
    
    public boolean onTouchEvent(MotionEvent me) {
    	return gestureScanner.onTouchEvent(me);
    }
    public boolean onDown(MotionEvent e) {
    	return true;
    }
    public void onLongPress(MotionEvent e) {
    }
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    	return true;
    }
    public void onShowPress(MotionEvent e) {	
    }
    public boolean onSingleTapUp(MotionEvent e) {
    	return true;
    }
    
    class GetLineFeed extends AsyncTask<String, String, Void> {
    	HttpClient client = null;
    	
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
    	protected Void doInBackground(String... vargs) {
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
	    			    String label = getString(R.string.expecting_train)+" "+time_remaining;
	    			   	Log.v(TAG, "label="+label);
	    			   	publishProgress(label);
    			    	return null;
    			    }
    			}
    			publishProgress(getString(R.string.shrug));
    		} catch(Throwable t) {
    			Log.v(TAG, ""+t);
    			//TODO: errors need to be reported
    			//Toast.makeText(this, "Request failed: "+t.toString(), Toast.LENGTH_LONG).show();
    		}
    		return null;
    	}
    	
    	@Override
    	//this gets executed on the UI thread, so don't clog it up
    	protected void onProgressUpdate(String... next_train_at) {
    		String next = next_train_at[0];
    		time_display.setText(next);
    	}
    	
    	@Override
    	protected void onPostExecute(Void junk) {
    		//client.getConnectionManager().shutdown();
    		progress.setVisibility(View.INVISIBLE);
    	}
    }
    
    public HashMap<String, Line> parseLine(XmlPullParser parser) throws XmlPullParserException, IOException {
    	HashMap<String, Line> lines = new HashMap<String, Line>();
    	int eventType = parser.getEventType();
    	Line line = new Line();
    	while (eventType != XmlPullParser.END_DOCUMENT) {
    		String tag = "";
    		if(eventType == XmlPullParser.START_TAG) {
    			tag = parser.getName();
    			if("line".equals(tag)) {
    				line = new Line();
    				line.name = parser.getAttributeValue(0);
    				lines.put(line.name, line);
    			} else if("station".equals(tag)) {
    				line.stations.add(parseStation(parser));
    			}
            }
    		eventType = parser.next();
    	}
    	return lines;
    }
    
    public Station parseStation(XmlPullParser parser) throws XmlPullParserException, IOException {
    	Station station = new Station();
		station.name = parser.getAttributeValue(0);
		station.lat = parser.getAttributeValue(1);
		station.lng = parser.getAttributeValue(2);
		station.flag = parser.getAttributeValue(3);
		int eventType = parser.next();
		int i = 0;
    	while(i < 2) { //only two tags per station
    		String tag = "";
    		if(eventType == XmlPullParser.START_TAG) {
    			tag = parser.getName();
    			if("outbound".equals(tag)) {
    				i++;
    				Route new_out = new Route();
    				new_out.code = parser.getAttributeValue(0);
    				new_out.prev_code = parser.getAttributeValue(1);
    				new_out.next_code = parser.getAttributeValue(2);
    				try {
    					new_out.flag = parser.getAttributeValue(3);
    				} catch(Exception e) {
    					new_out.flag = Line.ANY_BRANCH;
    				}
    				new_out.station = station;
    				station.outbound_routes.add(new_out);
    			} else if("inbound".equals(tag)) {
    				i++;
    				Route new_in = new Route();
    				new_in.code = parser.getAttributeValue(0);
    				new_in.prev_code = parser.getAttributeValue(1);
    				new_in.next_code = parser.getAttributeValue(2);
    				try {
    					new_in.flag = parser.getAttributeValue(3);
    				} catch(Exception e) {
    					new_in.flag = Line.ANY_BRANCH;
    				}
    				new_in.station = station;
    				station.inbound_routes.add(new_in);
    			}
    		}
    		eventType = parser.next();
    	}
    	return station;
    }
}