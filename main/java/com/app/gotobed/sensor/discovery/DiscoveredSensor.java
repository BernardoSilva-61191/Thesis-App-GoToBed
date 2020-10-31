package com.app.gotobed.sensor.discovery;

import org.json.JSONException;
import org.json.JSONObject;

public class DiscoveredSensor {

	private boolean valid;
	
	private String type;
	private String ip;
	private String hostname;

	public DiscoveredSensor(byte[] data) {

		String rawData=new String(data);
				
		valid=false;
		
		if(	rawData!=null &&
			rawData.indexOf("SCS-NOTIFY")!=-1 &&
			rawData.indexOf('}')!=-1
		) {
			try {
				JSONObject jsonObj=new JSONObject(rawData.substring(0, rawData.indexOf('}')+1));
				setType(jsonObj.getString("type"));
				setHostname(jsonObj.getString("hostname"));
				setIp(jsonObj.getString("ip"));
				valid=true;
			} catch (JSONException je) {
				// intentionally left empty
			}
		}

	}
	
	public boolean isValid() {
		return valid;
	}
	
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getType() {
		return type;
	}
	
}
