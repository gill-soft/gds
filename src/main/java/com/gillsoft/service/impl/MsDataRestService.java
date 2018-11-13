package com.gillsoft.service.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.gillsoft.config.Config;
import com.gillsoft.entity.Commission;
import com.gillsoft.entity.Resource;
import com.gillsoft.entity.User;
import com.gillsoft.model.Segment;
import com.gillsoft.service.MsDataService;

@Service
public class MsDataRestService extends AbstractRestService implements MsDataService {
	
	private static Logger LOGGER = LogManager.getLogger(MsDataRestService.class);
	
	private static Object synch = new Object();
	
	private RestTemplate template;
	
	@Autowired
	@Qualifier("msAuthHeader")
	private HttpHeaders msAuth;

	@Override
	public List<Resource> getUserResources(String userName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User getUser(String userName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Commission> getAllCommissions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Commission> getCommissions(String userName, Segment tripSegment) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	@Override
	public RestTemplate getTemplate() {
		if (template == null) {
			synchronized (synch) {
				if (template == null) {
					template = createTemplate(Config.getMsUrl());
				}
			}
		}
		return template;
	}

}
