package com.af.mbtwhere;

public class Route {
	public final String code;
	public final String nextCode;
	public final String prevCode;
	public final String flag;
	protected final Station station;
	
	public static class Builder {
		private String code;
		private String nextCode;
		private String prevCode;
		private String flag;
		private Station station;
		
		public Builder() { }
		
		public Builder code(String val) {
			code = val; return this;
		}
		public Builder nextCode(String val) {
			nextCode = val; return this;
		}
		public Builder prevCode(String val) {
			prevCode = val; return this;
		}
		public Builder flag(String val) {
			flag = val; return this;
		}
		public Builder station(Station val) {
			station = val; return this;
		}
		public Route build() {
			return new Route(code, nextCode, prevCode, flag, station);
		}
	}

	public Route(String code, String nextCode, String prevCode, String flag, Station station) {
		this.code = code;
		this.nextCode = nextCode;
		this.prevCode = prevCode;
		this.flag = flag;
		this.station = station;
	}
	
	@Override public boolean equals(Object o) {
		if (!(o instanceof Route)) {
			return false;
		}
		Route r = (Route)o; return r.code == code;
	}
	
	@Override public int hashCode() {
		return code.hashCode();
	}
	
	public Station nextStation(Line l) {
		return l.getStationByCode(nextCode);
	}
	
	public Station prevStation(Line l) {
		return l.getStationByCode(prevCode);
	}
	
	public boolean is_on(String flag) {
		return this.nextCode != null && this.nextCode.length() > 0 && !"NULL".equals(this.nextCode) && (this.flag == Line.ANY_BRANCH || this.flag.equals(flag));
	}
	
	@Override public String toString() {
		return code;
	}
}