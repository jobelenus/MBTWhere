package com.af.mbtwhere;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;

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

import com.github.droidfu.concurrent.BetterAsyncTask;

public class GetLineFeed extends BetterAsyncTask<String, Void, ArrayList<JSONObject>> {
	public static String TAG = "GetLineFeed";
	private static long CACHE_TIMEOUT = 1*60;
	private HttpClient client = null;
	private Context c;
	private LineLayout l = null;
	private StationLayout sl = null;
	private String cachedResponse = null;
	private Date cachedTime = new Date();
	
	public GetLineFeed(Context c, StationLayout l) {
		super(c);
		this.c = c;
		this.sl = l;
	}
	
	public GetLineFeed(Context c, LineLayout l) {
		super(c);
		this.c = c;
		this.l = l;
	}
	
	protected void setDisplay(ArrayList<JSONObject> feed) {
		if(l != null) {
			Log.v(TAG, "set L");
			l.setDisplay(feed);
		}
		if(sl != null) {
			Log.v(TAG, "set SL");
			sl.setDisplay(feed);
		}
	}
	
	protected String getFeed(String feed_url) {
		Date now = new Date();
		long diff = now.getTime() - cachedTime.getTime();
		if(diff > CACHE_TIMEOUT || cachedResponse == null) {
			HttpGet getMethod = new HttpGet(feed_url);
			String responseBody = "";
			try {
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				responseBody = client.execute(getMethod, responseHandler);
				cachedResponse = responseBody;
				cachedTime = new Date();
			} catch(IOException e) {
				Log.v(TAG, "IOException: "+e+"feed: "+feed_url);
				return "";
			}
		}
		return cachedResponse;
	}
	
	protected ArrayList<JSONObject> findTime(String feedUrl) {
		ArrayList<JSONObject> feed = new ArrayList<JSONObject>();
		try {
			JSONArray records = new JSONArray(getFeed(feedUrl));
			for (int i = 0; i < records.length(); ++i) {
			    JSONObject record = records.getJSONObject(i);
			    Log.v(TAG, "record="+record);
			    feed.add(record);
			}
			//TODO: errors need to be reported	
		} catch(JSONException e) {
			Log.v(TAG, "JSONException: "+e);
		}
		return feed;
	}
	
	protected ArrayList<JSONObject> doCheckedInBackground(Context c, String... vargs) {
		client = new DefaultHttpClient();
		String feedUrl = vargs[0];
		return findTime(feedUrl);
	}
	
	//this gets executed on the UI thread, so don't clog it up
	protected void onProgressUpdate(Void... junk) {
		
	}

	protected void after(Context c, ArrayList<JSONObject> next) {
		Log.v(TAG, "next: "+next);
		setDisplay(next);
	}

	protected void handleError(Context c, Exception e) {
		
	}
}