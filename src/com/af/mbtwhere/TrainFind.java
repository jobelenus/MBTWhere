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
import com.af.mbtwhere.Route.Builder;

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
    	Line line = null;
    	while (eventType != XmlPullParser.END_DOCUMENT) {
    		String tag = "";
    		if(eventType == XmlPullParser.START_TAG) {
    			tag = parser.getName();
    			if("line".equals(tag)) {
    				line = new Line(parser.getAttributeValue(0));
    				lines.put(line.name, line);
    			} else if("station".equals(tag)) {
    				if(line != null ) {
    					line.stations.add(parseStation(parser));
    				} else {
    					throw new XmlPullParserException("Data Format Exception: Station tag precedes line tag");
    				}
    			}
            }
    		eventType = parser.next();
    	}
    	return lines;
    }
    
    public Station parseStation(XmlPullParser parser) throws XmlPullParserException, IOException {
    	Station station = new Station(parser.getAttributeValue(0), parser.getAttributeValue(1), parser.getAttributeValue(2), parser.getAttributeValue(3));
		int eventType = parser.next();
		String tag = "";
    	while(true) { 
    		if(eventType == XmlPullParser.START_TAG) {
    			tag = parser.getName();
    			if("outbound".equals(tag) || "inbound".equals(tag)) {
    				Builder b  = new Route.Builder();
    				b.code(parser.getAttributeValue(0)).prevCode(parser.getAttributeValue(1)).nextCode(parser.getAttributeValue(2)).station(station);
    				try {
    					b.flag(parser.getAttributeValue(3));
    				} catch(Exception e) {
    					b.flag(Line.ANY_BRANCH);
    				}
    				Route r = b.build();
    				if("inbound".equals(tag)) {
    					station.inbound_routes.add(r);
    				} else {
    					station.outbound_routes.add(r);
    				}
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