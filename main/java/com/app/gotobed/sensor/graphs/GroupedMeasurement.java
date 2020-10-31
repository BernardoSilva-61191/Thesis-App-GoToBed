package com.app.gotobed.sensor.graphs;

public class GroupedMeasurement {

	private long startTime=0;
	private long endTime=0;
	
	private float averageStatus=0;
	private float averageHr=0;
	private float averageHrv=0;
	private float averageSv=0;
	private float averageRr=0;

	private float measurements=0;
	private float status1Measurements=0;
	
	public GroupedMeasurement(long startTime, long groupWidth) {
		this.startTime=startTime;
		this.endTime=startTime+groupWidth;
	}
	
	public boolean isCorrectForThisMeasurement(Measurement measurement) {
		return (
			measurement.getTimestamp()>=startTime &&
			measurement.getTimestamp()<=endTime
		);
	}

	public boolean isContainingMeasurements(){
		return measurements>0;
	}
			
	public void addMeasurement(Measurement measurement) {
		averageStatus+=measurement.getStatus();
		
		if(measurement.getStatus()==1 && measurement.getHr()>0.01f) {
			averageHr+=measurement.getHr();
			averageHrv+=measurement.getHrv();
			averageSv+=measurement.getSv();
			averageRr+=measurement.getRr();
			status1Measurements++;
		}

		measurements++;		
	}

	public long getStartTime() {
		return startTime;
	}
	
	public float getAverageStatus(){		
		// -1 status => no measurements
		return measurements==0?-1:(averageStatus/measurements);
	}

	public float getAverageHr(){
		return averageHr/(status1Measurements==0?1:status1Measurements);
	}
	
	public float getAverageHrv(){
		return averageHrv/(status1Measurements==0?1:status1Measurements);
	}
	
	public float getAverageSv(){
		return averageSv/(status1Measurements==0?1:status1Measurements);
	}

	public float getAverageRr(){
		return averageRr/(status1Measurements==0?1:status1Measurements);
	}

}
