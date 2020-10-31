package com.app.gotobed.sensor.discovery;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.app.gotobed.CordovaApp;

public class DiscovererThread extends Thread {

	private static final String BROADCAST_ADDRESS="255.255.255.255";
	private static final int BROADCAST_PORT=2000;
	private static final String BROADCAST_PAYLOAD="{\"type\":\"SCS-DISCOVER\",\"hostname\":\"Host-SCS\"}";
	private static final int BROADCAST_REPLY_WAIT_IN_MILLISECONDS=1000*5;
	private static final int BROADCAST_SOCKET_TIMEOUT_IN_MILLISECONDS=BROADCAST_REPLY_WAIT_IN_MILLISECONDS*2;
	
	private List<DiscoveredSensor> discoveredSensors;
	
	public DiscovererThread() {
		discoveredSensors=new ArrayList<DiscoveredSensor>();
	}

	public List<DiscoveredSensor> getDiscoveredSensorsAndInterrupt() {
		if (!Thread.currentThread().isInterrupted()) {
			Thread.currentThread().interrupt();
		}

		return discoveredSensors;
	}

	public void run() {

		try {
			Log.v(CordovaApp.BCG, "Discovery - start");
			DatagramSocket senderSocket = new DatagramSocket(BROADCAST_PORT);
			senderSocket.setBroadcast(true);
			senderSocket.setSoTimeout(3000);
			senderSocket.setReuseAddress(true);
			senderSocket.connect(InetAddress.getByName(BROADCAST_ADDRESS), BROADCAST_PORT);
			
			DatagramPacket packet=new DatagramPacket(BROADCAST_PAYLOAD.getBytes(), BROADCAST_PAYLOAD.length());
			senderSocket.send(packet);
			senderSocket.close();
			
			long now = System.currentTimeMillis();			
			DatagramSocket receiverSocket=new DatagramSocket(BROADCAST_PORT);
			receiverSocket.setSoTimeout(BROADCAST_SOCKET_TIMEOUT_IN_MILLISECONDS);

			do {
				try {
					byte[] receiveData = new byte[1024];
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					receiverSocket.receive(receivePacket);
					DiscoveredSensor discoveredSensor=new DiscoveredSensor(receivePacket.getData());
					if(discoveredSensor.isValid()) {
						discoveredSensors.add(discoveredSensor);
						Log.v(CordovaApp.BCG, "Discovery - found sensor at "+discoveredSensor.getIp());
					} else {
						Log.v(CordovaApp.BCG, "Discovery - found something that is not a sensor");
					}
				} catch (SocketTimeoutException ste) {
					// intentionally left empty
				}
								
			} while((System.currentTimeMillis()-now)<BROADCAST_REPLY_WAIT_IN_MILLISECONDS);

			Log.v(CordovaApp.BCG, "Discovery - not waiting any longer");

			receiverSocket.disconnect();
			receiverSocket.close();

		} catch(Exception e) {
			Log.wtf(CordovaApp.BCG, "Exception while discovery was running!", e);
		}		

	}

}
