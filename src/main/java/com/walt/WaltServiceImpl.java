package com.walt;

import com.walt.dao.CustomerRepository;
import com.walt.dao.DeliveryRepository;
import com.walt.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
public class WaltServiceImpl implements WaltService {

	private Random r = new Random();

	@Autowired
	private DeliveryRepository deliveryRepository;

	@Autowired
	private CustomerRepository customerRepository;

	private boolean isSameCity(Customer customer, Restaurant restaurant) {
		return customer.getCity().getId() == restaurant.getCity().getId();
	}

	@Override
	public Delivery createOrderAndAssignDriver(Customer customer, Restaurant restaurant, Date deliveryTime) {
		if (!isSameCity(customer, restaurant)) {
			System.out.println("Please Choose a restaurant in " + customer.getCity().getName());
			return null;
		}

		customerRepository.save(customer);

		List<DriverOrdersCount> drivers = deliveryRepository.findAvailableDrivers(customer.getCity(), deliveryTime);

		if (drivers.isEmpty()) {
			System.out.println("No Available drivers");
			return null;
		}

		Driver driver = drivers.get(0).getDriver();
		Delivery delivery = new Delivery(driver, restaurant, customer, deliveryTime);

		double distance = 20 * this.r.nextDouble();
		delivery.setDistance(distance);

		deliveryRepository.save(delivery);
		return delivery;
	}

	@Override
	public List<DriverDistance> getDriverRankReport() {
		return deliveryRepository.findAllDriverDistance();
	}

	@Override
	public List<DriverDistance> getDriverRankReportByCity(City city) {
		return deliveryRepository.findAllDriverDistanceByCity(city);
	}

}
