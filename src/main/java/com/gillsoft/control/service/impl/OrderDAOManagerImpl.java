package com.gillsoft.control.service.impl;

import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.gillsoft.control.service.OrderDAOManager;
import com.gillsoft.control.service.model.GroupeIdEntity;
import com.gillsoft.control.service.model.ManageException;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.OrderParams;

@Repository
public class OrderDAOManagerImpl implements OrderDAOManager {
	
	private final static String GET_ORDER = "from Order as o "
			+ "join fetch o.orders as ro "
			+ "join fetch ro.services as rs "
			+ "join fetch rs.statuses as ss "
			+ "left join fetch ss.price as p "
			+ "where (:orderId is not null or :serviceId is not null) "
			+ "and ((o.id = :orderId or :orderId is null) or (rs.id = :serviceId or :serviceId is null))";
	
	private final static String GET_DOCUMENTS = "from Order as o "
			+ "join fetch o.orders as ro "
			+ "join fetch ro.services as rs "
			+ "join fetch rs.statuses as ss "
			+ "left join fetch ss.price as p "
			+ "left join fetch o.documents as d "
			+ "where o.id = :orderId";
	
	private final static String GET_ORDERS = "from Order as o "
			+ "join fetch o.orders as ro "
			+ "join fetch ro.services as rs "
			+ "join fetch rs.statuses as ss "
			+ "left join fetch ss.price as p "
			+ "where ss.reported is false";
	
	private final static String REPORT_STATUSES = "update ServiceStatusEntity ss "
			+ "set ss.reported = true "
			+ "where ss.id in (:ids)";
	
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

	@Transactional
	@Override
	public Order booking(Order order) throws ManageException {
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
					.setParameter("serviceId", params.getServiceId())
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).getSingleResult();
		} catch (Exception e) {
			throw new ManageException("Error when get order", e);
		}
	}

	@Transactional
	@Override
	public Order cancel(Order order) throws ManageException {
		return update(order);
	}

	@Transactional
	@Override
	public Order returnServices(Order order) throws ManageException {
		return update(order);
	}

	@Transactional
	@Override
	public Order update(Order order) throws ManageException {
		try {
			sessionFactory.getCurrentSession().update(order);
			return order;
		} catch (Exception e) {
			throw new ManageException("Error when update order", e);
		}
	}

	@Transactional
	@Override
	public Order addServices(Order order) throws ManageException {
		return update(order);
	}

	@Transactional
	@Override
	public Order removeServices(Order order) throws ManageException {
		return update(order);
	}

	@SuppressWarnings("deprecation")
	@Transactional(readOnly = true)
	@Override
	public Order getDocuments(OrderParams params) throws ManageException {
		try {
			return sessionFactory.getCurrentSession().createQuery(GET_DOCUMENTS, Order.class)
					.setParameter("orderId", params.getOrderId())
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).getSingleResult();
		} catch (Exception e) {
			throw new ManageException("Error when get order documents", e);
		}
	}

	@SuppressWarnings("deprecation")
	@Transactional(readOnly = true)
	@Override
	public List<Order> getOrders(OrderParams params) throws ManageException {
		try {
			return sessionFactory.getCurrentSession().createQuery(GET_ORDERS, Order.class)
					.setMaxResults(params.getCount())
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).getResultList();
		} catch (Exception e) {
			throw new ManageException("Error when get orders", e);
		}
	}

	@Transactional
	@Override
	public void reportStatuses(Set<Long> ids) throws ManageException {
		try {
			sessionFactory.getCurrentSession().createQuery(REPORT_STATUSES)
					.setParameter("ids", ids).executeUpdate();
		} catch (Exception e) {
			throw new ManageException("Error when report statuses", e);
		}
	}

	@Transactional
	@Override
	public long getUniqueId(long groupeId) throws ManageException {
		try {
			GroupeIdEntity groupeIdEntity = new GroupeIdEntity();
			groupeIdEntity.setGroupeId(groupeId);
			sessionFactory.getCurrentSession().save(groupeIdEntity);
			return groupeIdEntity.getId();
		} catch (Exception e) {
			throw new ManageException("Error when create unique", e);
		}
	}

}
