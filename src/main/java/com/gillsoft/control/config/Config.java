package com.gillsoft.control.config;

import java.io.IOException;
import java.util.Properties;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

public class Config {

private static Properties properties;
	
	static {
		try {
			Resource resource = new ClassPathResource("config.properties");
			properties = PropertiesLoaderUtils.loadProperties(resource);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getResourceAgregatorUrl() {
		return properties.getProperty("resuorce.agregator.url");
	}
	
	public static String getSegmentsConnectionUrl() {
		return properties.getProperty("segments.connection.url");
	}
	
	public static String getPrintTicketUrl() {
		return properties.getProperty("print.ticket.url");
	}
	
	public static String getScheduleUrl() {
		return properties.getProperty("schedule.url");
	}
	
	public static String getMsUrl() {
		return properties.getProperty("ms.url");
	}
	
	public static String getMsLogin() {
		return properties.getProperty("ms.login");
	}
	
	public static String getMsPassword() {
		return properties.getProperty("ms.password");
	}
	
	public static int getRequestTimeout() {
		return Integer.valueOf(properties.getProperty("request.timeout", "30000"));
	}
	
}
