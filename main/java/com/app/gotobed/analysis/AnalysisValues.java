package com.app.gotobed.analysis;

public class AnalysisValues {

	private String recovery="";
	private String relaxation="";
	private String sleepTime="";
	private String inBedTime="";
	private String scaling="";
		
	public void consume(String line) {
		String[] split=line.split("\t");
		
		if(split==null || split.length!=2) {
			return;
		}
				
		if("Recovery".equals(split[0])) {
			recovery=split[1].trim();
		}

		if("Relaxation".equals(split[0])) {
			relaxation=split[1].trim();
		}

		if("Sleep Time".equals(split[0])) {
			sleepTime=split[1].trim();
		}

		if("In Bed Time".equals(split[0])) {
			inBedTime=split[1].trim();
		}

		if("HR scaling".equals(split[0])) {
			scaling=split[1].trim();
		}

	}

	public String getRecovery() {
		return recovery;
	}

	public String getRelaxation() {
		return relaxation;
	}

	public String getSleepTime() {
		return sleepTime;
	}

	public String getInBedTime() {
		return inBedTime;
	}

	public String getScaling() {
		return scaling;
	}

}
