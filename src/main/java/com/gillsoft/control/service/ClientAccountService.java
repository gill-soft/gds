package com.gillsoft.control.service;

import com.gillsoft.control.service.model.ClientView;

public interface ClientAccountService {
	
	public ClientView register(ClientView client);
	
	public ClientView getByUser(String clientName);

}
