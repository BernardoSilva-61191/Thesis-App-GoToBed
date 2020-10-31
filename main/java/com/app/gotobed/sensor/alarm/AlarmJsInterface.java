package com.app.gotobed.sensor.alarm;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.webkit.JavascriptInterface;


public class AlarmJsInterface {

    Context context;

    public AlarmJsInterface(Context context) {
        this.context=context;
    }

    @JavascriptInterface
    public void startAlarmActivity() {
        Intent intent = new Intent(context, AlarmActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
