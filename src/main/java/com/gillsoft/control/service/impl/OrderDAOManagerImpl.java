package com.gillsoft.control.service.impl;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.gillsoft.control.service.OrderDAOManager;
import com.gillsoft.control.service.model.ManageException;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.OrderParams;
import com.gillsoft.control.service.model.ServiceParams;
import com.gillsoft.model.ServiceItem;

@Repository
public class OrderDAOManagerImpl implements OrderDAOManager {
	
	private final static String GET_ORDER = "from Order as o "
			+ "join fetch o.orders as ro "
			+ "join fetch ro.services as rs "
			+ "join fetch rs.statuses as ss "
			+ "where o.id = :orderId "
			+ "and ss.userId = :userId "
			+ "and ss.created in "
			+ "(select max(ssMax.created) "
			+ "from ServiceStatus ssMax "
			+ "where ssMax.id = ss.id "
			+ "and ssMax.userId = :userId)";
	
	@Autowired
	protected SessionFactory sessionFactory;

	@Transactional
	@Override
	public Order create(Order order) throws ManageException {
		try {
			sessionFactory.getCurrentSession().save(order);
			return order;
		} catch (Exception e) {
			throw new ManageException("Error when save order", e);
		}
	}

	@Override
	public Order reserve(Order order) throws ManageException {
		return update(order);
	}

	@Transactional
	@Override
	public Order confirm(Order order) throws ManageException {
		return update(order);
	}

	@SuppressWarnings("deprecation")
	@Transactional(readOnly = true)
	@Override
	public Order get(OrderParams params) throws ManageException {
		try {
			return sessionFactory.getCurrentSession().createQuery(GET_ORDER, Order.class)
					.setParameter("orderId", params.getOrderId())
					.setParameter("userId", params.getUserId())
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).getSingleResult();
		} catch (Exception e) {
			throw new ManageException("Error when get order", e);
		}
	}

	@Override
	public ServiceItem getService(ServiceParams params) throws ManageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Order cancel(Order order) throws ManageException {
		return update(order);
	}

	@Override
	public Order returnServices(Order order) throws ManageException {
		return update(order);
	}

	@Override
	public Order update(Order order) throws ManageException {
		try {
			sessionFactory.getCurrentSession().update(order);
			return order;
		} catch (Exception e) {
			throw new ManageException("Error when update order", e);
		}
	}

	@Override
	public Order addServices(Order order) throws ManageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Order removeServices(Order order) throws ManageException {
		// TODO Auto-generated method stub
		return null;
	}

}
