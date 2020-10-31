package com.app.gotobed.sensor.reader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.Vector;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.app.gotobed.CordovaApp;
import com.app.gotobed.sensor.alarm.AlarmActivity;
import com.app.gotobed.sensor.alarm.AlarmReceiver;
import com.app.gotobed.sensor.alarm.RingtonePlayingService;
import com.app.gotobed.sensor.graphanalysis.GraphSleepAnalysisActivity;
import com.app.gotobed.sensor.storage.StorageJsInterface;
import com.app.gotobed.R;


import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.classifiers.functions.Logistic;
import weka.classifiers.lazy.IBk;
import weka.classifiers.rules.PART;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;
import xdroid.toaster.Toaster;

import static android.content.Context.MODE_PRIVATE;

public class ReaderThread extends Thread {

	private Context ctx;
	private File contextFilesDir;
	private String host;
	private int port;

	private Socket socket;
	private BufferedReader br;


	private List<Double> last_RRV = new ArrayList<Double>();


	private int consecutiveEmptyLineCount = 0;


	private List<Double> last_HR = new ArrayList<>();

	private List<Integer> old_HR = new ArrayList<Integer>();
	private List<Integer> old_RR = new ArrayList<Integer>();
	private List<Integer> old_SV = new ArrayList<Integer>();
	private List<Integer> old_HRV = new ArrayList<Integer>();
	private List<Integer> old_B2B = new ArrayList<Integer>();

	private List<Integer> old_HR2 = new ArrayList<Integer>();
	private List<Integer> old_RR2 = new ArrayList<Integer>();
	private List<Integer> old_SV2 = new ArrayList<Integer>();
	private List<Integer> old_HRV2 = new ArrayList<Integer>();
	private List<Integer> old_B2B2 = new ArrayList<Integer>();

	private List<Integer> old_HR3 = new ArrayList<Integer>();
	private List<Integer> old_RR3 = new ArrayList<Integer>();
	private List<Integer> old_SV3 = new ArrayList<Integer>();
	private List<Integer> old_HRV3 = new ArrayList<Integer>();
	private List<Integer> old_B2B3 = new ArrayList<Integer>();

	private List<Integer> old_HR4 = new ArrayList<Integer>();
	private List<Integer> old_RR4 = new ArrayList<Integer>();
	private List<Integer> old_SV4 = new ArrayList<Integer>();
	private List<Integer> old_HRV4 = new ArrayList<Integer>();
	private List<Integer> old_B2B4 = new ArrayList<Integer>();

	private List<Integer> old_HR5 = new ArrayList<Integer>();
	private List<Integer> old_RR5 = new ArrayList<Integer>();
	private List<Integer> old_SV5 = new ArrayList<Integer>();
	private List<Integer> old_HRV5 = new ArrayList<Integer>();
	private List<Integer> old_B2B5 = new ArrayList<Integer>();

	private List<Integer> old_HR6 = new ArrayList<Integer>();
	private List<Integer> old_RR6 = new ArrayList<Integer>();
	private List<Integer> old_SV6 = new ArrayList<Integer>();
	private List<Integer> old_HRV6 = new ArrayList<Integer>();
	private List<Integer> old_B2B6 = new ArrayList<Integer>();

	private List<Integer> old_HR7 = new ArrayList<Integer>();
	private List<Integer> old_RR7 = new ArrayList<Integer>();
	private List<Integer> old_SV7 = new ArrayList<Integer>();
	private List<Integer> old_HRV7 = new ArrayList<Integer>();
	private List<Integer> old_B2B7 = new ArrayList<Integer>();

	private List<Double> last_new_LFHRV = new ArrayList<>();

	private List<Double> last_HFHRV = new ArrayList<>();

	private List<Double> last_HF_LFHRV = new ArrayList<>();

	private List<Double> last_d_HF_LFHRV_dt = new ArrayList<>();

	private List<Double> last_SVFILT = new ArrayList<>();

	private List<Double> last_SVV = new ArrayList<>();

	private List<Double> last_RRFILT = new ArrayList<>();

	private List<Double> last_SSFILT = new ArrayList<>();

	private List<Integer> last_SS = new ArrayList<>();

    private List<Double> list_HRFILT = new ArrayList<Double>();
    private List<Double> list_HF_LFHRV = new ArrayList<Double>();
    private List<Double> list_d_HF_LFHRV_dt = new ArrayList<Double>();
    private List<Double> list_SVV = new ArrayList<Double>();
    private List<Double> list_RRV = new ArrayList<Double>();
    private List<Double> list_SSFILT = new ArrayList<Double>();


	private int count = -1;
	private int final_result = 0;


	public ReaderThread(Context ctx, File contextFilesDir, String host, int port) {
		this.ctx = ctx;
		this.contextFilesDir = contextFilesDir;
		this.host = host;
		this.port = port;

		this.consecutiveEmptyLineCount = 0;

	}

	public void closeEverything() {

		NotificationManager mNotificationManager =
				(NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(CordovaApp.STATUSBAR_ICON_NOTIFICATION_ID);

		try {
			if (socket != null && !socket.isInputShutdown()
					&& socket.isConnected()) {
				socket.shutdownInput();
			}
		} catch (IOException ioe) {
			Log.wtf(CordovaApp.BCG, "Problems closing socket input at " + host,
					ioe);
		}

		try {
			if (socket != null && !socket.isOutputShutdown()
					&& socket.isConnected()) {
				socket.shutdownOutput();
			}
		} catch (IOException ioe) {
			Log.wtf(CordovaApp.BCG,
					"Problems closing socket output at " + host, ioe);
		}

		try {
			if (socket != null && !socket.isClosed()) {
				socket.close();
			}
		} catch (IOException ioe) {
			Log.wtf(CordovaApp.BCG, "Problems closing socket at " + host, ioe);
		}

		Log.v(CordovaApp.BCG, "Sensor socket closed at " + host);
	}

	private void reconnect() {

		closeEverything();

		try {
			System.out.println("Reconnecting sensor socket at " + host);
			socket = new Socket();
			socket.connect(new InetSocketAddress(host, port), 100);
			socket.setSoTimeout(7000);
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();
			os.write("GET / HTTP/1.0\r\nuser-agent: BGC_App\r\nConnection: keep-alive\r\n\r\n"
					.getBytes());
			br = new BufferedReader(new InputStreamReader(is));
		} catch (IOException ioe) {
			Log.wtf(CordovaApp.BCG, ioe);
		}
	}

	private void reconnectMessage(String str) {
		Toaster.toast(str);
	}

	@Override
	public void run() {

		reconnect();

		StorageJsInterface historyStorage = new StorageJsInterface(contextFilesDir);

		try {

			String statusLine = null;
			while (!Thread.currentThread().isInterrupted()) {

				if(br != null) {
					try {
						statusLine = br.readLine();

						// add line to history with a "global" timestamp if the
						// status is somewhat valid
						if (statusLine != null && statusLine.indexOf(',') != -1) {
							String statusLineWithTimestamp = System.currentTimeMillis() + statusLine.substring(statusLine.indexOf(","));
							historyStorage.appendToHistory(statusLineWithTimestamp);

							String[] tokens = statusLine.split(",");
							int timestamp = Integer.parseInt(tokens[0]);
							int hr = Integer.parseInt(tokens[1]);
							int rr = Integer.parseInt(tokens[2]);
							int sv = Integer.parseInt(tokens[3]);
							int hrv = Integer.parseInt(tokens[4]);
							int ss = Integer.parseInt(tokens[5]);
							int status = Integer.parseInt(tokens[6]);
							int b2b = Integer.parseInt(tokens[7]);
							int b2b_1 = Integer.parseInt(tokens[8]);
							int b2b_2 = Integer.parseInt(tokens[9]);
							onStart(timestamp, hr, rr, sv, hrv, ss, status, b2b, b2b_1, b2b_2);


						}
					} catch (IOException ioe) {
						statusLine = null;
					}
					Log.v(CordovaApp.BCG, "Received [" + statusLine
							+ "] from sensor at " + host);
				}

				if (statusLine != null) {
					setNotification();
					if(consecutiveEmptyLineCount > 0) {
						reconnectMessage("MurataBCG: Reconnected to " + this.host);
					}
					consecutiveEmptyLineCount = 0;
				} else {
					if (!Thread.currentThread().isInterrupted()) {
						consecutiveEmptyLineCount++;
						if (consecutiveEmptyLineCount > 2) {
							Log.v(CordovaApp.BCG, "Sensor connection problems, reconnecting...");
							NotificationManager mNotificationManager =
									(NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

							//Cancel notification
							mNotificationManager.cancel(CordovaApp.STATUSBAR_ICON_NOTIFICATION_ID);
							reconnect();
						}
						Thread.sleep(300);
					}
				}
//				MemoryStatsLogger.logMemory();
			}
		} catch (Exception e) {
			Log.wtf(CordovaApp.BCG, e);
		} finally {
			closeEverything();
		}
		Log.v(CordovaApp.BCG, " -------- BCG SENSOR POLLER INTERRUPTED --------- ");

	}

	private void setNotification() {
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(ctx)
						.setSmallIcon(R.drawable.notification_icon)
						.setContentTitle("MurataBCG")
						.setContentText("Running");
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(ctx, CordovaApp.class);

		mBuilder.setContentIntent(PendingIntent.getActivity(ctx, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT));

		NotificationManager mNotificationManager =
				(NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(CordovaApp.STATUSBAR_ICON_NOTIFICATION_ID, mBuilder.build());
	}

	protected void onStart(int timestamp, int value_hr, int value_rr, int value_sv, int value_hrv, int value_ss, int value_status, int value_b2b, int value_b2b_1, int value_b2b_2) throws Exception{

        double HRFILT = calc_HRFILT(value_hr, value_rr, value_sv, value_hrv, value_b2b);

        double new_LFHRV = calc_new_LFHRV(value_hr, value_rr, value_sv, value_hrv, value_b2b);

        double HFHRV = calc_HFHRV(value_hr, value_rr, value_sv, value_hrv, value_b2b);

        double SVFILT = calc_SVFILT(value_hr, value_rr, value_sv, value_hrv, value_b2b);

        double SVV = calc_SVV(value_hr, value_rr, value_sv, value_hrv, value_b2b);

        double HF_LFHRV = calc_HRVFILT();

        double RRFILT = calc_RRFILT(value_hr, value_rr, value_sv, value_hrv, value_b2b);

        double RRV = calc_RRV(value_hr, value_rr, value_sv, value_hrv, value_b2b);

        double SSFILT = calc_SSFILT(value_ss);

        double d_HF_LFHRV_dt = calc_dHRVFILT();




		count++;
        if (count % 30 == 0) {
            System.out.println("30s by 30s ->>  " + count);

            list_HRFILT.add(HRFILT);
            list_HF_LFHRV.add(HF_LFHRV);
            list_d_HF_LFHRV_dt.add(d_HF_LFHRV_dt);
            list_SVV.add(SVV);
            list_RRV.add(RRV);
            list_SSFILT.add(SSFILT);

            final_result = calc_sleep(count, HRFILT, HF_LFHRV, d_HF_LFHRV_dt, SVV, RRV, SSFILT);

			System.out.println("BERNARDO -> "+count+":"+final_result);

            //Put sleep stage in a graph view
			Intent intent2 = new Intent(ctx, GraphSleepAnalysisActivity.class);

			intent2.putExtra("time",count);
			intent2.putExtra("stage",final_result);

			ctx.startService(intent2);

			//Set Music Alarm
			Intent intent = new Intent(ctx, RingtonePlayingService.class);

			intent.putExtra("sleepStage", final_result);

			ctx.startService(intent);

        }


	}

	double calc_HRFILT(int hr, int rr, int sv, int hrv, int b2b) {
		double result = 0;

		//starts with value = 60
		if (last_HR.isEmpty()) {
			double first_value = 60;
			last_HR.add(first_value);
			old_HR.add(hr);
			old_RR.add(rr);
			old_SV.add(sv);
			old_HRV.add(hrv);
			old_B2B.add(b2b);
			result = first_value;
			return result;
		} else {
			System.out.println("BERNAS -->> old hr  " + hr);
			//y(t) = (1−k)∗ y(t−1) + k ∗ x(t) -> lp, low pass filter
			if (hr > 0 && (hr != old_HR.get(old_HR.size() - 1) || rr != old_RR.get(old_RR.size() - 1) ||
					sv != old_SV.get(old_SV.size() - 1) || hrv != old_HRV.get(old_HRV.size() - 1) ||
					b2b != old_B2B.get(old_B2B.size() - 1))) {
				result = (double) ((1 - ((double) 1 / 256)) * last_HR.get(last_HR.size() - 1) + (double) hr / 256);
			} else {
				result = last_HR.get(last_HR.size() - 1);
			}
			last_HR.add(result);
			System.out.println("BERNAS -->> HRFILT  " + last_HR);
		}
		old_HR.add(hr);
		old_RR.add(rr);
		old_SV.add(sv);
		old_HRV.add(hrv);
		old_B2B.add(b2b);

		return result;
	}

	double calc_new_LFHRV(int hr, int rr, int sv, int hrv, int b2b) {
		double result = 0;

		//starts with value = 2.5
		if (last_new_LFHRV.isEmpty()) {
			double first_value = 2.5;
			last_new_LFHRV.add(first_value);
			old_HR2.add(hr);
			old_RR2.add(rr);
			old_SV2.add(sv);
			old_HRV2.add(hrv);
			old_B2B2.add(b2b);
			return first_value;
		}

		//y(t) = (1−k)∗ y(t−1) + k ∗ x(t) -> lp, low pass filter
		if(hr > 0 && (rr != old_RR2.get(old_RR2.size() - 1) || sv != old_SV2.get(old_SV2.size() - 1) ||
				hrv != old_HRV2.get(old_HRV2.size() - 1) || b2b != old_B2B2.get(old_B2B2.size() - 1))) {
			result = (double) ((1 - ((double) 1/1024)) * last_new_LFHRV.get(last_new_LFHRV.size() - 1) + (double) Math.abs(last_HR.get(last_HR.size() - 1) - hr)/1024);
		} else {
			result = last_new_LFHRV.get(last_new_LFHRV.size() - 1);
		}

		last_new_LFHRV.add(result);
		System.out.println("BERNAS -->> new_LFHRV  " + last_new_LFHRV.get(last_new_LFHRV.size() - 1));

		old_HR2.add(hr);
		old_RR2.add(rr);
		old_SV2.add(sv);
		old_HRV2.add(hrv);
		old_B2B2.add(b2b);

		return result;
	}

	double calc_HFHRV(int hr, int rr, int sv, int hrv, int b2b) {
		double result = 0;

		//starts with value = 20
		if (last_HFHRV.isEmpty()) {
			double first_value = 20;
			last_HFHRV.add(first_value);
			old_HR3.add(hr);
			old_RR3.add(rr);
			old_SV3.add(sv);
			old_HRV3.add(hrv);
			old_B2B3.add(b2b);
			return first_value;
		}

		//y(t) = (1−k)∗ y(t−1) + k ∗ x(t) -> lp, low pass filter
		if(hrv > 0 && hrv < 150 && (hr != old_HR3.get(old_HR3.size() - 1) || rr != old_RR3.get(old_RR3.size() - 1) ||
				sv != old_SV3.get(old_SV3.size() - 1) || hrv != old_HRV3.get(old_HRV3.size() - 1) ||
				b2b != old_B2B3.get(old_B2B3.size() - 1))) {
			result = (double) ((1 - ((double) 1/1024)) * last_HFHRV.get(last_HFHRV.size() - 1) + (double) hrv/1024);
		} else {
			result = last_HFHRV.get(last_HFHRV.size() - 1);
		}

		last_HFHRV.add(result);
		System.out.println("BERNAS -->> HRVFILT  " + last_HFHRV);

		old_HR3.add(hr);
		old_RR3.add(rr);
		old_SV3.add(sv);
		old_HRV3.add(hrv);
		old_B2B3.add(b2b);

		return result;
	}

	double calc_SVFILT(int hr, int rr, int sv, int hrv, int b2b) {
		double result = 0;

		//starts with value = 50
		if (last_SVFILT.isEmpty()) {
			double first_value = 50;
			last_SVFILT.add(first_value);
			old_HR4.add(hr);
			old_RR4.add(rr);
			old_SV4.add(sv);
			old_HRV4.add(hrv);
			old_B2B4.add(b2b);
			return first_value;
		}

		//y(t) = (1−k)∗ y(t−1) + k ∗ x(t) -> lp, low pass filter
		if(sv > 0 && sv < 100 && (hr != old_HR4.get(old_HR4.size() - 1) || rr != old_RR4.get(old_RR4.size() - 1) ||
				sv != old_SV4.get(old_SV4.size() - 1) || hrv != old_HRV4.get(old_HRV4.size() - 1) ||
				b2b != old_B2B4.get(old_B2B4.size() - 1))) {
			result = (double) ((1 - ((double) 1/256)) * last_SVFILT.get(last_SVFILT.size() - 1) + (double) sv/256);
		} else {
			result = last_SVFILT.get(last_SVFILT.size() - 1);
		}

		last_SVFILT.add(result);
		System.out.println("BERNAS -->> SVFILT  " + last_SVFILT);

		old_HR4.add(hr);
		old_RR4.add(rr);
		old_SV4.add(sv);
		old_HRV4.add(hrv);
		old_B2B4.add(b2b);

		return result;
	}

	double calc_SVV(int hr, int rr, int sv, int hrv, int b2b) {
		double result = 0;

		//starts with value = 0.1
		if (last_SVV.isEmpty()) {
			double first_value = 0.1;
			last_SVV.add(first_value);
			old_HR5.add(hr);
			old_RR5.add(rr);
			old_SV5.add(sv);
			old_HRV5.add(hrv);
			old_B2B5.add(b2b);
			return first_value;
		}

		//y(t) = (1−k)∗ y(t−1) + k ∗ x(t) -> lp, low pass filter
		if(sv > 0 && sv < 100 && (hr != old_HR5.get(old_HR5.size() - 1) || rr != old_RR5.get(old_RR5.size() - 1) ||
				sv != old_SV5.get(old_SV5.size() - 1) || hrv != old_HRV5.get(old_HRV5.size() - 1) ||
				b2b != old_B2B5.get(old_B2B5.size() - 1))) {
			result = (double) ((1 - ((double) 1/1024)) * last_SVV.get(last_SVV.size() - 1) + (double) Math.abs(sv - last_SVFILT.get(last_SVFILT.size() - 1))/last_SVFILT.get(last_SVFILT.size() - 1)/1024);
		} else {
			result = last_SVV.get(last_SVV.size() - 1);
		}

		last_SVV.add(result);
		System.out.println("BERNAS -->> SVV  " + last_SVV);

		old_HR5.add(hr);
		old_RR5.add(rr);
		old_SV5.add(sv);
		old_HRV5.add(hrv);
		old_B2B5.add(b2b);

		return result;
	}

	double calc_RRFILT(int hr, int rr, int sv, int hrv, int b2b) {
		double result = 0;

		//starts with value = 15
		if (last_RRFILT.isEmpty()) {
			double first_value = 15;
			last_RRFILT.add(first_value);
			old_HR6.add(hr);
			old_RR6.add(rr);
			old_SV6.add(sv);
			old_HRV6.add(hrv);
			old_B2B6.add(b2b);
			return first_value;
		}

		//y(t) = (1−k)∗ y(t−1) + k ∗ x(t) -> lp, low pass filter
		if(rr > 0 && (hr != old_HR6.get(old_HR6.size() - 1) || rr != old_RR6.get(old_RR6.size() - 1) ||
				sv != old_SV6.get(old_SV6.size() - 1) || hrv != old_HRV6.get(old_HRV6.size() - 1) ||
				b2b != old_B2B6.get(old_B2B6.size() - 1))) {
			result = (double) ((1 - ((double) 1/1024)) * last_RRFILT.get(last_RRFILT.size() - 1) + (double) rr/1024);
		} else {
			result = last_RRFILT.get(last_RRFILT.size() - 1);
		}

		last_RRFILT.add(result);
		System.out.println("BERNAS -->> RRFILT  " + last_RRFILT);

		old_HR6.add(hr);
		old_RR6.add(rr);
		old_SV6.add(sv);
		old_HRV6.add(hrv);
		old_B2B6.add(b2b);

		return result;
	}

	double calc_RRV(int hr, int rr, int sv, int hrv, int b2b) {
		double result = 0;

		//starts with value = 2.5
		if (last_RRV.isEmpty()) {
			double first_value = 2.5;
			last_RRV.add(first_value);
			old_HR7.add(hr);
			old_RR7.add(rr);
			old_SV7.add(sv);
			old_HRV7.add(hrv);
			old_B2B7.add(b2b);
			return first_value;
		}

		//y(t) = (1−k)∗ y(t−1) + k ∗ x(t) -> lp, low pass filter
		if(rr > 0 && (hr != old_HR7.get(old_HR7.size() - 1) || rr != old_RR7.get(old_RR7.size() - 1) ||
				sv != old_SV7.get(old_SV7.size() - 1) || hrv != old_HRV7.get(old_HRV7.size() - 1) ||
				b2b != old_B2B7.get(old_B2B7.size() - 1))) {
			result = (double) ((1 - ((double) 1/1024)) * last_RRV.get(last_RRV.size() - 1) + (double) Math.abs(rr - last_RRFILT.get(last_RRFILT.size() - 1))/1024);
		} else {
			result = last_RRV.get(last_RRV.size() - 1);
		}

		last_RRV.add(result);
		System.out.println("BERNAS -->> RRV  " + last_RRV);

		old_HR7.add(hr);
		old_RR7.add(rr);
		old_SV7.add(sv);
		old_HRV7.add(hrv);
		old_B2B7.add(b2b);

		return result;
	}

	double calc_SSFILT(int ss) {
		double result = 0;

		//starts with value = 600
		if (last_SSFILT.isEmpty()) {
			double first_value = 600;
			last_SSFILT.add(first_value);
			last_SS.add(ss);
			return first_value;
		}

		//y(t) = (1−k)∗ y(t−1) + k ∗ x(t) -> lp, low pass filter
		result = (double) ((1 - ((double) 1/1024)) * last_SSFILT.get(last_SSFILT.size() - 1) + (double) ss/1024);

		last_SSFILT.add(result);
		System.out.println("BERNAS -->> SSFILT  " + last_SSFILT);

		last_SS.add(ss);

		return result;
	}

	double calc_HRVFILT() {
		double result = 0;

		result = (double) (last_HFHRV.get(last_HFHRV.size() - 1) / last_new_LFHRV.get(last_new_LFHRV.size() - 1) / last_SVV.get(last_SVV.size() - 1) / 10);

		last_HF_LFHRV.add(result);
		System.out.println("BERNAS -->> HRVFILT  " + last_HF_LFHRV);

		return result;
	}

	double calc_dHRVFILT() {
		double result = 0;

		//starts with value = 0
		if (last_d_HF_LFHRV_dt.isEmpty()) {
			double first_value = 0;
			last_d_HF_LFHRV_dt.add(first_value);
			return first_value;
		}

		result = (double) ((1 - ((double) 1/1024)) * last_d_HF_LFHRV_dt.get(last_d_HF_LFHRV_dt.size() - 1) + (last_HF_LFHRV.get(last_HF_LFHRV.size() - 1) - last_HF_LFHRV.get(last_HF_LFHRV.size() - 2))/1024);

		last_d_HF_LFHRV_dt.add(result);
		System.out.println("BERNAS -->> dHRVFILT  " + last_d_HF_LFHRV_dt);

		return result;
	}

	//last stage
	int calc_sleep(int time, double HRFILT, double HRVFILT, double dHRV, double SVV, double RRV, double SSFILT) throws Exception {
		FastVector atts;
		Instances data;
		double[] vals;

		// 1. set up attributes
		atts = new FastVector();
		// - numeric
		atts.addElement(new Attribute("time"));
		atts.add(new Attribute("hrfilt"));
		atts.add(new Attribute("hrvfilt"));
		atts.add(new Attribute("dhrv"));
		atts.add(new Attribute("svv"));
		atts.add(new Attribute("rrv"));
		atts.add(new Attribute("ssfilt"));
		atts.add(new Attribute("sleepRef", "?"));


		// 2. create Instances object
		data = new Instances("sleepStageFilter", atts, 0);

		// 3. fill with data
		// first instance
		vals = new double[data.numAttributes()];
		// - numeric
		vals[0] = time;
		vals[1] = HRFILT;
		vals[2] = HRVFILT;
		vals[3] = dHRV;
		vals[4] = SVV;
		vals[5] = RRV;
		vals[6] = SSFILT;
		// add
		data.add(new DenseInstance(1.0, vals));



		data.setClassIndex(data.numAttributes()-1);
		// 4. output data
		System.out.println(data);



		System.out.println("ActualClass \t ActualValue \t PredictedValue \t PredictedClass");

		String act = data.instance(0).stringValue(data.instance(0).numAttributes() - 1);

		double actual = data.instance(0).classValue();

		Instance inst = data.instance(0);

		InputStream is = ctx.getAssets().open("randomForest.model");
		ObjectInputStream ois = new ObjectInputStream(is);
		Classifier cls = (Classifier) ois.readObject();
		ois.close();

		double predict = cls.classifyInstance(inst);
		String pred = inst.toString(inst.numAttributes() - 1);
		System.out.println(act + " \t\t " + actual + " \t\t " + predict + " \t\t " + pred);

		int sleepStage = (int) predict;


		return sleepStage;
	}



}
