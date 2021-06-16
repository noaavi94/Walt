package com.walt.model;

public class DriverOrdersCount {
	private Driver driver;
	private long count;

	public DriverOrdersCount(Driver driver, long count) {
		this.driver = driver;
		this.count = count;
	}

	public Driver getDriver() {
		return driver;
	}

	public void setDriver(Driver driver) {
		this.driver = driver;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

}
