package com.app.gotobed.sensor.graphs;

import org.apache.cordova.CordovaActivity;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;

public class GraphJsInterface {

	CordovaActivity activity;
	WebView webView;
	
	public GraphJsInterface(CordovaActivity activity, WebView webView) {
		this.webView=webView;
		this.activity=activity;
	}
	
	@JavascriptInterface
	public void getValuesForResolution(int resolution, long millisecondsBack) {
		GrouperThread gt = new GrouperThread(activity, webView, resolution, millisecondsBack);
		gt.setPriority(Thread.MAX_PRIORITY);
		gt.start();
		
	}
	
}
