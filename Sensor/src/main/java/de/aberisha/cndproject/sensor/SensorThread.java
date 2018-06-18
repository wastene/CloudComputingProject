package de.aberisha.cndproject.sensor;

import java.util.Random;

public class SensorThread extends Thread{
	
	private Sensor sensor;
	
	private boolean run = true;
	
	private int bounds = 100;
	
	public SensorThread(Sensor sensor) {
		this.sensor = sensor;
	}
	
	public SensorThread(Sensor sensor, int bounds) {
		this.sensor = sensor;
		this.bounds = bounds;
	}
	
	@Override
	public void run() {
		super.run();
		
		Random r = new Random();
		
		while(run) {
			sensor.setValue(r.nextInt(bounds));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void stopThread() {
		run = false;
	}
}
