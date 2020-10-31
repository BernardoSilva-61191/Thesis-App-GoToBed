/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.app.gotobed;

import org.apache.cordova.CordovaActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.app.gotobed.analysis.AnalysisJsInterface;
import com.app.gotobed.sensor.alarm.AlarmJsInterface;
import com.app.gotobed.sensor.graphanalysis.GraphSleepAnalysisJsInterface;
import com.app.gotobed.sensor.reader.ReaderJsInterface;
import com.app.gotobed.sensor.calibration.CalibrationJsInterface;
import com.app.gotobed.sensor.command.CommandJsInterface;
import com.app.gotobed.sensor.discovery.DiscoveryJsInterface;
import com.app.gotobed.sensor.graphs.GraphJsInterface;
import com.app.gotobed.sensor.runningmode.RunningModeJsInterface;
import com.app.gotobed.sensor.storage.StorageJsInterface;

public class CordovaApp extends CordovaActivity
{	
	public static final String BCG="BCG_sensor";
    public static final int STATUSBAR_ICON_NOTIFICATION_ID = 1;
	
	ReaderJsInterface rji;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        super.init();

        Context context = getApplicationContext();

        /*CharSequence text = Settings.Global.getString(getApplicationContext().getContentResolver(), Settings.Global.WIFI_SLEEP_POLICY);

        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        */

        WebView web = (WebView)this.appView.getEngine().getView();
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);       
                
        web.addJavascriptInterface(
            new AnalysisJsInterface(web),
            "SensorAnalysis"
           );        
      
        web.addJavascriptInterface(
        	new CalibrationJsInterface(web),
        	"SensorCalibration"
        );

        web.addJavascriptInterface(
        	new DiscoveryJsInterface(context),
        	"SensorDiscovery"
        );
        
        rji=new ReaderJsInterface(context);
        web.addJavascriptInterface(
        	rji,
        	"SensorReader"
        );

        web.addJavascriptInterface(
        	new CommandJsInterface(),
        	"SensorCmd"
        );

        web.addJavascriptInterface(
           	new StorageJsInterface(context.getFilesDir()),
           	"SensorStorage"
        );

        web.addJavascriptInterface(
           	new GraphJsInterface(this, web),
           	"SensorGraph"
        );

        web.addJavascriptInterface(
        	new RunningModeJsInterface(),
            "SensorRunningMode"
        );

        web.addJavascriptInterface(
                new AlarmJsInterface(context),
                "SensorAlarm"
        );

        web.addJavascriptInterface(
                new GraphSleepAnalysisJsInterface(context),
                "SensorSleepAnalysis"
        );


        settings.setAllowFileAccess(true);
        settings.setDomStorageEnabled(true);

        web.clearCache(true);
                
        // Set by <content src="index.html" /> in config.xml
        loadUrl(launchUrl);
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	if(rji!=null) {
    		rji.stopPolling();
    	}
    	Log.v(BCG, "Destroyed!");
    }

    @Override
    public void onTrimMemory(int level) {
    	Log.v(BCG, "---------- RECEIVED ON TRIM MEMORY LEVEL "+level);
    	super.onTrimMemory(level);
    }
}
