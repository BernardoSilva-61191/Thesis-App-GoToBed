package com.app.gotobed.sensor.reader;

import com.app.gotobed.CordovaApp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.webkit.JavascriptInterface;

public class ReaderJsInterface {

	private Context context;
	private int mId = 1;
		
	public ReaderJsInterface(Context context) {
		this.context=context;
	}
	
	@JavascriptInterface
	public void startPolling(String host) {
		try {
			Intent intent=new Intent(context, ReaderService.class);
			intent.putExtra("host", host);
			context.startService(intent);

		} catch (Exception e) {
			Log.wtf(CordovaApp.BCG, "Got an exception while starting polling to host "+host, e);
		}
	}

	@JavascriptInterface
	public void stopPolling() {
		Log.d(CordovaApp.BCG, "ReaderJsInterface stopping polling");
		Intent intent=new Intent(context, ReaderService.class);
		context.stopService(intent);
	}
}
