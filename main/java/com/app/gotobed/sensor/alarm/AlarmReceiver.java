package com.app.gotobed.sensor.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.app.gotobed.sensor.reader.ReaderThread;

/**
 * Created by Bernardo on 06/05/2018.
 */

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("We are in the receiver.", "Yes");

        String get_your_string = intent.getExtras().getString("extra");


        Log.e("What is the key", get_your_string);

        String sleepStage = intent.getExtras().getString("sleepStage");

        Intent service_intent = new Intent(context, RingtonePlayingService.class);

        service_intent.putExtra("sleepStage", sleepStage);
        service_intent.putExtra("extra", get_your_string);

        context.startService(service_intent);
    }
}

