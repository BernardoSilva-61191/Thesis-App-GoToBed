package com.app.gotobed.sensor.graphanalysis;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.app.gotobed.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Random;

public class GraphSleepAnalysisActivity extends AppCompatActivity {

    private LineGraphSeries<DataPoint> series;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_sleep_analysis);


        GraphView graphView = (GraphView) findViewById(R.id.graph);

        series = new LineGraphSeries<DataPoint>();

        int i = 0;
        while (i < 1500) {

            int x = i;
            String sleepStage = getIntent().getExtras().getString("stage");

            int y = Integer.parseInt(sleepStage);

            series.appendData(new DataPoint(x, y), true, 1500);
            i = i + 30;
        }

        graphView.addSeries(series);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        GraphView graphView = (GraphView) findViewById(R.id.graph);

        series = new LineGraphSeries<DataPoint>();

        String time = data.getExtras().getString("time");


        String sleepStage = data.getExtras().getString("stage");
        System.out.println("last one -> " + sleepStage);

        int x = Integer.parseInt(time);
        int y = Integer.parseInt(sleepStage);

        series.appendData(new DataPoint(x, y), true, 1500);


        graphView.addSeries(series);
    }
}
