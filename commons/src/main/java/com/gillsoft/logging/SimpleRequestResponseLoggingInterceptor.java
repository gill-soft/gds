package com.gillsoft.logging;

import java.io.IOException;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

public class SimpleRequestResponseLoggingInterceptor extends RequestResponseLoggingInterceptor {

	@Override
	public ClientHttpResponse execute(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		return execution.execute(request, body);
	}

}
