package de.aberisha.cndproject.sensor;

public class Sensor {

	private String name;
	
	private double value = 0.0;
	
	private String unit = "";
	
	public Sensor(final String name, final String unit) {
		this.name = name;
		this.unit = unit;
	}
	
	public Sensor(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}
}
