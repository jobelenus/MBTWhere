package com.af.mbtwhere;

import java.util.ArrayList;

import android.util.Log;

public class Line {
	String name;
	final static int ANY = 0;
	final static int INBOUND = 1;
	final static int OUTBOUND = 2;
	static final String ANY_BRANCH = "-1";
	static final String BRAINTREE_BRANCH = "0";
	static final String ASHMONT_BRANCH = "1";
	ArrayList<Station> stations = new ArrayList<Station>();
	
	public String[] getStationsByName() {
		String[] ret = new String[stations.size()];
		int i = 0;
		for(Station station : stations) {
			ret[i++] = station.name;
		}
		return ret;
	}
	
	public Station getStation(String name) {
		for(Station station : stations) {
			if(name.equals(station.name)) {
				return station;
			}
		}
		return null;
	}
	
	public Station getStationByCode(String code) {
		return getStationByCode(code, ANY);
	}
	
	public Station getStationByCode(String code, int direction) {
		for(Station station : stations) {
			for(Route route: station.inbound_routes) {
				if (direction==INBOUND || direction==ANY) {
					if(code.equals(route.code)) {
						return route.station;
					}
				}
			}
			for(Route route: station.outbound_routes) {
				if (direction==OUTBOUND || direction==ANY) {
					if(code.equals(route.code)) {
						return route.station;
					}
				}
			}
		}
		return null;
	}
	
	public String getCodeFor(String start, String end) {
		Station start_station = getStation(start);
		Station end_station = getStation(end);
		
		Route next_out = nextOutboundTo(start_station, end_station);
		Route next_in = nextInboundTo(start_station, end_station);
		if(next_out != null) {
			return next_out.code;
		} else if (next_in != null) {
			return next_in.code;
		} else {
			return null;
		}
	}
	
	public Route nextOutboundTo(Station start, Station end) {
		if(start.name.equals(end.name)) {
			return start.outbound_routes.get(0); //doesn't matter which, since we are referencing `code` which is the same for all outbound routes
		} else {
			for(Route route : start.outbound_routes) {
				if(route.next_code != null && route.next_code.length() > 0 && !"NULL".equals(route.next_code) && (route.flag == Line.ANY_BRANCH || route.flag == end.flag)) {
					Station next_station = getStationByCode(route.next_code, OUTBOUND);
					if(next_station==null) {
						Log.v("MBTLine", "flipping from outbound to inbound");
						next_station = getStationByCode(route.next_code, INBOUND);
						return nextInboundTo(next_station, end);
					} else {
						return nextOutboundTo(next_station, end);
					}
				}
			}
			return null;
		}
	}
	
	public Route nextInboundTo(Station start, Station end) {
		if(start.name.equals(end.name)) {
			return start.inbound_routes.get(0);
		} else {
			for(Route route : start.inbound_routes) {
				if(route.next_code != null && route.next_code.length() > 0 && !"NULL".equals(route.next_code) && (route.flag == Line.ANY_BRANCH || route.flag == end.flag)) {
					Station next_station = getStationByCode(route.next_code, INBOUND);
					if(next_station == null) {
						Log.v("MBTLine", "flipping from inbound to outbound");
						next_station = getStationByCode(route.next_code, OUTBOUND);
						return nextOutboundTo(next_station, end);
					}
					return nextInboundTo(next_station, end);
				}
			}
			return null;
		}
	}
}
