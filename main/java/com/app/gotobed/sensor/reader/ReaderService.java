package com.app.gotobed.sensor.reader;

import com.app.gotobed.CordovaApp;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ReaderService extends Service {

	private ReaderThread srt;
	private String previousHost=null;
	
	@Override
	public void onCreate() {}
	
	private void stopPolling() {
		if(srt!=null && !srt.isInterrupted()) {
			Log.v(CordovaApp.BCG, "Stopping previous reader thread.");
            NotificationManager mNotificationManager =
                    (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(CordovaApp.STATUSBAR_ICON_NOTIFICATION_ID);
			srt.interrupt();
		} else {
			Log.v(CordovaApp.BCG, "There was no previous reader thread or it was not running.");			
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if(intent==null && previousHost==null) {
			Log.wtf(CordovaApp.BCG, "Problems starting reader service - intent was null and did not know which host to connect to, doing nothing.");
			return super.onStartCommand(intent, flags, startId);			
		}

		stopPolling();

		if(intent==null) {
			Log.wtf(CordovaApp.BCG, "Reader service received intent that was null - trying to connect to "+previousHost+" anyway");
		} else {
			previousHost=intent.getExtras().getString("host");
		}
		
		try {
		
			srt=new ReaderThread(this, this.getFilesDir(), previousHost, 8080);
			srt.start();

			Log.v(CordovaApp.BCG, "Sensor reader service started in START_STICKY mode");
			return START_STICKY;

		} catch (Exception e) {	
			Log.wtf(CordovaApp.BCG, "Problems starting reader service, not starting it (calling super)", e);
			return super.onStartCommand(intent, flags, startId);
		}
		
	}		
	
	@Override
	public void onDestroy() {
		stopPolling();
		
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
