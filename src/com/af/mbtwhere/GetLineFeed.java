package com.af.mbtwhere;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.github.droidfu.concurrent.BetterAsyncTask;

public class GetLineFeed extends BetterAsyncTask<String, Void, ArrayList<String>> {
	public static String TAG = "GetLineFeed";
	private HttpClient client = null;
	private Context c;
	private LineLayout l;
	
	public GetLineFeed(Context c, LineLayout l) {
		super(c);
		this.c = c;
		this.l = l;
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
	
	protected String getFeed(String feed_url) {
		HttpGet getMethod = new HttpGet(feed_url);
		String responseBody = "";
		try {
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			responseBody = client.execute(getMethod, responseHandler);
			
		} catch(IOException e) {
			Log.v(TAG, "IOException: "+e);
		}
		return responseBody;
	}
	
	protected ArrayList<String> findTime(String feed_url, String route_code, int numRecords) {
		ArrayList<String> times = new ArrayList<String>();
		try {
			JSONArray records = new JSONArray(getFeed(feed_url));
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
    			    String label = c.getString(R.string.expecting_train)+" "+time_remaining;
    			   	Log.v(TAG, "label="+label);
			    	times.add(label);
			    }
			}
		//TODO: errors need to be reported	
		} catch(JSONException e) {
			Log.v(TAG, "JSONException: "+e);
		}
		if(times.size() == 0) {
			times.add(c.getString(R.string.shrug));
		}
		return times;
	}
	
	protected ArrayList<String> doCheckedInBackground(Context c, String... vargs) {
		client = new DefaultHttpClient();
		String feedUrl = vargs[0];
		String routeCode = vargs[1];
		int numRoutes = Integer.parseInt(vargs[2]);
		
		ArrayList<String> times = findTime(feedUrl, routeCode, numRoutes);
		return times;
	}
	
	@Override
	//this gets executed on the UI thread, so don't clog it up
	protected void onProgressUpdate(Void... junk) {
		
	}

	@Override
	protected void after(Context c, ArrayList<String> next) {
		//client.getConnectionManager().shutdown();
		l.getProgress().setVisibility(View.INVISIBLE);
		l.setDisplay(next);
	}

	@Override
	protected void handleError(Context c, Exception e) {
		
	}
}