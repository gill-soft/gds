package com.gillsoft.control.core;

import java.math.BigDecimal;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gillsoft.control.core.ReturnHelper.ServiceReturnHandler;
import com.gillsoft.control.core.data.MsDataController;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.model.AdditionalServiceItem;
import com.gillsoft.model.Price;
import com.gillsoft.model.ReturnCondition;
import com.gillsoft.model.ServiceItem;
import com.gillsoft.util.StringUtil;

@Component
public class AdditionalServiceReturnHandler implements ServiceReturnHandler {

	@Autowired
	private OrderResponseConverter orderResponseConverter;
	
	@Autowired
	private MsDataController dataController;
	
	@Override
	public void calculateReturn(Order order, ServiceItem serviceItem) {
		AdditionalServiceItem additionalService = getAdditionalService(order, serviceItem);
		if (additionalService != null) {
			if (additionalService.getEnableReturn() == null
					|| additionalService.getEnableReturn()) {
				ReturnCondition condition = new ReturnCondition();
				condition.setId(StringUtil.generateUUID());
				condition.setMinutesBeforeDepart(-1000);
				condition.setReturnPercent(new BigDecimal(100));
				serviceItem.getPrice().getTariff().setReturnConditions(Collections.singletonList(condition));
			}
			Price price = dataController.recalculateReturn(additionalService, serviceItem.getPrice(), serviceItem.getPrice().getSource());
			price.getTariff().setReturnConditions(null);
			price.getReturned().getTariff().setReturnConditions(null);
			serviceItem.setPrice(price);
		}
	}
	
	/*
	 * Возвращает допсервис, по переданному сервису.
	 */
	private AdditionalServiceItem getAdditionalService(Order order, ServiceItem service) {
		ServiceItem orderService = orderResponseConverter.getOrderService(order, service);
		if (orderService != null
				&& orderService.getAdditionalService() != null
				&& order.getResponse().getAdditionalServices() != null) {
			return order.getResponse().getAdditionalServices().get(orderService.getAdditionalService().getId());
		}
		return null;
	}

}
