package com.walt;

import com.walt.dao.*;
import com.walt.model.City;
import com.walt.model.Customer;
import com.walt.model.Delivery;
import com.walt.model.Driver;
import com.walt.model.DriverDistance;
import com.walt.model.Restaurant;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

import javax.annotation.Resource;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@SpringBootTest()
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaltTest {

	private List<Restaurant> restaurants;
	private List<Customer> customers;
	private List<Driver> drivers;

	@TestConfiguration
	static class WaltServiceImplTestContextConfiguration {

		@Bean
		public WaltService waltService() {
			return new WaltServiceImpl();
		}
	}

	@Autowired
	WaltService waltService;

	@Resource
	CityRepository cityRepository;

	@Resource
	CustomerRepository customerRepository;

	@Resource
	DriverRepository driverRepository;

	@Resource
	DeliveryRepository deliveryRepository;

	@Resource
	RestaurantRepository restaurantRepository;

	private final Date FixedDate = new Date();

	private Date getDateByHour(int hours) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(FixedDate);
		calendar.add(Calendar.HOUR_OF_DAY, hours);
		return calendar.getTime();
	}

	@BeforeEach()
	public void prepareData() {

		City jerusalem = new City("Jerusalem");
		City tlv = new City("Tel-Aviv");
		City bash = new City("Beer-Sheva");
		City haifa = new City("Haifa");

		cityRepository.save(jerusalem);
		cityRepository.save(tlv);
		cityRepository.save(bash);
		cityRepository.save(haifa);

		createDrivers(jerusalem, tlv, bash, haifa);

		createCustomers(jerusalem, tlv, haifa);

		createRestaurant(jerusalem, tlv);
	}

	private void createRestaurant(City jerusalem, City tlv) {
		Restaurant meat = new Restaurant("meat", jerusalem, "All meat restaurant");
		Restaurant vegan = new Restaurant("vegan", tlv, "Only vegan");
		Restaurant cafe = new Restaurant("cafe", tlv, "Coffee shop");
		Restaurant chinese = new Restaurant("chinese", tlv, "chinese restaurant");
		Restaurant mexican = new Restaurant("restaurant", tlv, "mexican restaurant ");

		this.restaurants = Lists.newArrayList(meat, vegan, cafe, chinese, mexican);
		restaurantRepository.saveAll(restaurants);
	}

	private void createCustomers(City jerusalem, City tlv, City haifa) {
		Customer beethoven = new Customer("Beethoven", tlv, "Ludwig van Beethoven");
		Customer mozart = new Customer("Mozart", jerusalem, "Wolfgang Amadeus Mozart");
		Customer chopin = new Customer("Chopin", haifa, "Frédéric François Chopin");
		Customer rachmaninoff = new Customer("Rachmaninoff", tlv, "Sergei Rachmaninoff");
		Customer bach = new Customer("Bach", tlv, "Sebastian Bach. Johann");

		this.customers = Lists.newArrayList(beethoven, mozart, chopin, rachmaninoff, bach);
		customerRepository.saveAll(this.customers);
	}

	private void createDrivers(City jerusalem, City tlv, City bash, City haifa) {
		Driver mary = new Driver("Mary", tlv);
		Driver patricia = new Driver("Patricia", tlv);
		Driver jennifer = new Driver("Jennifer", haifa);
		Driver james = new Driver("James", bash);
		Driver john = new Driver("John", bash);
		Driver robert = new Driver("Robert", jerusalem);
		Driver david = new Driver("David", jerusalem);
		Driver daniel = new Driver("Daniel", tlv);
		Driver noa = new Driver("Noa", haifa);
		Driver ofri = new Driver("Ofri", haifa);
		Driver nata = new Driver("Neta", jerusalem);

		this.drivers = Lists.newArrayList(mary, patricia, jennifer, james, john, robert, david, daniel, noa, ofri,
				nata);
		driverRepository.saveAll(drivers);
	}

	@Test
	public void testBasics() {
		assertEquals(((List<City>) cityRepository.findAll()).size(), 4);
		assertEquals((driverRepository.findAllDriversByCity(cityRepository.findByName("Beer-Sheva")).size()), 2);
	}

	@Test
	public void testCreateOrderAndAssignDriver_ShouldAddSingleDelivery() {
		Customer customer = customerRepository.findByName("Beethoven");
		Restaurant restaurant = restaurantRepository.findByName("vegan");

		Date deliveryTime = getDateByHour(1);

		// 3 Available Drivers in Tel Aviv
		assertEquals(3, deliveryRepository.findAvailableDrivers(customer.getCity(), deliveryTime).size());

		Delivery delivery = waltService.createOrderAndAssignDriver(customer, restaurant, getDateByHour(1));

		assertEquals(customer, delivery.getCustomer());
		assertEquals(((List<Delivery>) deliveryRepository.findAll()).size(), 1);
	}

	@Test
	public void testCreateOrderAndAssignDriver_NotTheSameCity_ShouldFail() {
		Customer customer = customerRepository.findByName("Beethoven");
		Restaurant restaurant = restaurantRepository.findByName("meat");

		Delivery delivery = waltService.createOrderAndAssignDriver(customer, restaurant, getDateByHour(1));
		
		assertNull(delivery);
		assertEquals(((List<Delivery>) deliveryRepository.findAll()).size(), 0);
	}

	@Test
	public void testCreateOrderAndAssignDriver_ShouldAddMultipleDeliveries() {
		Restaurant restaurant = restaurantRepository.findByName("cafe");
		int hour = 1;
		for (Customer customer : this.customers) {
			if (customer.getCity().getName().equals("Tel-Aviv")) {
				waltService.createOrderAndAssignDriver(customer, restaurant, getDateByHour(hour));
				hour++;
			}
		}

		List<Delivery> deliveries = (List<Delivery>) deliveryRepository.findAll();

		assertEquals(3, deliveries.size());
		assertArrayEquals(deliveries.stream().map(d -> d.getDriver().getName()).toArray(),
				new String[] { "Mary", "Patricia", "Daniel" });

	}

	@Test
	public void testCreateOrderAndAssignDriver_ShouldAssignDiffrentDrivers() {
		Restaurant restaurant = restaurantRepository.findByName("cafe");
		Customer bach = customerRepository.findByName("Bach");

		Delivery d1 = waltService.createOrderAndAssignDriver(bach, restaurant, getDateByHour(1));
		assertEquals("Mary", d1.getDriver().getName());

		Delivery d2 = waltService.createOrderAndAssignDriver(bach, restaurant, getDateByHour(2));
		assertEquals("Patricia", d2.getDriver().getName());

		Delivery d3 = waltService.createOrderAndAssignDriver(bach, restaurant, getDateByHour(2));
		assertEquals("Daniel", d3.getDriver().getName());

		Delivery d4 = waltService.createOrderAndAssignDriver(bach, restaurant, getDateByHour(3));
		assertEquals("Mary", d4.getDriver().getName());

		// Even tho Mary is the most busy- she is the only one available at hour 2
		Delivery d5 = waltService.createOrderAndAssignDriver(bach, restaurant, getDateByHour(2));
		assertEquals("Mary", d5.getDriver().getName());
	}

	@Test
	public void testCreateOrderAndAssignDriver_NoAvailableDrivers_ShouldFail() {
		Restaurant restaurant = restaurantRepository.findByName("meat");
		Customer customer = customerRepository.findByName("Mozart");

		int numOfDriversInJerusalem = this.drivers.stream().filter(d -> d.getCity().getName().equals("Jerusalem"))
				.collect(Collectors.toList()).size();
		while (numOfDriversInJerusalem-- > 0)
			waltService.createOrderAndAssignDriver(customer, restaurant, getDateByHour(1));

		assertNull(waltService.createOrderAndAssignDriver(customer, restaurant, getDateByHour(1)));
	}

	public void addRandomDeliveries() {
		for (Customer customer : this.customers) {
			for (Restaurant restaurant : this.restaurants) {
				int hour = (int) (Math.random() * (24 + 1));
				waltService.createOrderAndAssignDriver(customer, restaurant, getDateByHour(hour));
			}
		}
	}


	@Test
	public void testCreateOrderAndAssignDriver_ShouldHaveUniqueDeliveryTimes() {
		addRandomDeliveries();

		List<Delivery> deliveries = (List<Delivery>) deliveryRepository.findAll();

		Driver driver = deliveries.get(0).getDriver();
		List<Date> dates = deliveries.stream().filter(d -> d.getDriver().getName().equals(driver.getName()))
				.map(d -> d.getDeliveryTime()).collect(Collectors.toList());

		assertEquals(dates.size(), new HashSet<>(dates).size());

	}
	
	public void printReport(List<DriverDistance> report) {
		for (DriverDistance d : report) {
			System.out.println(d.getDriver().getName() + " " + d.getTotalDistance());
		}
	}

	@Test
	public void testGetDriverRankReport() {
		addRandomDeliveries();

		List<Delivery> deliveries = (List<Delivery>) deliveryRepository.findAll();
		Driver driver = deliveries.get(0).getDriver();

		// calculate total distance for a specific driver from the Repository
		long totalDistance = (long) deliveries.stream().filter(d -> d.getDriver().getName().equals(driver.getName()))
				.map(d -> d.getDistance()).reduce(0.0, (s, e) -> s + e).doubleValue();

		// get the total distance from waltService.getDriverRankReport function
		List<DriverDistance> report = waltService.getDriverRankReport();
		DriverDistance driverDistance = (DriverDistance) report.stream()
				.filter(d -> d.getDriver().getName().equals(driver.getName())).collect(Collectors.toList()).get(0);

		assertEquals(totalDistance, driverDistance.getTotalDistance().longValue());

		printReport(report);

	}

	@Test
	public void testGetDriverRankReportByCity() {
		addRandomDeliveries();

		City city = cityRepository.findByName("Tel-Aviv");
		List<DriverDistance> report = waltService.getDriverRankReportByCity(city);

		for (DriverDistance d : report) {
			assertEquals(d.getDriver().getCity().getId(), city.getId());
		}

		printReport(report);

	}

}
