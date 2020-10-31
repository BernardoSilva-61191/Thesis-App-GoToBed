package com.app.gotobed.sensor.runningmode;

import android.util.Log;
import android.webkit.JavascriptInterface;

import com.app.gotobed.CordovaApp;
import com.app.gotobed.util.ApiCall;

public class RunningModeJsInterface {
		
	@JavascriptInterface
	public void setBCGRunningMode(String host) {
		
		Log.v(CordovaApp.BCG, "Setting BCG mode (mode 0) for sensor at "+host);
		
		new ApiCall(
			host,
			"/bcg/mode",
			"{ \"mode\": 0 }"
		).execute();
	}
}
