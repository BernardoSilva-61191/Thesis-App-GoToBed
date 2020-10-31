package com.app.gotobed.analysis;

import org.apache.cordova.CordovaActivity;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;

public class AnalysisJsInterface {

	CordovaActivity activity;
	WebView webView;
	
	public AnalysisJsInterface(WebView webView) {
		this.webView=webView;
	}
	
	@JavascriptInterface
	public void getValuesForResolution(int resolution, String assetName) {
		ReadAnalysisAsyncTask raat=new ReadAnalysisAsyncTask(assetName,  webView, resolution);
		raat.execute();		
	}
	
}
