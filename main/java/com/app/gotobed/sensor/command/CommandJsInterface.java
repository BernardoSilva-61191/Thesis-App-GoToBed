package com.app.gotobed.sensor.command;

import android.webkit.JavascriptInterface;

import com.app.gotobed.util.ApiCall;

public class CommandJsInterface {
		
	@JavascriptInterface
	public void restore(String host) {
		new ApiCall(
			host,
			"/bcg/cmd",
			"{ \"cmd\": \"restore\" } "
		).execute();
	}
}
