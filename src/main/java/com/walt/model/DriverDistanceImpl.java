package com.walt.model;

public class DriverDistanceImpl implements DriverDistance {

	private Driver driver;
	private double totalDistance;

	public DriverDistanceImpl(Driver driver, Double totalDistance) {
		this.driver = driver;
		if (totalDistance == null)
			this.totalDistance = 0;
		else
			this.totalDistance = totalDistance.doubleValue();
	}

	@Override
	public Driver getDriver() {
		return driver;
	}

	@Override
	public Long getTotalDistance() {
		return new Long((long) totalDistance);
	}

}
