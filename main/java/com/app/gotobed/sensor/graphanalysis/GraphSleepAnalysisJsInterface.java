package com.app.gotobed.sensor.graphanalysis;

import android.content.Context;
import android.content.Intent;
import android.webkit.JavascriptInterface;


public class GraphSleepAnalysisJsInterface {

    Context context;

    public GraphSleepAnalysisJsInterface(Context context) {
        this.context=context;
    }

    @JavascriptInterface
    public void startSleepAnalysisActivity() {
        Intent intent = new Intent(context, GraphSleepAnalysisActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
