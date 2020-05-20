package com.gillsoft.control.service;

import java.util.List;
import java.util.Set;

import com.gillsoft.control.service.model.ManageException;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.OrderParams;

public interface OrderDAOManager {
	
	public Order create(Order order) throws ManageException;
	
	public Order booking(Order order) throws ManageException;
	
	public Order confirm(Order order) throws ManageException;
	
	public Order getFullOrder(OrderParams params) throws ManageException;
	
	public Order getOrderPart(OrderParams params) throws ManageException;
	
	public Order cancel(Order order) throws ManageException;
	
	public Order returnServices(Order order) throws ManageException;
	
	public Order update(Order order) throws ManageException;
	
	public Order addServices(Order order) throws ManageException;
	
	public Order removeServices(Order order) throws ManageException;
	
	public Order getDocuments(OrderParams params) throws ManageException;
	
	public List<Order> getOrders(OrderParams params) throws ManageException;
	
	public void reportStatuses(Set<Long> ids) throws ManageException;
	
	public long getUniqueId(long groupeId) throws ManageException;

}
