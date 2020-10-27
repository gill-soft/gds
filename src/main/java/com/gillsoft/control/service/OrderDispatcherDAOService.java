package com.gillsoft.control.service;

import java.util.Date;
import java.util.List;

import com.gillsoft.control.service.model.ManageException;
import com.gillsoft.control.service.model.MappedService;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.TripDateServices;

public interface OrderDispatcherDAOService {
	
	public List<MappedService> getMappedServices(long carrierId, Date tripDepartureFrom, Date tripDepartureTo) throws ManageException;
	
	public List<TripDateServices> getGroupedServices(long carrierId, Date tripDepartureFrom, Date tripDepartureTo) throws ManageException;
	
	public List<Order> getFromMappedOrders(long carrierId, long tripId, long fromId, Date fromDeparture) throws ManageException;
	
	public List<Order> getToMappedOrders(long carrierId, long tripId, long toId, Date toDeparture) throws ManageException;
	
	public List<Order> getTripMappedOrders(long carrierId, long tripId, Date departure) throws ManageException;

}
