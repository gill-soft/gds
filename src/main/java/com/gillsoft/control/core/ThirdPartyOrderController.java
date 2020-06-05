package com.gillsoft.control.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import com.gillsoft.control.api.MethodUnavalaibleException;
import com.gillsoft.control.api.NoDataFoundException;
import com.gillsoft.control.service.OrderDAOManager;
import com.gillsoft.control.service.model.ManageException;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.OrderParams;
import com.gillsoft.model.Resource;
import com.gillsoft.model.Segment;
import com.gillsoft.model.ServiceStatus;
import com.gillsoft.model.response.OrderResponse;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ThirdPartyOrderController {
	
	private static Logger LOGGER = LogManager.getLogger(ThirdPartyOrderController.class);
	
	@Autowired
	private OrderResponseConverter orderConverter;
	
	@Autowired
	private OrderDAOManager manager;
	
	@Autowired
	private OrderRequestController requestController;
	
	public void saveOrUpdate(List<OrderResponse> responses) {
		for (OrderResponse orderResponse : responses) {
			try {
				Order order = findOrCreateOrder(getCopy(orderResponse));
				if (order == null) {
					throw new NoDataFoundException("Order not found and not created");
				}
				confirmOrder(getCopy(orderResponse), order);
				returOrder(getCopy(orderResponse), order);
				cancelOrder(getCopy(orderResponse), order);
			} catch (Exception e) {
				LOGGER.error(e);
			}
		}
		
	}
	
	private OrderResponse getCopy(OrderResponse orderResponse) {
		return (OrderResponse) SerializationUtils.deserialize(SerializationUtils.serialize(orderResponse));
	}
	
	private Order findOrCreateOrder(OrderResponse orderResponse) {
		Order order = null;
		try {
			return manager.getFullOrder(createFindOrderParams(orderResponse.getResources().get(0)));
		} catch (ManageException e) {
			order = orderConverter.convertToNewOrder(orderResponse);
		}
		try {
			manager.create(order);
		} catch (ManageException e) {
			LOGGER.error(e);
		}
		return order;
	}
	
	private OrderParams createFindOrderParams(OrderResponse orderResponse) {
		OrderParams params = new OrderParams();
		params.setResourceNativeOrderId(new IdModel(getResourceId(orderResponse),
				orderResponse.getOrderId()).asString());
		return params;
	}
	
	private long getResourceId(OrderResponse orderResponse) {
		if (orderResponse.getSegments() != null
				&& !orderResponse.getSegments().isEmpty()) {
			for (Segment segment : orderResponse.getSegments().values()) {
				Resource resource = segment.getResource();
				if (resource != null) {
					return orderConverter.getParamsResourceId(orderConverter.getResourceParam(resource.getId()));
				}
			}
		}
		return -1;
	}
	
	private void confirmOrder(OrderResponse orderResponse, Order order) {
		try {
			requestController.checkStatus(order, requestController.getStatusesForConfirm());
			
			Set<ServiceStatus> statuses = new HashSet<>();
			statuses.add(ServiceStatus.CONFIRM);
			statuses.add(ServiceStatus.RETURN);
			statuses.add(ServiceStatus.CANCEL);
			requestController.checkStatus(orderResponse.getResources(), statuses);
			
			order = orderConverter.convertToConfirm(order, orderResponse.getResources(),
					ServiceStatus.CONFIRM, ServiceStatus.CONFIRM_ERROR);
			manager.confirm(order);
		} catch (MethodUnavalaibleException | ManageException e) {
			LOGGER.error(e);
		}
	}
	
	private void returOrder(OrderResponse orderResponse, Order order) {
		try {
			requestController.checkStatus(order, requestController.getStatusesForReturn());
			requestController.checkStatus(orderResponse.getResources(), Collections.singleton(ServiceStatus.RETURN));
			
			order = orderConverter.convertToReturn(order, orderResponse.getResources());
			manager.confirm(order);
		} catch (MethodUnavalaibleException | ManageException e) {
			LOGGER.error(e);
		}
	}
	
	private void cancelOrder(OrderResponse orderResponse, Order order) {
		try {
			requestController.checkStatus(order, requestController.getStatusesForCancel());
			requestController.checkStatus(orderResponse.getResources(), Collections.singleton(ServiceStatus.CANCEL));
			
			order = orderConverter.convertToConfirm(order, orderResponse.getResources(),
					ServiceStatus.CANCEL, ServiceStatus.CANCEL_ERROR);
			manager.confirm(order);
		} catch (MethodUnavalaibleException | ManageException e) {
			LOGGER.error(e);
		}
	}

}
