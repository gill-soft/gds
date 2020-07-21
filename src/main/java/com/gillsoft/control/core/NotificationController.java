package com.gillsoft.control.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.control.service.model.Order;
import com.gillsoft.model.Customer;
import com.gillsoft.model.Lang;
import com.gillsoft.model.Locality;
import com.gillsoft.model.Segment;
import com.gillsoft.model.ServiceItem;
import com.gillsoft.model.response.OrderResponse;
import com.gillsoft.util.StringUtil;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class NotificationController {
	
	private static Logger LOGGER = LogManager.getLogger(NotificationController.class);
	
	public Map<String, List<String>> createOrderNotifications(Order order) {
		OrderResponse orderResponse = order.getResponse();
		if (orderResponse == null) {
			return null;
		}
		Map<String, String> clientNotifications = new HashMap<>();
		for (ServiceItem service : orderResponse.getServices()) {
			if (service.getError() == null
					&& service.getSegment() != null
					&& service.getCustomer() != null) {
				Customer customer = orderResponse.getCustomers().get(service.getCustomer().getId());
				Segment segment = orderResponse.getSegments().get(service.getSegment().getId());
				if (customer != null
						&& segment != null
						&& !clientNotifications.containsKey(service.getCustomer().getId() + "_" + service.getSegment().getId())) {
					StringBuilder notification = new StringBuilder();
					notification.append("Ви купили билет ")
							.append(getLocalityName(orderResponse, segment.getDeparture(), true))
							.append("-")
							.append(getLocalityName(orderResponse, segment.getArrival(), true))
							.append("-")
							.append(StringUtil.dateFormat.format(segment.getDepartureDate()))
							.append(". ")
							.append("Для регистрации на рейс и покупки обратного билета по сниженным ценам перейдите по ссылке ")
							.append("http://tinyurl.com/ybhsnvvj");
					LOGGER.info(notification);
					clientNotifications.put(service.getCustomer().getId() + "_" + service.getSegment().getId(), notification.toString());
				}
			}
		}
		return clientNotifications.entrySet().stream().collect(Collectors.groupingBy(entry -> entry.getKey().split("_")[0],
				Collectors.mapping(Map.Entry::getValue, Collectors.collectingAndThen(
						Collectors.toList(), list -> list.stream().distinct().collect(Collectors.toList())))));
	}
	
	private String getLocalityName(OrderResponse response, Locality locality, boolean checkParent) {
		if (locality == null
				|| response.getLocalities() == null) {
			return "";
		}
		Locality station = response.getLocalities().get(locality.getId());
		if (station != null) {
			if (checkParent
					&& station.getParent() != null) {
				String cityName = getLocalityName(response, station.getParent(), false);
				if (!cityName.isEmpty()) {
					return cityName;
				}
			}
			return station.getName(Lang.RU);
		}
		return "";
	}

}
