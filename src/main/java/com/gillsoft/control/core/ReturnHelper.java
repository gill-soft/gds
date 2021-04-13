package com.gillsoft.control.core;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.gillsoft.control.service.model.Order;
import com.gillsoft.model.ServiceItem;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReturnHelper {
	
	@Autowired
	private List<ServiceReturnHandler> returnHandlers;
	
	public void calculateReturn(Order order, ServiceItem serviceItem) {
		for (ServiceReturnHandler returnHandler : returnHandlers) {
			returnHandler.calculateReturn(order, serviceItem);
		}
	}
	
	public static interface ServiceReturnHandler {
		
		public void calculateReturn(Order order, ServiceItem serviceItem);
		
	}

}
