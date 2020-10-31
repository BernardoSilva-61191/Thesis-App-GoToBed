package com.app.gotobed.sensor.discovery;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.app.gotobed.CordovaApp;

public class DiscoveryJsInterface {

	DiscovererThread sdt;
	
	Context context;
	
	public DiscoveryJsInterface(Context context) {
		this.context=context;
	}
	
	@JavascriptInterface
	public void startDiscovery() {
		Log.v(CordovaApp.BCG, "Starting discovery");
		
		if(sdt!=null && sdt.isAlive()) {
			sdt.interrupt();
		}

		sdt=new DiscovererThread();
		sdt.start();		
	}
	
	@JavascriptInterface
	public String getDiscoveredSensors() {
		JSONArray result=new JSONArray();

		if(sdt==null) {
			return result.toString();
		}

		List<DiscoveredSensor> discoveredSensors = sdt.getDiscoveredSensorsAndInterrupt();

		for(DiscoveredSensor sensor : discoveredSensors) {
			try {
				JSONObject sensorJson=new JSONObject();
				sensorJson.put("ip", sensor.getIp());
				sensorJson.put("hostname", sensor.getHostname());
				result.put(sensorJson);
			} catch (JSONException je) {
				Log.wtf(CordovaApp.BCG, je);
			}
		}
				
		return result.toString();
	}
	
}
