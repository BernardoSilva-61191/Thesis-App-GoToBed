package com.app.gotobed.sensor.calibration;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import android.util.Log;

import com.app.gotobed.CordovaApp;
import com.app.gotobed.util.ApiCallWithJsCallback;

public class CalibrationJsInterface {
		
	private WebView webView;
	
	public CalibrationJsInterface(WebView webView) {
		this.webView=webView;
	}
	
	@JavascriptInterface
	public void sendCalibrationParameters(String host, String parameters) {
        String parameterString = "{\"pars\": \"" + parameters + "\"}";
        Log.wtf(CordovaApp.BCG, parameterString);
		new ApiCallWithJsCallback(
			host,
			"/bcg/pars", parameterString,
			webView,
			"window.app.sendCalibrationParametersCallback"
		).execute();
	}

	@JavascriptInterface
	public void queryCalibrationStatus(String host) {
		new ApiCallWithJsCallback(
			host,
			"/bcg/cali",
			null,
			webView,
			"window.app.receiveCalibrationStatus"
		).execute();
	}
	
}
