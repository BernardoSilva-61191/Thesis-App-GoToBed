package com.app.gotobed.sensor.alarm;

/**
 * Created by Bernardo on 27/05/2018.
 */

public class Alarm {

    private String id;
    private String alarmTime;
    private String Date;

    public Alarm() {
    }

    public  Alarm(String id, String alarmTime, String Date) {
        this.id = id;
        this.alarmTime = alarmTime;
        this.Date = Date;
    }

    public String getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(String alarmTime) {
        this.alarmTime = alarmTime;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
