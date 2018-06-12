package com.gillsoft.core;

import java.rmi.AccessException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.core.store.ResourceStore;
import com.gillsoft.model.request.ResourceRequest;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ResourceActivity {
	
	@Autowired
	private ResourceStore store;
	
	private ConcurrentMap<ResourceRequest, Activity> activities = new ConcurrentHashMap<>();
	
	private ResourceActivity() {
		
	}

	public void check(ResourceRequest request) throws AccessException {
		Activity activity = activities.get(request);
		if (activity == null ||
				checkTime(activity)) {
			activity = updateActivity(request);
		}
		if (!activity.isActiv()) {
			throw createAccessException(request); 
		}
	}
	
	private boolean checkTime(Activity activity) {
		return System.currentTimeMillis() - activity.getLastCheck().getTime() > 30000;
	}
	
	private Activity updateActivity(ResourceRequest request) {
		boolean activ = store.getResourceService(request.getParams()).isAvailable();
		Activity activity = new Activity(activ);
		activities.put(request, activity);
		return activity;
	}
	
	public AccessException createAccessException(ResourceRequest request) {
		return new AccessException("Resource is unavaliable");
	}
	
	private class Activity {
		
		private Date lastCheck = new Date();
		
		private boolean activ;

		private Activity(boolean activ) {
			this.activ = activ;
		}

		public Date getLastCheck() {
			return lastCheck;
		}

		public boolean isActiv() {
			return activ;
		}

	}
	
}
