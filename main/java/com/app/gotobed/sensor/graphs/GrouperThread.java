package com.app.gotobed.sensor.graphs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.cordova.CordovaActivity;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.webkit.WebView;

import com.app.gotobed.CordovaApp;
import com.app.gotobed.sensor.storage.StorageJsInterface;
import com.app.gotobed.util.JavascriptExecutor;


public class GrouperThread extends Thread {

	private CordovaActivity mainActivity; // so we can access ui thread
	private WebView webView;
	
	private long resolution;
	private long millisecondsBack;
	
	
	public GrouperThread(CordovaActivity activity, WebView webView, long resolution, long millisecondsBack){
		this.mainActivity=activity;
		this.webView=webView;
		
		this.resolution=resolution;
		this.millisecondsBack=millisecondsBack;
	}
	
	@Override
	public void run() {
		
		File historyFile=new StorageJsInterface(mainActivity.getApplication().getFilesDir()).getHistoryFile(false);
		if(historyFile==null) {
			reportStatusToUi("");
		} else {
			long now=System.currentTimeMillis()/1000*1000;
			long cutOff=now-millisecondsBack;
			String data=calculateGroupings(historyFile, now, cutOff, resolution);
			reportStatusToUi(data);
		}
	}

	private String calculateGroupings(File historyFile, long now, long cutOff, long resolution) {
		
		long start=System.currentTimeMillis();
		
		long halfTime=now-(now-cutOff)/2;	
		
		List<Measurement> measurements=new ArrayList<Measurement>();

		// load history
		try {
			// if asking history from more than a day, fetch all items
			int secondsBack=(int)((now-cutOff)/1000);
			secondsBack+=30; // add some slack
			
			long interestingHistorySize=secondsBack*45; // typical line = 34 chars + newline + slack digits
			long uselessHistorySize=historyFile.length()-interestingHistorySize;
			
			FileReader fileReader = new FileReader(historyFile);
			BufferedReader br=new BufferedReader(fileReader);

			if(uselessHistorySize>0) {
				Log.v(
					CordovaApp.BCG,
					"Ignoring "+uselessHistorySize/1024+"kB from history"
				);
				fileReader.skip(uselessHistorySize);
				br.readLine(); // skip to end of line
			}
			
			Measurement measurement;
			String line=null;
			int count=0;
			while((line=br.readLine())!=null) {
				if(count%10000==0 && count>0 && line!=null) {
					Log.v(
						CordovaApp.BCG,
						count+" items read from sensor history so far ..."
					);
				}
				count++;
				try {
					measurement=new Measurement(line);
					if(measurement.isValid() && measurement.isOccuringAfter(cutOff)) {
						measurements.add(measurement);
					}
				} catch (Exception e) {
					Log.wtf(
						CordovaApp.BCG,
						"Unable to read measurement "+line,
						e
					);
				}
			}
			br.close();
		} catch (IOException ioe){
			ioe.printStackTrace();			
		}

		if(measurements.isEmpty()) {
			return "";
		}
		
		// sort by timestamp, just in case
		Collections.sort(measurements);
				
		// resolution is the screen resolution or number of samples if smaller
		long actualRes=Math.min(resolution, measurements.size());
		long groupWidth=(now-cutOff)/actualRes;
				
		long status0Count=0;
		long status1Count=0;
		long status2Count=0;
		long statusOtherCount=0;
		
		long outOfBedCount=0;
		long consecutiveStatus0Blocks=0;
		long consecutiveNonStatus0Blocks=0;
		
		// group by timestamp using resolution
		long currentTime=cutOff;
		GroupedMeasurement currentGroup=new GroupedMeasurement(currentTime, groupWidth);
		List<GroupedMeasurement> groupedMeasurements=new ArrayList<GroupedMeasurement>();	
		for(Measurement m: measurements) {

			if(m.getTimestamp()>halfTime) {
				
				int status=m.getStatus();
	
				// consecutive "out of bed"-statuses
				if(status==0) {
					consecutiveStatus0Blocks++;
					
					// reset "is this non-status-0 just a single glitchy measurement"-counter
					// because this IS a status 0 measurement
					consecutiveNonStatus0Blocks=0;
				} else {
					consecutiveNonStatus0Blocks++;
					
					// more than 15 non-0-status measurements mean this is not just a
					// temporary glitch, but there are actually a bunch of valid non-status-0
					// measurements so ...
					if(consecutiveNonStatus0Blocks>15) {					
						// ... if there was a status-0 phase going on for long enough before this ...
						if(consecutiveStatus0Blocks>15) {
							// ... then count it as out of bed phase
							outOfBedCount++;
							// more than 15 non-status-0 measurements, so next status 0 will
							// count as a new "out of bed" phase
							consecutiveStatus0Blocks=0;						
						}
					}
	
				}
				
				// count status
				switch(status) {
					case 0:status0Count++;break;
					case 1:status1Count++;break;
					case 2:status2Count++;break;
					default:statusOtherCount++;break;
				}
				
			}

			// multiple measurements per screen pixel - perform grouping
			if(!currentGroup.isCorrectForThisMeasurement(m)) {
				groupedMeasurements.add(currentGroup);
				do {
					currentTime+=groupWidth;
					currentGroup=new GroupedMeasurement(currentTime, groupWidth);
					if(!currentGroup.isCorrectForThisMeasurement(m)) {
						groupedMeasurements.add(currentGroup);
					}
				} while (!currentGroup.isCorrectForThisMeasurement(m));
			}
			currentGroup.addMeasurement(m);
		}
		groupedMeasurements.add(currentGroup);

		// last measurement belonged to an ongoing out-of-bed phase? ...
		if(consecutiveStatus0Blocks>15) {
			// .. remember to count it
			outOfBedCount++;
		}
		
		Log.v(CordovaApp.BCG, "Converting to json");

		// convert to JSON
		JSONObject all=new JSONObject();
		
		try {
			JSONObject status=new JSONObject();
			status.put("status0Count", status0Count);
			status.put("status1Count", status1Count);
			status.put("status2Count", status2Count);
			status.put("statusOtherCount", statusOtherCount);
			status.put("outOfBedCount", outOfBedCount);
			status.put("cutOff", cutOff);
			status.put("halfTime", halfTime);
			status.put("now", now);
			all.put("status", status);

			StringBuilder sb=new StringBuilder();
			for(GroupedMeasurement gm:groupedMeasurements) {
				sb.append(gm.getStartTime());
				sb.append(",");
				sb.append(gm.getAverageHr());
				sb.append(",");				
				sb.append(gm.getAverageRr());
				sb.append(",");
				sb.append(gm.getAverageSv());
				sb.append(",");
				sb.append(gm.getAverageHrv());
				sb.append(",");
				sb.append(0); // fake signal strength
				sb.append(",");
				sb.append(gm.getAverageStatus());
				sb.append(",0,0,0"); // fake b2b, b2b' and b2b''
				sb.append("\\n");
			}		
			all.put("measurements", sb.toString().trim());
		} catch (JSONException je) {
			Log.wtf(CordovaApp.BCG, je);
		}
		
		Log.v(CordovaApp.BCG, "Groupings done in "+(System.currentTimeMillis()-start)+"ms");

		return all.toString();
	}

	private void reportStatusToUi(final String jsonString) {
		if(this.mainActivity==null) {
			return;
		}
		
		this.mainActivity.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				JavascriptExecutor.execute(webView, "window.app.receiveGraphDataCallback", jsonString);
			}
		});
	}

}
