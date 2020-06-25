package com.gillsoft.control.service;

import com.gillsoft.ms.entity.Client;

public interface ClientAccountService {
	
	public Client register(Client client);
	
	public Client getByUser(String clientName);

}
