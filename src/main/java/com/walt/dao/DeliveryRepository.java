package com.walt.dao;

import com.walt.model.City;
import com.walt.model.Delivery;
import com.walt.model.DriverDistance;
import com.walt.model.DriverOrdersCount;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DeliveryRepository extends CrudRepository<Delivery, Long> {

	@Query("SELECT new com.walt.model.DriverOrdersCount(dr, Count(del)) FROM Driver AS dr "
			+ "LEFT JOIN Delivery AS del "
			+ "ON dr = del.driver " 
			+ "WHERE dr NOT IN "
				+ "(SELECT DISTINCT bs.driver "
				+ "FROM Delivery AS bs "
				+ "WHERE (HOUR(bs.deliveryTime) < (HOUR(:deliverytime) + 1))"
				+ "AND (HOUR(:deliverytime) < (HOUR(bs.deliveryTime) + 1))) " 
			+ "AND dr.city = :city "
			+ "GROUP BY dr "
			+ "ORDER BY Count(del)")
	List<DriverOrdersCount> findAvailableDrivers(@Param("city") City city, @Param("deliverytime") Date deliverytime);

	@Query("SELECT new com.walt.model.DriverDistanceImpl(dr, SUM(del.distance)) "
			+ "FROM Driver AS dr "
			+ "LEFT JOIN Delivery AS del "
			+ "ON dr = del.driver "
			+ "GROUP BY dr "
			+ "ORDER BY SUM(del.distance) DESC")
	List<DriverDistance> findAllDriverDistance();

	@Query("SELECT new com.walt.model.DriverDistanceImpl(dr, SUM(del.distance)) "
			+ "FROM Driver as dr "
			+ "LEFT JOIN Delivery AS del " 
			+ "ON dr = del.driver " 
			+ "WHERE del.driver.city = :city "
			+ "GROUP BY dr "
			+ "ORDER BY SUM(del.distance) DESC")
	List<DriverDistance> findAllDriverDistanceByCity(@Param("city") City city);

}
