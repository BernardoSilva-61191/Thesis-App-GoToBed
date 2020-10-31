package com.app.gotobed.sensor.graphs;

public class Measurement implements Comparable<Measurement> {

	private boolean valid;

	private int status;
	
	private long timestamp;

	private float hr;
	private float hrv;
	private float sv;
	private float rr;
	
	public Measurement(String data) throws Exception {
		
		if(data==null || data.indexOf(",")==-1) {
			valid=false;
			return;
		}
		
		String[] split=data.split(",");
		
		timestamp=Long.parseLong(split[0]);
		hr=Float.parseFloat(split[1]);
		rr=Float.parseFloat(split[2]);
		sv=Float.parseFloat(split[3]);
		hrv=Float.parseFloat(split[4]);
		// signal strength is [5]
		status=Integer.parseInt(split[6]);
		// b2b [7]
		// b2b' [8]
		// b2b'' [9]
		
		valid=true;
	}
	

	public boolean isValid() {
		return valid;
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	public int getStatus() {
		return status;
	}
	
	public boolean isOccuringAfter(long cutoffTimestamp) {
		return this.timestamp>cutoffTimestamp;
	}
	
	public float getHr() {
		return hr;
	}
	
	public float getHrv() {
		return hrv;
	}
	
	public float getRr() {
		return rr;
	}
	
	public float getSv() {
		return sv;
	}

	@Override
	public int compareTo(Measurement other) {
		return other.getTimestamp()<this.getTimestamp()?1:-1;
	}
	
}
