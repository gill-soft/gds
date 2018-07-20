package com.gillsoft.logging;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import com.gillsoft.util.StringUtil;

public abstract class RequestResponseLoggingInterceptor implements ClientHttpRequestInterceptor {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private Charset charset;
	
	public RequestResponseLoggingInterceptor() {
		this.charset = StandardCharsets.UTF_8;
	}

	public RequestResponseLoggingInterceptor(Charset charset) {
		this.charset = charset;
	}
	
	public abstract ClientHttpResponse execute(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException;

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		String id = StringUtil.generateUUID();
		logRequest(id, request, body);
		ClientHttpResponse response = execute(request, body, execution);
		logResponse(id, response);
		return response;
	}

	private void logRequest(String id, HttpRequest request, byte[] body) throws IOException {
		LOGGER.info(new StringBuilder().append("\n")
				.append("Exchange id  : ").append(id).append("\n")
				.append("URI          : ").append(request.getURI()).append("\n")
				.append("Method       : ").append(request.getMethod()).append("\n")
				.append("Headers      : ").append(request.getHeaders()).append("\n")
				.append("Request body : ").append(new String(body, charset)).append("\n").toString());
	}

	private void logResponse(String id, ClientHttpResponse response) throws IOException {
		LOGGER.info(new StringBuilder().append("\n")
				.append("Exchange id  : ").append(id).append("\n")
				.append("Status code  : ").append(response.getStatusCode()).append("\n")
				.append("Status text  : ").append(response.getStatusText()).append("\n")
				.append("Headers      : ").append(response.getHeaders()).append("\n")
				.append("Response body: ").append(
						StreamUtils.copyToString(response.getBody(), charset)).append("\n").toString());
	}

}
