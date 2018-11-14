package com.gillsoft.service;

import java.util.List;

import com.gillsoft.entity.Commission;
import com.gillsoft.entity.Organisation;
import com.gillsoft.entity.Resource;
import com.gillsoft.entity.User;
import com.gillsoft.model.Segment;

public interface MsDataService {
	
	public List<Resource> getUserResources(String userName);
	
	public User getUser(String userName);
	
	public Organisation getUserOrganisation(String userName);
	
	public List<Commission> getAllCommissions();
	
	public List<Commission> getCommissions(String userName, Segment tripSegment);

}
