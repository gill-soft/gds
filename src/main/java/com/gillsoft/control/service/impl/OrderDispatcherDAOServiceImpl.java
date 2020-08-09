package com.gillsoft.control.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.gillsoft.control.service.OrderDispatcherDAOService;
import com.gillsoft.control.service.model.ManageException;
import com.gillsoft.control.service.model.MappedService;
import com.gillsoft.control.service.model.Order;

@Repository
public class OrderDispatcherDAOServiceImpl implements OrderDispatcherDAOService {
	
	private final static String EXISTS_STATUS = "exists (from ServiceStatusEntity as sse "
				+ "where sse.parent = rs "
				+ "and sse.created = (select max(ssem.created) from ServiceStatusEntity as ssem "
					+ "where ssem.parent = rs) "
				+ "and sse.status = 'CONFIRM')";
	
	private final static String GET_MAPPED_SERVICES = "select mc from Order as o "
			+ "join o.orders as ro "
			+ "join ro.services as rs "
			+ "join rs.mappedServices as mc "
			+ "where mc.tripDeparture >= :tripDepartureFrom "
			+ "and mc.tripDeparture <= :tripDepartureTo "
			+ "and " + EXISTS_STATUS;
	
	private final static String SELECT_ORDERS = "select o from Order as o "
			+ "join fetch o.orders as ro "
			+ "join fetch ro.services as rs "
			+ "join fetch rs.statuses as ss "
			+ "left join fetch ss.price as p "
			+ "join fetch rs.mappedServices as mc";
	
	private final static String GET_FROM_MAPPED_ORDERS = SELECT_ORDERS
			+ " where mc.tripId = :tripId "
			+ "and mc.fromId = :fromId "
			+ "and mc.fromDeparture >= :fromDepartureFrom "
			+ "and mc.fromDeparture <= :fromDepartureTo "
			+ "and " + EXISTS_STATUS;
	
	private final static String GET_TO_MAPPED_ORDERS = SELECT_ORDERS
			+ " where mc.tripId = :tripId "
			+ "and mc.toId = :toId "
			+ "and mc.toDeparture >= :toDepartureFrom "
			+ "and mc.toDeparture <= :toDepartureTo "
			+ "and " + EXISTS_STATUS;
	
	private final static String GET_TRIP_MAPPED_ORDERS = SELECT_ORDERS
			+ " where mc.tripId = :tripId "
			+ "and mc.tripDeparture >= :tripDepartureFrom "
			+ "and mc.tripDeparture <= :tripDepartureTo "
			+ "and " + EXISTS_STATUS;
	
	@Autowired
	protected SessionFactory sessionFactory;

	@SuppressWarnings("deprecation")
	@Transactional(readOnly = true)
	@Override
	public List<MappedService> getMappedServices(Date tripDepartureFrom, Date tripDepartureTo) throws ManageException {
		try {
			return sessionFactory.getCurrentSession().createQuery(GET_MAPPED_SERVICES, MappedService.class)
					.setParameter("tripDepartureFrom", beginOfDay(tripDepartureFrom))
					.setParameter("tripDepartureTo", endOfDay(tripDepartureTo))
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).getResultList();
		} catch (Exception e) {
			throw new ManageException("Error when get order", e);
		}
	}

	@SuppressWarnings("deprecation")
	@Transactional(readOnly = true)
	@Override
	public List<Order> getFromMappedOrders(long tripId, long fromId, Date fromDeparture) throws ManageException {
		try {
			return sessionFactory.getCurrentSession().createQuery(GET_FROM_MAPPED_ORDERS, Order.class)
					.setParameter("tripId", tripId)
					.setParameter("fromId", fromId)
					.setParameter("fromDepartureFrom", beginOfDay(fromDeparture))
					.setParameter("fromDepartureTo", endOfDay(fromDeparture))
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).getResultList();
		} catch (Exception e) {
			throw new ManageException("Error when get order", e);
		}
	}

	@SuppressWarnings("deprecation")
	@Transactional(readOnly = true)
	@Override
	public List<Order> getToMappedOrders(long tripId, long toId, Date toDeparture) throws ManageException {
		try {
			return sessionFactory.getCurrentSession().createQuery(GET_TO_MAPPED_ORDERS, Order.class)
					.setParameter("tripId", tripId)
					.setParameter("toId", toId)
					.setParameter("toDepartureFrom", beginOfDay(toDeparture))
					.setParameter("toDepartureTo", endOfDay(toDeparture))
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).getResultList();
		} catch (Exception e) {
			throw new ManageException("Error when get order", e);
		}
	}

	@SuppressWarnings("deprecation")
	@Transactional(readOnly = true)
	@Override
	public List<Order> getTripMappedOrders(long tripId, Date departure) throws ManageException {
		try {
			return sessionFactory.getCurrentSession().createQuery(GET_TRIP_MAPPED_ORDERS, Order.class)
					.setParameter("tripId", tripId)
					.setParameter("tripDepartureFrom", beginOfDay(departure))
					.setParameter("tripDepartureTo", endOfDay(departure))
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).getResultList();
		} catch (Exception e) {
			throw new ManageException("Error when get order", e);
		}
	}
	
	private Date beginOfDay(Date date) {
		Calendar time = Calendar.getInstance();
		time.setTime(date);
		time.set(Calendar.HOUR_OF_DAY, 0);
		time.set(Calendar.MINUTE, 0);
		time.set(Calendar.SECOND, 0);
		time.set(Calendar.MILLISECOND, 0);
		return time.getTime();
	}
	
	private Date endOfDay(Date date) {
		Calendar time = Calendar.getInstance();
		time.setTime(date);
		time.set(Calendar.HOUR_OF_DAY, 23);
		time.set(Calendar.MINUTE, 59);
		time.set(Calendar.SECOND, 59);
		time.set(Calendar.MILLISECOND, 999);
		return time.getTime();
	}

}
