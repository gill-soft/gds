package com.gillsoft.control.core;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gillsoft.control.service.OrderDispatcherDAOService;
import com.gillsoft.control.service.model.MappedService;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.TripDateServices;
import com.gillsoft.util.StringUtil;

@Service
public class DispatcherService {
	
	private static Logger LOGGER = LogManager.getLogger(DispatcherService.class);

	@Autowired
	private OrderDispatcherDAOService dispatcherDAOService;
	
	public List<MappedService> getMappedServices(long carrierId, Date tripDepartureFrom, Date tripDepartureTo) {
		try {
			return dispatcherDAOService.getMappedServices(carrierId, tripDepartureFrom, tripDepartureTo);
		} catch (Exception e) {
			LOGGER.error("Can not get mapped services "
					+ StringUtil.fullDateFormat.format(tripDepartureFrom) + " "
					+ StringUtil.fullDateFormat.format(tripDepartureTo), e);
			return null;
		}
	}
	
	public List<TripDateServices> getGroupedServices(long carrierId, Date tripDepartureFrom, Date tripDepartureTo) {
		try {
			return dispatcherDAOService.getGroupedServices(carrierId, tripDepartureFrom, tripDepartureTo);
		} catch (Exception e) {
			LOGGER.error("Can not get grouped services "
					+ StringUtil.fullDateFormat.format(tripDepartureFrom) + " "
					+ StringUtil.fullDateFormat.format(tripDepartureTo), e);
			return null;
		}
	}
	
	public List<Order> getFromMappedOrders(long carrierId, long tripId, long fromId, Date fromDeparture) {
		try {
			return dispatcherDAOService.getFromMappedOrders(carrierId, tripId, fromId, fromDeparture);
		} catch (Exception e) {
			LOGGER.error("Can not get from mapped orders " + tripId + " " + fromId + " "
					+ StringUtil.fullDateFormat.format(fromDeparture), e);
			return null;
		}
	}

	public List<Order> getToMappedOrders(long carrierId, long tripId, long toId, Date toDeparture) {
		try {
			return dispatcherDAOService.getToMappedOrders(carrierId, tripId, toId, toDeparture);
		} catch (Exception e) {
			LOGGER.error("Can not get to mapped orders " + tripId + " " + toId + " "
					+ StringUtil.fullDateFormat.format(toDeparture), e);
			return null;
		}
	}

	public List<Order> getTripMappedOrders(long carrierId, long tripId, Date departure) {
		try {
			return dispatcherDAOService.getTripMappedOrders(carrierId, tripId, departure);
		} catch (Exception e) {
			LOGGER.error("Can not get trip mapped orders " + tripId + " "
					+ StringUtil.fullDateFormat.format(departure), e);
			return null;
		}
	}
	
}
