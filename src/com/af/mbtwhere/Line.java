package com.af.mbtwhere;

import java.util.ArrayList;

import android.util.Log;

public class Line {
	String name;
	final static int ANY = 0;
	final static int INBOUND = 1;
	final static int OUTBOUND = 2;
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
			if(direction==ANY) {
				if(code.equals(station.outbound_code) || code.equals(station.inbound_code)) {
					return station;
				}
			} else if (direction==INBOUND) {
				if(code.equals(station.inbound_code)) {
					return station;
				}
			} else if (direction==OUTBOUND) {
				if(code.equals(station.outbound_code)) {
					return station;
				}
			}
		}
		return null;
	}
	
	public String getCodeFor(String start, String end) {
		Station start_station = getStation(start);
		Station end_station = getStation(end);
		
		Station next_out = nextOutboundTo(start_station, end_station);
		Station next_in = nextInboundTo(start_station, end_station);
		if(next_out != null) {
			return next_out.outbound_code;
		} else if (next_in != null) {
			return next_in.inbound_code;
		} else {
			return null;
		}
	}
	
	public Station nextOutboundTo(Station start, Station end) {
		if(start.name.equals(end.name)) {
			return start;
		} else if(start.outbound_next_code != null && start.outbound_next_code.length() > 0 && !"NULL".equals(start.outbound_next_code)) {
			Station next_station = getStationByCode(start.outbound_next_code, OUTBOUND);
			if(next_station==null) {
				Log.v("MBTLine", "flipping from outbound to inbound");
				next_station = getStationByCode(start.outbound_next_code, INBOUND);
				return nextInboundTo(next_station, end);
			} else {
				return nextOutboundTo(next_station, end);
			}
		} else {
			return null;
		}
	}
	
	public Station nextInboundTo(Station start, Station end) {
		if(start.name.equals(end.name)) {
			return start;
		} else if(start.inbound_next_code != null && start.inbound_next_code.length() > 0 && !"NULL".equals(start.inbound_next_code)) {
			Station next_station = getStationByCode(start.inbound_next_code, INBOUND);
			if(next_station == null) {
				Log.v("MBTLine", "flipping from inbound to outbound");
				next_station = getStationByCode(start.inbound_next_code, OUTBOUND);
				return nextOutboundTo(next_station, end);
			}
			return nextInboundTo(next_station, end);
		} else {
			return null;
		}
	}
}
