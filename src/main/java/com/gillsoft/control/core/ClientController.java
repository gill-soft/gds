package com.gillsoft.control.core;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.control.service.ClientAccountService;
import com.gillsoft.control.service.model.ClientView;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.OrderClient;
import com.gillsoft.model.Customer;
import com.gillsoft.ms.entity.Client;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ClientController {
	
	@Autowired
	private ClientAccountService clientService;
	
	public Client register(Customer customer, List<String> clientNotifications) {
		ClientView client = createClient(customer);
		client.setNotifications(clientNotifications);
		return clientService.register(client);
	}
	
	private ClientView createClient(Customer customer) {
		ClientView client = new ClientView();
		client.setFields(customer);
		return client;
	}
	
	public Client getByUser(String clientName) {
		return clientService.getByUser(clientName);
	}
	
	public Client getByCustomer(Customer customer) {
		ClientView client = createClient(customer);
		return getByUser(client.getLogin());
	}
	
	public void addClientsToOrder(Order order, Map<String, Customer> customers) {
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

}
