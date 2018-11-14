package com.gillsoft.core;

import org.apache.logging.log4j.Logger;

import com.gillsoft.model.response.Response;

public class Utils {

	private Utils() {
		
	}
	
	public static boolean isError(Logger logger, Response response) {
		if (response.getError() == null) {
			return false;
		}
		logger.error("Response id: " + response.getId()
				+ " Get locations from resource error: " + response.getError().getName(),
				new Exception(response.getError().getMessage()));
		return true;
	}

}
