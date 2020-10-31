package com.app.gotobed.sensor.alarm;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import com.app.gotobed.CordovaApp;
import com.app.gotobed.R;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

import static android.content.Context.ALARM_SERVICE;

public class AlarmActivity extends AppCompatActivity {

    AlarmManager alarm_manager;
    TimePicker alarm_timepicker;
    TextView update_text;
    TextView date_text;
    Context context;
    PendingIntent pending_intent;
    int year;
    int month;
    int day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        this.context = this;

        alarm_manager = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarm_timepicker = (TimePicker) findViewById(R.id.timePicker);

        update_text = (TextView) findViewById(R.id.update_text);

        date_text = (TextView) findViewById(R.id.dateText);

        final Calendar calendar = Calendar.getInstance();

        final String currentDate = DateFormat.getDateInstance().format(calendar.getTime());

        date_text.setText(currentDate);

        final Intent intent = new Intent(this.context, AlarmReceiver.class);

        Button alarm_on = (Button) findViewById(R.id.alarm_on);
        alarm_on.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {

                calendar.set(Calendar.HOUR_OF_DAY, alarm_timepicker.getHour());
                calendar.set(Calendar.MINUTE, alarm_timepicker.getMinute());

                int hour = alarm_timepicker.getHour();
                int minute = alarm_timepicker.getMinute();

                String hour_string = String.valueOf(hour);
                String minute_string = String.valueOf(minute);

                if (hour > 12) {
                    hour_string = String.valueOf(hour - 12);
                }

                if (minute < 10) {
                    minute_string = "0" + String.valueOf(minute);
                }

                set_alarm_text("Alarm set to: " + hour_string + ":" + minute_string);

                intent.putExtra("extra", "alarm on");

                pending_intent = PendingIntent.getBroadcast(AlarmActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                alarm_manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pending_intent);

                Intent intent_back = new Intent(AlarmActivity.this, CordovaApp.class);
                intent_back.putExtra("extra_hour", hour_string);
                intent_back.putExtra("extra_minute", minute_string);
                intent_back.putExtra("extra_date", currentDate);
                startActivity(intent_back);

            }
        });

        Button alarm_off = (Button) findViewById(R.id.alarm_off);
        alarm_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set_alarm_text("Alarm off");

                alarm_manager.cancel(pending_intent);

                intent.putExtra("extra", "alarm off");

                sendBroadcast(intent);
            }
        });


    }

    private void set_alarm_text(String s) {
        update_text.setText(s);
    }
}

