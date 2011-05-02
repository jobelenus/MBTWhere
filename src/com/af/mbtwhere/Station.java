package com.af.mbtwhere;

import java.util.ArrayList;

public class Station {
	String name;
	String lat;
	String lng;
	String flag;
	ArrayList<Route> inbound_routes = new ArrayList<Route>();
	ArrayList<Route> outbound_routes = new ArrayList<Route>();
}
