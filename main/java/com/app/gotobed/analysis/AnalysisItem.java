package com.app.gotobed.analysis;

import com.app.gotobed.CordovaApp;

import android.util.Log;

public class AnalysisItem {

	private float maxTime=0;
	private float time=0;
	private float hr=0;
	private float hrv=0;
	private float rIndex=0;
	private float sleep=0;
	
	int forPixel=0;
	
	int items=0;
	
	public AnalysisItem(String rawString, int forPixel) {
		this.forPixel=forPixel;
		addItem(rawString);		
	}
	
	public boolean isValidForPixel(int pixel) {
		return pixel==forPixel;
	}
	
	public void addItem(String rawString) {
		try {

			String[] split = rawString.split(",");
			float currentTime=Float.parseFloat(split[0]);
			maxTime=Math.max(maxTime, currentTime);

			time+=currentTime;
			hr+=Float.parseFloat(split[1]);
			hrv+=Float.parseFloat(split[2]);
			rIndex+=Float.parseFloat(split[3]);
			sleep+=Float.parseFloat(split[4]);
			items++;
			
		} catch (Exception e) {
			Log.wtf(CordovaApp.BCG, e);
		}
	}
	
	public float getAverageTime(){
		return items==0?0:time/items;
	}

	public float getAverageHr(){
		return items==0?0:hr/items;
	}

	public float getAverageHrv(){
		return items==0?0:hrv/items;
	}

	public float getAverageRIndex(){
		return items==0?0:rIndex/items;
	}

	public float getAverageSleep(){
		return items==0?0:sleep/items;
	}
	
	public float getMaxTime() {
		return maxTime;
	}

}
