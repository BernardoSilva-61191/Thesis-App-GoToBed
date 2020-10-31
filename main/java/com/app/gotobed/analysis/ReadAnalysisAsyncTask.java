package com.app.gotobed.analysis;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebView;

import com.app.gotobed.CordovaApp;
import com.app.gotobed.util.JavascriptExecutor;

public class ReadAnalysisAsyncTask extends AsyncTask<String, Void, String> {
	
	private String assetName;
	private WebView webView;
	private int resolution;
	
	public ReadAnalysisAsyncTask(String fileName, WebView webView, int resolution) {
		this.assetName=fileName;
		this.webView=webView;
		this.resolution=resolution;
	}
		
	public void execute() {
		this.execute(assetName);
	}
	
	@Override
	protected String doInBackground(String... assets) {

		String line;
		InputStream is;
		BufferedReader br;

		try {
			// read values file
			AnalysisValues values=new AnalysisValues();
			is = webView.getContext().getAssets().open(assetName+"_values.txt");
			br = new BufferedReader(new InputStreamReader(is));
			line = br.readLine(); 
			while(line!=null) {
				values.consume(line);
				line=br.readLine();
			}			
			br.close();
			
			List<String> lines=new ArrayList<String>();
		
			// read data file
			is = webView.getContext().getAssets().open(assetName+"_data.txt");
			br = new BufferedReader(new InputStreamReader(is));
			line = br.readLine(); 
			float lineCount=0;
			while(line!=null) {
				// skip header
				if(lineCount>0) {
					lines.add(line);					
				}
				line=br.readLine();

				lineCount=lineCount+1.f;
			}			
			br.close();
			Log.v(CordovaApp.BCG, "Analysis - read "+lines.size()+" lines and processing for resolution "+resolution);

			// group file
			List<AnalysisItem> items=new ArrayList<AnalysisItem>();
			AnalysisItem currentItem=null;
			
			float currentLineNumber=0;
			for(String currentLine : lines) {
				int pixelForThisLine=(int)Math.floor((currentLineNumber/lineCount*resolution));
				if(currentItem==null) {
					currentItem=new AnalysisItem(currentLine, pixelForThisLine);
				} else {
					if(currentItem.isValidForPixel(pixelForThisLine)) {
						currentItem.addItem(currentLine);
					} else {
						items.add(currentItem);
						currentItem=new AnalysisItem(currentLine, pixelForThisLine);
					}
				}				
				currentLineNumber++;
			}
			items.add(currentItem);
	
			// convert to json
			JSONObject json=new JSONObject();
			json.put("recovery", values.getRecovery());
			json.put("relaxation", values.getRelaxation());
			json.put("sleepTime", values.getSleepTime());
			json.put("inBedTime", values.getInBedTime());
			json.put("hrScaling", values.getScaling());
			json.put("lastTime", items.get(items.size()-1).getMaxTime());

			JSONArray ja=new JSONArray();
			for(AnalysisItem item:items) {
				JSONObject jo=new JSONObject();
				jo.put("time", item.getAverageTime());
				jo.put("hr", item.getAverageHr());
				jo.put("hrv", item.getAverageHrv());
				jo.put("rindex", item.getAverageRIndex());
				jo.put("sleep", item.getAverageSleep());
				ja.put(jo);
			}
			json.put("data", ja);
			
			return json.toString();

		} catch (Exception e) {
			Log.wtf(CordovaApp.BCG, e);
			return "";
		}
	}
	
	@Override
	protected void onPostExecute(String result) {
		JavascriptExecutor.execute(webView, "window.app.receiveAnalysisGraphDataCallback", result);
	}

	
}
