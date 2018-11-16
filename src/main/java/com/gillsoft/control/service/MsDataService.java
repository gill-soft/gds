package com.gillsoft.control.service;

import java.util.List;

import com.gillsoft.ms.entity.Commission;
import com.gillsoft.ms.entity.Organisation;
import com.gillsoft.ms.entity.Resource;
import com.gillsoft.ms.entity.User;
import com.gillsoft.model.Segment;

public interface MsDataService {
	
	public List<Resource> getUserResources(String userName);
	
	public User getUser(String userName);
	
	public Organisation getUserOrganisation(String userName);
	
	public List<Commission> getAllCommissions();
	
	public List<Commission> getCommissions(String userName, Segment tripSegment);

}
