package com.gillsoft.control.core;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gillsoft.control.core.data.DataConverter;
import com.gillsoft.control.core.data.MsDataController;
import com.gillsoft.control.service.OrderDispatcherDAOService;
import com.gillsoft.control.service.model.MappedService;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.TripDateServices;
import com.gillsoft.ms.entity.RoutePoint;
import com.gillsoft.ms.entity.Trip;
import com.gillsoft.util.StringUtil;

@Service
public class DispatcherService {
	
	private static Logger LOGGER = LogManager.getLogger(DispatcherService.class);

	@Autowired
	private OrderDispatcherDAOService dispatcherDAOService;
	
	@Autowired
	private OrderResponseConverter converter;
	
	@Autowired
	private MsDataController dataController;
	
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
			Trip trip = getTrip(tripId);
			List<RoutePoint> fromPoints = getPointsById(trip, fromId);
			return replaceResponse(dispatcherDAOService.getFromMappedOrders(carrierId, tripId, fromId, fromDeparture, getFromDepartureTo(fromPoints, fromDeparture)));
		} catch (Exception e) {
			LOGGER.error("Can not get from mapped orders " + tripId + " " + fromId + " "
					+ StringUtil.fullDateFormat.format(fromDeparture), e);
			return null;
		}
	}
	
	private Trip getTrip(long tripId) {
		Trip trip = dataController.getTrip(tripId);
		if (trip == null) {
			throw new NullPointerException("Trip is not present");
		}
		return trip;
	}
	
	private List<RoutePoint> getPointsById(Trip trip, long pointId) {
		List<RoutePoint> route = DataConverter.getRoute(trip);
		String id = String.valueOf(pointId);
		return route.stream().filter(p -> id.equals(p.getLocality().getParent().getId())).collect(Collectors.toList());
	}
	
	private Date getFromDepartureTo(List<RoutePoint> fromPoints, Date fromDeparture) throws ParseException {
		RoutePoint departurePoint = getDeparturePoint(fromPoints, fromDeparture);
		RoutePoint maxDeparturePoint = fromPoints.stream().max(Comparator.comparing(p -> p.getArrivalDay() + p.getDepartureTime())).get();
		Calendar departureDate = Calendar.getInstance();
		departureDate.setTime(StringUtil.fullDateFormat.parse(StringUtil.dateFormat.format(fromDeparture)
				+ " " + maxDeparturePoint.getDepartureTime()));
		departureDate.add(Calendar.DATE, maxDeparturePoint.getArrivalDay() - departurePoint.getArrivalDay());
		return departureDate.getTime();
	}
	
	private RoutePoint getDeparturePoint(List<RoutePoint> fromPoints, Date fromDeparture) {
		String departureTime = StringUtil.timeFormat.format(fromDeparture);
		Optional<RoutePoint> finded = fromPoints.stream().filter(p -> departureTime.equals(p.getDepartureTime())).findFirst();
		if (finded.isPresent()) {
			return finded.get();
		} else {
			throw new NullPointerException("Departure time is not present");
		}
	}
	
	public List<Order> getToMappedOrders(long carrierId, long tripId, long toId, Date toArrival) {
		try {
			Trip trip = getTrip(tripId);
			List<RoutePoint> toPoints = getPointsById(trip, toId);
			return replaceResponse(dispatcherDAOService.getToMappedOrders(carrierId, tripId, toId, toArrival, getToArrivalTo(toPoints, toArrival)));
		} catch (Exception e) {
			LOGGER.error("Can not get to mapped orders " + tripId + " " + toId + " "
					+ StringUtil.fullDateFormat.format(toArrival), e);
			return null;
		}
	}
	
	private Date getToArrivalTo(List<RoutePoint> toPoints, Date toArrival) throws ParseException {
		RoutePoint arrivalPoint = getArrivalPoint(toPoints, toArrival);
		RoutePoint maxArrivalPoint = toPoints.stream().max(Comparator.comparing(p -> p.getArrivalDay() + p.getArrivalTime())).get();
		Calendar arrivalDate = Calendar.getInstance();
		arrivalDate.setTime(StringUtil.fullDateFormat.parse(StringUtil.dateFormat.format(toArrival)
				+ " " + maxArrivalPoint.getArrivalTime()));
		arrivalDate.add(Calendar.DATE, maxArrivalPoint.getArrivalDay() - arrivalPoint.getArrivalDay());
		return arrivalDate.getTime();
	}
	
	private RoutePoint getArrivalPoint(List<RoutePoint> toPoints, Date toDeparture) {
		String arrivalTime = StringUtil.timeFormat.format(toDeparture);
		Optional<RoutePoint> finded = toPoints.stream().filter(p -> arrivalTime.equals(p.getArrivalTime())).findFirst();
		if (finded.isPresent()) {
			return finded.get();
		} else {
			throw new NullPointerException("Arrival time is not present");
		}
	}

	public List<Order> getTripMappedOrders(long carrierId, long tripId, Date departure) {
		try {
			return replaceResponse(dispatcherDAOService.getTripMappedOrders(carrierId, tripId, departure));
		} catch (Exception e) {
			LOGGER.error("Can not get trip mapped orders " + tripId + " "
					+ StringUtil.fullDateFormat.format(departure), e);
			return null;
		}
	}
	
	private List<Order> replaceResponse(List<Order> orders) {
		orders.forEach(o -> converter.replaceResponseWithConverted(o));
		return orders;
	}
	
}
