package com.af.mbtwhere;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import java.util.HashMap;
import com.af.mbtwhere.LineLayout;

public class TrainFind extends Activity {
	private static final String TAG = "MBTWhere";
	protected HashMap<String, String> feeds = new HashMap<String, String>();
	protected HashMap<String, Line> lines = new HashMap<String, Line>();
	protected LineLayout[] linePanels;
	HorizontalPager pager;
	
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        
        feeds.put("red", "http://developer.mbta.com/Data/red.json");
        feeds.put("orange", "http://developer.mbta.com/Data/orange.json");
        feeds.put("blue", "http://developer.mbta.com/Data/blue.json");
        
        XmlPullParser xpp = getResources().getXml(R.xml.stations);
        try {
        	lines = parseLine(xpp);
        } catch(XmlPullParserException e) {
        	Log.v(TAG, "parsing exception" + e);
        } catch(IOException e) {
        	Log.v(TAG, "io exception" + e);
        }
        pager = (HorizontalPager)findViewById(R.id.lines);
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        linePanels = new LineLayout[lines.size()];
        int i = 0;
        for(Line line : lines.values()) {
        	LineLayout linePanel = new LineLayout(this, feeds.get(line.name), line);
        	inflater.inflate(R.layout.picker, linePanel);
        	linePanels[i] = linePanel;
        	pager.addView(linePanel);
        	linePanel.setup();
        	i++;
        }
    }
    
    private final HorizontalPager.OnScreenSwitchListener onScreenSwitchListener =
        new HorizontalPager.OnScreenSwitchListener() {
            public void onScreenSwitched(final int screen) {
                /*
                 * this method is executed if a screen has been activated, i.e. the screen is
                 * completely visible and the animation has stopped (might be useful for
                 * removing / adding new views)
                 */
            }
        };
    
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
		String tag = "";
    	while(true) { 
    		if(eventType == XmlPullParser.START_TAG) {
    			tag = parser.getName();
    			if("outbound".equals(tag)) {
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
    		} else if(eventType == XmlPullParser.END_TAG) {
    			tag = parser.getName();
    			if("station".equals(tag)) {
    				return station;
    			}
    		}
    		eventType = parser.next();
    	}
    }
}