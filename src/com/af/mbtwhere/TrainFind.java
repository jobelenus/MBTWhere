package com.af.mbtwhere;

import java.io.IOException;
import java.text.SimpleDateFormat;
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
            	progress.setVisibility(View.VISIBLE);
            	new GetLineFeed().execute(red_feed, route);
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
    			    String this_code = record.getString("PlatformKey");
    			    String this_type = record.getString("InformationType");
    			    if(this_code.equals(route_code) && "Predicted".equals(this_type)) {
    			    	SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy h:m:s a");
    			    	Date this_time = sdf.parse(record.getString("Time"));
    			    	Date today = new Date();
    			    	//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd h:m:s a");
    			    	long diff = this_time.getTime() - today.getTime();
    			    	if(diff > 0) {
	    			    	String label = getString(R.string.expecting_train)+String.format(" %02d:%02ld", 
	    			    			TimeUnit.MILLISECONDS.toSeconds(diff) / 60,
	    			    		    TimeUnit.MILLISECONDS.toSeconds(diff) % 60 );
	    			    	publishProgress(label);
    			    	} else {
    			    		publishProgress(getString(R.string.now_train));
    			    	}
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
    				station.outbound_code = parser.getAttributeValue(0);
    				station.outbound_prev_code = parser.getAttributeValue(1);
    				station.outbound_next_code = parser.getAttributeValue(2);
    			} else if("inbound".equals(tag)) {
    				i++;
    				station.inbound_code = parser.getAttributeValue(0);
    				station.inbound_prev_code = parser.getAttributeValue(1);
    				station.inbound_next_code = parser.getAttributeValue(2);
    			}
    		}
    		eventType = parser.next();
    	}
    	return station;
    }
}