package com.app.gotobed.sensor.alarm;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


import com.app.gotobed.R;

/**
 * Created by Bernardo on 06/05/2018.
 */

public class RingtonePlayingService extends Service {

    MediaPlayer media_song;
    MediaPlayer  media_song_stage2;
    int startId;
    boolean isRunning;
    String sleepStage;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("LocalService", "received start id " + startId + ": " + intent);

        String state = intent.getExtras().getString("extra");

        Log.e("Ringtone state:extra is", " " + state);


        assert state != null;
        switch (state) {
            case "alarm on":
                startId = 1;
                break;
            case "alarm off":
                startId = 0;
                break;
            default:
                startId = 0;
                break;
        }

        if(!this.isRunning && startId == 1) {
            Log.e("there is no music, ", "and you want to start");

            //getSleepStage to display different song
            String sleepStage = intent.getExtras().getString("sleepStage");

            System.out.println("sleep -> " + sleepStage);
            media_song_stage2 = MediaPlayer.create(this, R.raw.sonsterra);

            if(sleepStage == "3") {

                media_song_stage2.start();
            } else {
                if(media_song_stage2.isPlaying())
                    media_song_stage2.stop();
                else {
                    try {
                        new Thread().sleep(15 *   // minutes to sleep
                                60 *   // seconds to a minute
                                1000); // milliseconds to a second


                        media_song = MediaPlayer.create(this, R.raw.iphone_6_original);
                        media_song.start();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }



            this.isRunning = true;
            this.startId = 0;

            NotificationManager notify_manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            Intent intent_alarmActivity = new Intent(this.getApplicationContext(), AlarmActivity.class);

            PendingIntent pending_alarmActivity = PendingIntent.getActivity(this, 0, intent_alarmActivity, 0);

            Notification not_popup = new Notification.Builder(this)
                    .setContentTitle("An alarm is going off")
                    .setContentText("Click me")
                    .setContentIntent(pending_alarmActivity)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setAutoCancel(true)
                    .build();

            notify_manager.notify(0, not_popup);

        } else if (this.isRunning && startId == 0) {
            Log.e("there is music, ", "and you want to end");

            media_song.stop();
            media_song.reset();

            this.isRunning = false;
            this.startId = 0;

        } else if (!this.isRunning && startId == 0) {
            Log.e("there is no music, ", "and you want to end");

            this.isRunning = false;
            this.startId = 0;
        } else if (this.isRunning && startId == 1) {
            Log.e("there is music, ", "and you want to start");

            this.isRunning = true;
            this.startId = 1;
        } else {
            Log.e("else ", "somehow you reached this");
        }



        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {

        Log.e("on Destroy called", " ");

        super.onDestroy();
        this.isRunning = false;
    }
}

