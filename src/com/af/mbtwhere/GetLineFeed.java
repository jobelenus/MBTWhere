package com.af.mbtwhere;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.github.droidfu.concurrent.BetterAsyncTask;

public class GetLineFeed extends BetterAsyncTask<String, Void, String> {
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
		client = new DefaultHttpClient();
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
    			    String label = c.getString(R.string.expecting_train)+" "+time_remaining;
    			   	Log.v(TAG, "label="+label);
			    	return label;
			    }
			}
			return c.getString(R.string.shrug);
		} catch(Throwable t) { //TODO: don't catch everything, check connection first, and catch IOError
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
		l.getProgress().setVisibility(View.INVISIBLE);
		l.getDisplay().setText(next);
	}

	@Override
	protected void handleError(Context c, Exception e) {
		
	}
}