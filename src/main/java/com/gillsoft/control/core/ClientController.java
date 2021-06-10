package com.gillsoft.control.core;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import com.gillsoft.concurrent.PoolType;
import com.gillsoft.concurrent.ThreadPoolStore;
import com.gillsoft.control.core.data.MsDataController;
import com.gillsoft.control.service.ClientAccountService;
import com.gillsoft.control.service.OrderDAOManager;
import com.gillsoft.control.service.model.ClientView;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.OrderClient;
import com.gillsoft.model.Customer;
import com.gillsoft.model.Lang;
import com.gillsoft.ms.entity.Client;
import com.gillsoft.util.StringUtil;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ClientController {
	
	@Autowired
	private ClientAccountService clientService;
	
	@Autowired
	private OrderDAOManager manager;
	
	@Autowired
	private NotificationController notificationController;
	
	@Autowired
	private MsDataController dataController;
	
	public void registerClients(Order order) {
		final Order copy = copy(order);
		String userName = dataController.getUserName();
		ThreadPoolStore.execute(PoolType.ORDER, () -> {
			dataController.signIn(userName);
			for (Entry<String, Customer> customer : copy.getResponse().getCustomers().entrySet()) {
				Client client = register(customer.getValue());
				if (client != null) {
					try {
						manager.addOrderClient(copy, client);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			notificationController.sendNotification(Lang.UA, copy);
		});
	}
	
	public Order copy(Order order) {
		try {
			return StringUtil.jsonStringToObject(Order.class, StringUtil.objectToJsonString(order));
		} catch (IOException e) {
			return (Order) SerializationUtils.deserialize(SerializationUtils.serialize(order));
		}
	}
	
	public ClientView register(Customer customer) {
		ClientView client = getByCustomer(customer);
		if (client != null) {
			return client;
		}
		client = createClient(customer);
		client.setSendValidationCode(false);
		return clientService.register(client);
	}
	
	private ClientView createClient(Customer customer) {
		ClientView client = new ClientView();
		client.setFields(customer);
		return client;
	}
	
	public ClientView getByUser(String clientName) {
		return clientService.getByUser(clientName);
	}
	
	public ClientView getByCustomer(Customer customer) {
		ClientView client = createClient(customer);
		return getByUser(client.getLogin());
	}
	
	public void addClientsToOrder(Order order, Map<String, Customer> customers) {
		updateCustomerPhones(customers.values());
		for (Customer customer : customers.values()) {
			OrderClient orderClient = new OrderClient();
			Client client = getByCustomer(customer);
			if (client != null) {
				orderClient.setClientId(client.getId());
			}
			orderClient.setPhone(customer.getPhone());
			order.addOrderClient(orderClient);
		}
	}
	
	public void updateCustomerPhones(Collection<Customer> customers) {
		customers.forEach(c -> c.setPhone(com.gillsoft.pubsub.util.StringUtil.getCorrectPhone(c.getPhone())));
	}

}
