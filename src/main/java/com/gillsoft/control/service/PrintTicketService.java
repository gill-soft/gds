package com.gillsoft.control.service;

import java.util.List;

import com.gillsoft.control.service.model.PrintOrderWrapper;
import com.gillsoft.model.Document;

public interface PrintTicketService {
	
	public List<Document> create(PrintOrderWrapper orderWrapper);

}
