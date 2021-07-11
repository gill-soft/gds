package com.gillsoft.control.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.gillsoft.control.service.OrderDAOManager;
import com.gillsoft.control.service.model.GroupeIdEntity;
import com.gillsoft.control.service.model.ManageException;
import com.gillsoft.control.service.model.MappedService;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.OrderClient;
import com.gillsoft.control.service.model.OrderParams;
import com.gillsoft.control.service.model.ResourceService;
import com.gillsoft.model.ServiceStatus;
import com.gillsoft.ms.entity.Client;

@Repository
public class OrderDAOManagerImpl implements OrderDAOManager {
	
	private final static String GET_FULL_ORDER = "from Order as o "
			+ "join fetch o.orders as ro "
			+ "join fetch ro.services as rs "
			+ "join fetch rs.statuses as ss "
			+ "left join fetch ss.price as p "
			+ "left join fetch o.clients as c "
			+ "where (o.id = :orderId or :orderId is null) "
			+ "and exists (from ResourceOrder as ro2 "
				+ "join ro2.services as rs2 "
				+ "where ro2.parent = o "
				+ "and (rs2.id = :serviceId or :serviceId is null) "
				+ "and (ro2.resourceNativeOrderId = :nativeOrderId or :nativeOrderId is null))";
	
	private final static String GET_ORDER_PART = "from Order as o "
			+ "join fetch o.orders as ro "
			+ "join fetch ro.services as rs "
			+ "join fetch rs.statuses as ss "
			+ "left join fetch ss.price as p "
			+ "left join fetch o.clients as c "
			+ "where (o.id = :orderId or :orderId is null) "
			+ "and (rs.id = :serviceId or :serviceId is null) "
			+ "and (ro.resourceNativeOrderId = :nativeOrderId or :nativeOrderId is null)";
	
	private final static String GET_DOCUMENTS = "from Order as o "
			+ "join fetch o.orders as ro "
			+ "join fetch ro.services as rs "
			+ "join fetch rs.statuses as ss "
			+ "left join fetch ss.price as p "
			+ "left join fetch o.clients as c "
			+ "left join fetch o.documents as d "
			+ "where (o.id = :orderId or :orderId is null) "
			+ "and (rs.id = :serviceId or :serviceId is null) "
			+ "and (ro.resourceNativeOrderId = :nativeOrderId or :nativeOrderId is null)";
	
	private final static String GET_ORDERS = "select o from Order as o "
			+ "join fetch o.orders as ro "
			+ "join fetch ro.services as rs "
			+ "join fetch rs.statuses as ss "
			+ "left join fetch ss.price as p "
			+ "left join fetch o.clients as c "
			+ "left join fetch rs.mappedServices as mc "
			+ "where exists (select oe from Order as oe "
				+ "join oe.orders as roe "
				+ "join roe.services as rse "
				+ "join rse.statuses as sse "
				+ "left join oe.clients as ce "
				+ "left join rse.mappedServices as mce "
				+ "where oe = o "
				+ "and (sse.reported is :reported or :reported is null) "
				+ "and ((:userId is null and :clientId is null and :clientPhone is null) "
						+ "{0}) "
				+ "and (sse.created >= :from or :from is null) "
				+ "and (sse.created <= :to or :to is null) "
				+ "and (rse.departure >= :departureFrom or :departureFrom is null or mce.toDeparture >= :departureFrom) "
				+ "and (rse.departure <= :departureTo or :departureTo is null) "
				+ "and ((rse.departure >= :mappedDeparture or :mappedDeparture is null) "
					+ "and (rse.mappedTrip is :mappedTrip or :mappedTrip is null)) "
				+ "and (:statusesStr is null or "
					+ "exists (from ServiceStatusEntity as ssee "
					+ "where ssee.parent = rse "
					+ "and ssee.created = (select max(sseem.created) from ServiceStatusEntity as sseem "
						+ "where sseem.parent = rse) "
					+ "and ssee.status in (:statuses))))";
	
	private final static String USER_ID_CONDITION = "or sse.userId = :userId";
	private final static String CLIENT_ID_CONDITION = "or ce.clientId = :clientId";
	private final static String CLIENT_PHONE_CONDITION = "or ce.phone = :clientPhone";
	
	private final static String REPORT_STATUSES = "update ServiceStatusEntity ss "
			+ "set ss.reported = true "
			+ "where ss.id in (:ids)";
	
	private final static String UPDATE_RESPONSE = "update Order as o "
			+ "set o.response = :response "
			+ "where o.id = :id";
	
	private final static String MARK_SERVICE_MAPPED_TRIP = "update ResourceService as rs "
			+ "set rs.mappedTrip = true "
			+ "where rs.id = :id";
	
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

	@Transactional(readOnly = true)
	@Override
	public Order getFullOrder(OrderParams params) throws ManageException {
		return getOrder(GET_FULL_ORDER, params);
	}
	
	@Transactional(readOnly = true)
	@Override
	public Order getOrderPart(OrderParams params) throws ManageException {
		return getOrder(GET_ORDER_PART, params);
	}
	
	@SuppressWarnings("deprecation")
	private Order getOrder(String query, OrderParams params) throws ManageException {
		try {
			return sessionFactory.getCurrentSession().createQuery(query, Order.class)
					.setParameter("orderId", params.getOrderId())
					.setParameter("serviceId", params.getServiceId())
					.setParameter("nativeOrderId", params.getResourceNativeOrderId())
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

	@Transactional(readOnly = true)
	@Override
	public Order getDocuments(OrderParams params) throws ManageException {
		return getOrder(GET_DOCUMENTS, params);
	}

	@SuppressWarnings("deprecation")
	@Transactional(readOnly = true)
	@Override
	public List<Order> getOrders(OrderParams params) throws ManageException {
		try {
			Map<Long, Order> result = new HashMap<>();
			boolean queryCalled = false;
			if (params.getUserId() != null) {
				queryCalled = true;
				result.putAll(createOrdersQuery(MessageFormat.format(GET_ORDERS, USER_ID_CONDITION), params)
						.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).getResultList().stream()
						.collect(Collectors.toMap(Order::getId, o -> o)));
			}
			if (params.getClientId() != null) {
				queryCalled = true;
				result.putAll(createOrdersQuery(MessageFormat.format(GET_ORDERS, CLIENT_ID_CONDITION), params)
						.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).getResultList().stream()
						.collect(Collectors.toMap(Order::getId, o -> o)));
			}
			if (params.getClientPhone() != null) {
				queryCalled = true;
				result.putAll(createOrdersQuery(MessageFormat.format(GET_ORDERS, CLIENT_PHONE_CONDITION), params)
						.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).getResultList().stream()
						.collect(Collectors.toMap(Order::getId, o -> o)));
			}
			if (!queryCalled) {
				result.putAll(createOrdersQuery(MessageFormat.format(GET_ORDERS, ""), params)
						.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).getResultList().stream()
						.collect(Collectors.toMap(Order::getId, o -> o)));
			}
			return new ArrayList<>(result.values());
		} catch (Exception e) {
			throw new ManageException("Error when get orders", e);
		}
	}
	
	private Query<Order> createOrdersQuery(String query, OrderParams params) {
		return sessionFactory.getCurrentSession().createQuery(query, Order.class)
				.setParameter("reported", params.getReported())
				.setParameter("userId", params.getUserId())
				.setParameter("clientId", params.getClientId())
				.setParameter("clientPhone", params.getClientPhone())
				.setParameter("from", params.getFrom())
				.setParameter("to", params.getTo())
				.setParameter("departureFrom", params.getDepartureFrom())
				.setParameter("departureTo", params.getDepartureTo())
				.setParameter("mappedDeparture", params.getMappedDeparture())
				.setParameter("mappedTrip", params.getMappedTrip())
				.setParameter("statusesStr", params.getStatuses() == null || params.getStatuses().isEmpty() ? null :
					params.getStatuses().stream().map(ServiceStatus::name).collect(Collectors.joining(",")))
				.setParameterList("statuses", params.getStatuses() == null || params.getStatuses().isEmpty() ? 
					Collections.singleton(ServiceStatus.UNAVAILABLE) : params.getStatuses());
	}

	@Transactional
	@Override
	public void reportStatuses(Set<Long> ids) throws ManageException {
		try {
			sessionFactory.getCurrentSession().createQuery(REPORT_STATUSES)
					.setParameterList("ids", ids).executeUpdate();
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

	@Transactional
	@Override
	public void updateOrderResponse(Order order) throws ManageException {
		try {
			sessionFactory.getCurrentSession().createQuery(UPDATE_RESPONSE)
					.setParameter("id", order.getId())
					.setParameter("response", order.getResponse()).executeUpdate();
		} catch (Exception e) {
			throw new ManageException("Error when update response", e);
		}
	}

	@Transactional
	@Override
	public void markResourceServiceMappedTrip(ResourceService service) throws ManageException {
		try {
			for (MappedService mappedService : service.getMappedServices()) {
				sessionFactory.getCurrentSession().saveOrUpdate(mappedService);
			}
			sessionFactory.getCurrentSession().createQuery(MARK_SERVICE_MAPPED_TRIP)
					.setParameter("id", service.getId()).executeUpdate();
		} catch (Exception e) {
			throw new ManageException("Error when update service", e);
		}
	}

	@Transactional
	@Override
	public void addOrderClient(Order order, Client client) throws ManageException {
		try {
			OrderClient orderClient = new OrderClient();
			orderClient.setFields(client);
			orderClient.setParent(order);
			sessionFactory.getCurrentSession().save(orderClient);
		} catch (Exception e) {
			throw new ManageException("Error when add order client", e);
		}
	}
	
}
