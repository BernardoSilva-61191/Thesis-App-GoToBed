package com.app.gotobed.sensor.storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.util.Log;
import android.webkit.JavascriptInterface;

import com.app.gotobed.CordovaApp;

public class StorageJsInterface {

	private File internalStorage;
	
	public StorageJsInterface(File contextFilesDir) {
		internalStorage = contextFilesDir;
	}

	public File getHistoryFile(boolean createIfMissing){
		File historyFile=new File(internalStorage, "bcg_data.txt");

		if(historyFile.exists() && historyFile.isFile()) {
			return historyFile;
		}
		
		if(!createIfMissing) {
			return null;
		}
		
		try {
			historyFile.createNewFile();
		} catch (IOException ioe) {
			Log.wtf(CordovaApp.BCG, ioe);
			return null;
		}
		
		return getHistoryFile(false);
	}	
	
	private String getFirstMeasurement() {
		File historyFile=getHistoryFile(false);
		if(historyFile==null || historyFile.length()==0) {
			return "";
		}
		
		try {
			BufferedReader br=new BufferedReader(new FileReader(historyFile));
			String result=br.readLine();
			br.close();
			return result;
		} catch (IOException ioe){
			Log.wtf(CordovaApp.BCG, ioe);
			return "";
		}
	}
	
	private List<String> getLastMeasurements(int wantedNumberOfLines) {

		long start=System.currentTimeMillis();
		
		ArrayList<String> results=new ArrayList<String>();
		java.io.RandomAccessFile raf = null;
	    try {
	    	File historyFile = getHistoryFile(false);
	    	if(historyFile==null) {
	    		return Collections.emptyList();
	    	}
	        raf = new java.io.RandomAccessFile( historyFile, "r" );
	        long fileLength = raf.length() - 1;
	        StringBuilder sb = new StringBuilder();

	        for(long filePointer = fileLength; filePointer != -1; filePointer--){
	            raf.seek(filePointer);
	            int readByte = raf.readByte();
	            sb.append((char)readByte);
	            
	            if(readByte == 10) {                
	            	String stringToAdd=sb.reverse().toString().trim();
	            	if(stringToAdd.length()>0) {
	            		results.add(stringToAdd);
	            	}

                	sb=new StringBuilder();
                	                	
	            	if(results.size()>wantedNumberOfLines-1) {
	            		break;
	            	}
	            }
	        }

	        System.out.println("(measurement tail in "+(System.currentTimeMillis()-start)+"ms)");
	        
	        return results;
	    } catch( java.io.FileNotFoundException fnfe ) {
			Log.wtf(CordovaApp.BCG, fnfe);
	        return null;
	    } catch( java.io.IOException ioe) {
			Log.wtf(CordovaApp.BCG, ioe);
	        return null;
	    }
	    finally {
	        if (raf != null )
	            try {
	                raf.close();
	            } catch (IOException e) {
	            	// intentionally left empty
	            }
	    }
	}
		
	private int parseHoursAgoFromMeasurement(String measurement){
		if(measurement==null || measurement.indexOf(',')==-1) {
			return -1;
		}

		return (int)(System.currentTimeMillis()-Long.parseLong(measurement.split(",")[0]))/60000;
	}
	
	@JavascriptInterface
	public String getDiskStatus() {
		long totalSpace=internalStorage.getTotalSpace()/1024/1024;
		long freeSpace=internalStorage.getFreeSpace()/1024/1024;

		long historySize=0;
		long historyMeasurements=0;

		File historyFile=getHistoryFile(false);
		if(historyFile!=null) {
			historySize=historyFile.length()/1024/1024;
			historyMeasurements=(long)Math.floor(historyFile.length()/34.d)+1;
		}
		
		String first=getFirstMeasurement();
		
		List<String> lastMeasurements = getLastMeasurements(5);
		String last=lastMeasurements.size()>0?lastMeasurements.get(0):null;
		
		return totalSpace+"|"+
			freeSpace+"|"+
			historySize+"|"+
			historyMeasurements+"|"+
			parseHoursAgoFromMeasurement(first)+"|"+
			parseHoursAgoFromMeasurement(last)+"|";		
	}

	@JavascriptInterface
	public boolean eraseHistory() {
		File historyFile=getHistoryFile(false);
		if(historyFile!=null) {
			return historyFile.delete();
		}
		return false;
	}

	@JavascriptInterface
	public boolean appendToHistory(String data) {
		File historyFile=getHistoryFile(true);
		
		try {
			PrintWriter out= new PrintWriter(new BufferedWriter(new FileWriter(historyFile, true)));
		    out.println(data);
		    out.close();
		    return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@JavascriptInterface
	public String getHistory(int count) {	
		StringBuilder sb=new StringBuilder();
		for(String measurement: getLastMeasurements(count)) {
			if(measurement.trim().length()>0) {
				sb.append(measurement);
				sb.append("\n");
			}			
		}
		return sb.toString();
	}

}
