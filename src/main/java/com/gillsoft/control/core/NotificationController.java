package com.gillsoft.control.core;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.concurrent.PoolType;
import com.gillsoft.concurrent.ThreadPoolStore;
import com.gillsoft.control.core.data.MsDataController;
import com.gillsoft.control.service.model.ClientView;
import com.gillsoft.control.service.model.NotificationView;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.model.Customer;
import com.gillsoft.model.Lang;
import com.gillsoft.model.Locality;
import com.gillsoft.model.RoutePoint;
import com.gillsoft.model.Segment;
import com.gillsoft.model.ServiceItem;
import com.gillsoft.model.response.OrderResponse;
import com.gillsoft.ms.entity.NotificationType;
import com.gillsoft.pubsub.model.Const;
import com.gillsoft.pubsub.model.Recipient;
import com.gillsoft.pubsub.service.SimpleNotificationService;
import com.gillsoft.pubsub.util.StringUtil;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class NotificationController {
	
	private static Logger LOGGER = LogManager.getLogger(NotificationController.class);
	
	@Autowired
	private SimpleNotificationService sender;
	
	@Autowired
	private MsDataController dataController;
	
	@Autowired
	private ClientController clientController;
	
	public void sendNotification(Lang lang, Order order) {
		OrderResponse orderResponse = order.getResponse();
		if (orderResponse != null) {
			updateOrder(orderResponse);
			Set<String> clientNotifications = new HashSet<>();
			for (ServiceItem service : orderResponse.getServices()) {
				if (service.getError() == null
						&& service.getSegment() != null
						&& service.getCustomer() != null) {
					Customer customer = service.getCustomer();
					Segment segment = service.getSegment();
					if (customer != null
							&& segment != null
							&& !clientNotifications.contains(service.getCustomer().getId() + "_" + service.getSegment().getId())) {
						clientNotifications.add(service.getCustomer().getId() + "_" + service.getSegment().getId());
						List<NotificationView> notifications = dataController.getNotifications(NotificationType.BUY_TICKETS, segment);
						if (notifications != null
								&& !notifications.isEmpty()) {
							for (NotificationView notification : notifications) {
								ThreadPoolStore.execute(PoolType.ORDER, () -> {
									ClientView client = clientController.getByCustomer(customer);
									if (client != null) {
										try {
											sender.sendMessage(Collections.singletonList(createRecipient(notification.getChannels().iterator(), client)),
													notification.getDescription(lang), service, lang);
										} catch (IOException e) {
											LOGGER.error("Can not send message", e);
										}
									}
								});
							}
						}
					}
				}
			}
		}
	}
	
	private Recipient createRecipient(Iterator<String> chanel, ClientView client) {
		Recipient recipient = new Recipient();
		String chanelCode = chanel.next();
		recipient.setCode(chanelCode);
		setParams(recipient, chanelCode, client);
		if (chanel.hasNext()) {
			recipient.setRecipient(createRecipient(chanel, client));
		}
		return recipient;
	}
	
	private void setParams(Recipient recipient, String chanel, ClientView client) {
		recipient.setParams(new HashMap<>());
		switch (chanel) {
		case Const.CHANNEL_EMAIL:
			recipient.getParams().put(Const.PARAM_EMAIL, client.getEmail());
			break;
		case Const.CHANNEL_FCM:
			recipient.getParams().put(Const.PARAM_APP_TOKEN, client.getAppUserToken());
			break;
		case Const.CHANNEL_SMS:
		case Const.CHANNEL_VIBER:
		case Const.CHANNEL_VIBER_BOT:
		case Const.CHANNEL_VIBER_DRIVER_BOT:
			recipient.getParams().put(Const.PARAM_PHONE, StringUtil.getCorrectPhone(client.getPhone()));
			break;
		default:
			break;
		}
	}
	
	private void updateOrder(OrderResponse order) {
        if (order.getLocalities() != null) {
            for (Map.Entry<String, Locality> entry : order.getLocalities().entrySet()) {
                Locality locality = entry.getValue();
                locality.setId(entry.getKey());
                if (locality.getParent() != null) {
                    if (order.getLocalities().containsKey(locality.getParent().getId())) {
                        locality.setParent(order.getLocalities().get(locality.getParent().getId()));
                    }
                }
            }
        }
        if (order.getAdditionalServices() != null) {
            order.getAdditionalServices().forEach((id, as) -> as.setId(id));
        }
        if (order.getOrganisations() != null) {
            order.getOrganisations().forEach((id, o) -> o.setId(id));
        }
        if (order.getSegments() != null) {
            for (Map.Entry<String, Segment> entry : order.getSegments().entrySet()) {
                Segment segment = entry.getValue();
                segment.setId(entry.getKey());
                if (segment.getFreeSeatsCount() == null) {
                    segment.setFreeSeatsCount(50);
                }
                if (order.getOrganisations() != null) {

                    if (segment.getCarrier() != null) {
                        segment.setCarrier(order.getOrganisations().get(segment.getCarrier().getId()));
                    }
                    if (segment.getInsurance() != null) {
                        segment.setInsurance(order.getOrganisations().get(segment.getInsurance().getId()));
                    }
                }
                if (order.getLocalities() != null) {
                    if (segment.getRoute() != null) {
                        for (RoutePoint point : segment.getRoute().getPath()) {
                            point.setLocality(order.getLocalities().get(point.getLocality().getId()));
                        }
                    }
                    if (segment.getDeparture() != null) {
                        segment.setDeparture(order.getLocalities().get(segment.getDeparture().getId()));
                    }
                    if (segment.getArrival() != null) {
                        segment.setArrival(order.getLocalities().get(segment.getArrival().getId()));
                    }
                }
                if (order.getVehicles() != null
                        && segment.getVehicle() != null) {
                    segment.setVehicle(order.getVehicles().get(segment.getVehicle().getId()));
                }
                if (order.getAdditionalServices() != null
                        && segment.getAdditionalServices() != null) {
                    segment.setAdditionalServices(segment.getAdditionalServices().stream()
                            .map(as -> order.getAdditionalServices().get(as.getId())).collect(Collectors.toList()));
                }
            }
        }
        if (order.getCustomers() != null) {
            order.getCustomers().forEach((id, c) -> c.setId(id));
        }
        if (order.getServices() != null) {
            for (ServiceItem service : order.getServices()) {
                if (service.getSegment() != null) {
                    service.setSegment(order.getSegments().get(service.getSegment().getId()));
                }
                if (service.getCustomer() != null) {
                    service.setCustomer(order.getCustomers().get(service.getCustomer().getId()));
                }
                if (service.getAdditionalService() != null) {
                    service.setAdditionalService(order.getAdditionalServices().get(service.getAdditionalService().getId()));
                }
            }
        }
    }
	
}
