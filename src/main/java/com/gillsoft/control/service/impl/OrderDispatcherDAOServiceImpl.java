package com.gillsoft.control.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.TemporalType;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.gillsoft.control.service.OrderDispatcherDAOService;
import com.gillsoft.control.service.model.ManageException;
import com.gillsoft.control.service.model.MappedService;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.TripDateServices;

@Repository
public class OrderDispatcherDAOServiceImpl implements OrderDispatcherDAOService {
	
	private final static String EXISTS_STATUS = "exists (from ServiceStatusEntity as sse "
				+ "where sse.parent = rs "
				+ "and sse.created = (select max(ssem.created) from ServiceStatusEntity as ssem "
					+ "where ssem.parent = rs "
					+ "and ssem.status not like '%_ERROR') "
				+ "and sse.status = 'CONFIRM')";
	
	private final static String GET_MAPPED_SERVICES = "select mc from Order as o "
			+ "join o.orders as ro "
			+ "join ro.services as rs "
			+ "join rs.mappedServices as mc "
			+ "where mc.tripDeparture >= :tripDepartureFrom "
			+ "and mc.tripDeparture <= :tripDepartureTo "
			+ "and mc.carrierId = :carrierId "
			+ "and " + EXISTS_STATUS;
	
	private final static String GET_GROUPED_SERVICES = "select new com.gillsoft.control.service.model.TripDateServices(mc.tripId, mc.tripDeparture, count(mc.tripId)) "
			+ "from Order as o "
			+ "join o.orders as ro "
			+ "join ro.services as rs "
			+ "join rs.mappedServices as mc "
			+ "where mc.tripDeparture >= :tripDepartureFrom "
			+ "and mc.tripDeparture <= :tripDepartureTo "
			+ "and mc.carrierId = :carrierId "
			+ "and " + EXISTS_STATUS
			+ " group by mc.tripId, mc.tripDeparture";
	
	private final static String SELECT_ORDERS = "select o from Order as o "
			+ "join fetch o.orders as ro "
			+ "join fetch ro.services as rs "
			+ "join fetch rs.statuses as ss "
			+ "left join fetch ss.price as p "
			+ "join fetch rs.mappedServices as mc";
	
	private final static String GET_FROM_MAPPED_ORDERS = SELECT_ORDERS
			+ " where mc.tripId = :tripId "
			+ "and mc.fromId = :fromId "
			+ "and mc.carrierId = :carrierId "
			+ "and mc.fromDeparture >= :fromDepartureFrom "
			+ "and mc.fromDeparture <= :fromDepartureTo "
			+ "and " + EXISTS_STATUS;
	
	private final static String GET_TO_MAPPED_ORDERS = SELECT_ORDERS
			+ " where mc.tripId = :tripId "
			+ "and mc.toId = :toId "
			+ "and mc.carrierId = :carrierId "
			+ "and mc.toDeparture >= :toDepartureFrom "
			+ "and mc.toDeparture <= :toDepartureTo "
			+ "and " + EXISTS_STATUS;
	
	private final static String GET_TRIP_MAPPED_ORDERS = SELECT_ORDERS
			+ " where mc.tripId = :tripId "
			+ "and mc.tripDeparture = :tripDeparture "
			+ "and mc.carrierId = :carrierId "
			+ "and " + EXISTS_STATUS;
	
	@Autowired
	protected SessionFactory sessionFactory;

	@Transactional(readOnly = true)
	@Override
	public List<MappedService> getMappedServices(long carrierId, Date tripDepartureFrom, Date tripDepartureTo) throws ManageException {
		try {
			return sessionFactory.getCurrentSession().createQuery(GET_MAPPED_SERVICES, MappedService.class)
					.setParameter("tripDepartureFrom", tripDepartureFrom, TemporalType.DATE)
					.setParameter("tripDepartureTo", tripDepartureTo, TemporalType.DATE)
					.setParameter("carrierId", carrierId).getResultList();
		} catch (Exception e) {
			throw new ManageException("Error when get mapped services", e);
		}
	}
	
	@Transactional(readOnly = true)
	@Override
	public List<TripDateServices> getGroupedServices(long carrierId, Date tripDepartureFrom, Date tripDepartureTo)
			throws ManageException {
		try {
			return sessionFactory.getCurrentSession().createQuery(GET_GROUPED_SERVICES, TripDateServices.class)
					.setParameter("tripDepartureFrom", tripDepartureFrom, TemporalType.DATE)
					.setParameter("tripDepartureTo", tripDepartureTo, TemporalType.DATE)
					.setParameter("carrierId", carrierId).getResultList();
		} catch (Exception e) {
			throw new ManageException("Error when get grouped services", e);
		}
	}

	@SuppressWarnings("deprecation")
	@Transactional(readOnly = true)
	@Override
	public List<Order> getFromMappedOrders(long carrierId, long tripId, long fromId, Date fromDeparture) throws ManageException {
		try {
			return sessionFactory.getCurrentSession().createQuery(GET_FROM_MAPPED_ORDERS, Order.class)
					.setParameter("tripId", tripId)
					.setParameter("fromId", fromId)
					.setParameter("carrierId", carrierId)
					.setParameter("fromDepartureFrom", beginOfDay(fromDeparture))
					.setParameter("fromDepartureTo", endOfDay(fromDeparture))
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).getResultList();
		} catch (Exception e) {
			throw new ManageException("Error when get from mapped orders", e);
		}
	}

	@SuppressWarnings("deprecation")
	@Transactional(readOnly = true)
	@Override
	public List<Order> getToMappedOrders(long carrierId, long tripId, long toId, Date toDeparture) throws ManageException {
		try {
			return sessionFactory.getCurrentSession().createQuery(GET_TO_MAPPED_ORDERS, Order.class)
					.setParameter("tripId", tripId)
					.setParameter("toId", toId)
					.setParameter("carrierId", carrierId)
					.setParameter("toDepartureFrom", beginOfDay(toDeparture))
					.setParameter("toDepartureTo", endOfDay(toDeparture))
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).getResultList();
		} catch (Exception e) {
			throw new ManageException("Error when get to mapped orders", e);
		}
	}

	@SuppressWarnings("deprecation")
	@Transactional(readOnly = true)
	@Override
	public List<Order> getTripMappedOrders(long carrierId, long tripId, Date departure) throws ManageException {
		try {
			return sessionFactory.getCurrentSession().createQuery(GET_TRIP_MAPPED_ORDERS, Order.class)
					.setParameter("tripId", tripId)
					.setParameter("tripDeparture", departure, TemporalType.DATE)
					.setParameter("carrierId", carrierId)
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).getResultList();
		} catch (Exception e) {
			throw new ManageException("Error when get trip mapped orders", e);
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
