package com.af.mbtwhere;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.github.droidfu.concurrent.BetterAsyncTask;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ServiceFeed extends BetterAsyncTask<String, Void, ArrayList<String>>{
	public static String TAG = "ServiceFeed";
	private final HashMap<String, String> services = new HashMap<String, String>();
	private Context c;
	private LineLayout ll = null;
	private StationLayout sl = null;
	
	public ServiceFeed(Context c, LineLayout l) {
		super(c);
		init(c);
		this.ll = l;
	}
	
	public ServiceFeed(Context c, StationLayout l) {
		super(c);
		init(c);
		this.sl = l;
	}
	
	protected void init(Context c) {
        services.put("red", "http://talerts.com/rssfeed/alertsrss.aspx?15");
        services.put("orange", "http://talerts.com/rssfeed/alertsrss.aspx?16");
        services.put("blue", "http://talerts.com/rssfeed/alertsrss.aspx?18");
        services.put("green", "http://talerts.com/rssfeed/alertsrss.aspx?17");
		this.c = c;
	}
	
	protected void stopProgress() {
		if(ll != null) {
			ll.stopProgress();
		}
		if(sl != null) {
			sl.stopProgress();
		}
	}
	
	public ArrayList<String> getServiceUpdate(String line) {
		XmlPullParser xpp;
		ArrayList<String> messages = new ArrayList<String>();
		String xml = this.getXml(line);
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
	        xpp = factory.newPullParser();
	        xpp.setInput(new StringReader (xml));
        	messages = ServiceFeed.parseFeed(xpp);
        } catch(XmlPullParserException e) {
        	Log.v(TAG, "parsing exception" + e);
        } catch(IOException e) {
        	Log.v(TAG, "io exception" + e); //TODO: if BOM changes from xml data, we need to update this...
        } catch(ParseException e) {
        	Log.v(TAG, "parse exception" + e);
        } catch(Exception e) {
        	Log.v(TAG, "exception" + e);
        }
		return messages;
	}
	
	public static ArrayList<String> parseFeed(XmlPullParser xpp) throws XmlPullParserException, IOException, ParseException {
		ArrayList<String> messages = new ArrayList<String>();
		int eventType = xpp.getEventType();
    	while (eventType != XmlPullParser.END_DOCUMENT) {
    		String tag = "";
    		if(eventType == XmlPullParser.START_TAG) {
    			tag = xpp.getName();
    			if("item".equals(tag)) {
    				Log.v(TAG, "found item");
    				String message = parseItem(xpp);
    				if(message != null) {
    					messages.add(message);
    				}
    			}
            }
    		eventType = xpp.next();
    	}
		return messages;
	}
	
	public static String parseItem(XmlPullParser xpp) throws XmlPullParserException, IOException, ParseException {
		Log.v(TAG, "parsing item");
		String message = null;
		int eventType = xpp.next();
		String tag;
    	while(true) { 
    		if(eventType == XmlPullParser.START_TAG) {
    			tag = xpp.getName();
    			Log.v(TAG, "start: "+tag);
    			eventType = xpp.next();
    			if(eventType == XmlPullParser.TEXT) {
	    			if("description".equals(tag)) {
	    				message = xpp.getText();
	    				Log.v(TAG, "message: "+message);
	    			}
	    			if("pubDate".equals(tag)) {
	    				//formatted as such Mon, 25 Apr 2011 11:15:49 GMT
	    				String dateText = xpp.getText();
	    				SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss zzz");
	    				Date pubdate = sdf.parse(dateText);
	    				Date today = new Date();
	    				sdf.applyPattern("mm/dd/yy");
	    				if(sdf.format(pubdate) != sdf.format(today)) { //old update
	    					message = null; //return nothing
	    				}
	    				Log.v(TAG, "done parsing pubDate");
	    			}
    			}
    		} else if(eventType == XmlPullParser.END_TAG) {
    			tag = xpp.getName();
    			Log.v(TAG, "end: "+tag);
    			if("item".equals(tag)) {
    				return message;
    			}
    		}
    		eventType = xpp.next();
    	}
	}
	
	@SuppressWarnings("finally")
	public String getXml(String line) {
		HttpClient client = new DefaultHttpClient();
		String feed_url = services.get(line);
		HttpGet getMethod = new HttpGet(feed_url);
		String responseBody = "";
		try {
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			responseBody = client.execute(getMethod, responseHandler);
		} catch(Throwable t) { //TODO: don't catch everything, check connection first, and catch IOError
			Log.v(TAG, ""+t);
		} finally {
			String bomString = new String("ï»¿");
			if(bomString.equals(responseBody.substring(0, bomString.length()))) {
				responseBody = responseBody.substring(bomString.length());
			}
			Log.v(TAG, responseBody);
			return responseBody;
		}
	}

	protected ArrayList<String> doCheckedInBackground(Context c, String... vargs) {
		String line = vargs[0];
		return getServiceUpdate(line);
	}
	
	protected void after(Context c, ArrayList<String> messages) {
		stopProgress();
		if(messages.size() == 0) {
			Toast.makeText(c, R.string.no_updates, Toast.LENGTH_SHORT).show();
		} else {
			Intent i = new Intent(c, ServiceUpdate.class).putStringArrayListExtra("com.af.mbtwhere.ServiceUpdates", messages);
			c.startActivity(i);
		}
	}

	@Override
	protected void handleError(Context arg0, Exception arg1) {
		// TODO Auto-generated method stub
		
	}
}