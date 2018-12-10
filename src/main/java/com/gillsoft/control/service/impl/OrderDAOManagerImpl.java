package com.gillsoft.control.service.impl;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gillsoft.control.service.OrderDAOManager;
import com.gillsoft.control.service.model.ManageException;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.OrderParams;
import com.gillsoft.control.service.model.ServiceParams;
import com.gillsoft.model.ServiceItem;

@Service
public class OrderDAOManagerImpl implements OrderDAOManager {
	
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Order confirm(Order order) throws ManageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Order get(OrderParams params) throws ManageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceItem getService(ServiceParams params) throws ManageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Order cancel(Order order) throws ManageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Order returnServices(Order order) throws ManageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Order update(Order order) throws ManageException {
		// TODO Auto-generated method stub
		return null;
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
