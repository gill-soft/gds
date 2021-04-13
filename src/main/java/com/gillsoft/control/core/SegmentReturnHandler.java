package com.gillsoft.control.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gillsoft.control.core.ReturnHelper.ServiceReturnHandler;
import com.gillsoft.control.core.data.MsDataController;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.model.Segment;
import com.gillsoft.model.ServiceItem;

@Component
public class SegmentReturnHandler implements ServiceReturnHandler {
	
	@Autowired
	private OrderResponseConverter orderResponseConverter;
	
	@Autowired
	private MsDataController dataController;

	@Override
	public void calculateReturn(Order order, ServiceItem serviceItem) {
		Segment segment = getSegment(order, serviceItem);
		if (segment != null) {
			serviceItem.setPrice(dataController.recalculateReturn(segment,
					getDepartureTimeZone(segment), serviceItem.getPrice(), serviceItem.getPrice().getSource()));
		}
	}
	
	/*
	 * Возвращает сегмент рейса, по переданному сервису.
	 */
	private Segment getSegment(Order order, ServiceItem service) {
		ServiceItem orderService = orderResponseConverter.getOrderService(order, service);
		if (orderService != null
				&& orderService.getSegment() != null
				&& order.getResponse().getSegments() != null) {
			return order.getResponse().getSegments().get(orderService.getSegment().getId());
		}
		return null;
	}
	
	private String getDepartureTimeZone(Segment segment) {
		if (segment != null) {
			return Utils.getLocalityTimeZone(segment.getDeparture().getId());
		}
		return null;
	}

}
