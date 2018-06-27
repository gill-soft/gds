package com.gillsoft.logging;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.gillsoft.util.StringUtil;

public class RequestResponseLoggingFilter implements Filter {
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	@Override
	public void destroy() {
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {

		ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper((HttpServletRequest) request);
		ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper((HttpServletResponse) response);
		
		filterChain.doFilter(requestWrapper, responseWrapper);
		
		String id = StringUtil.generateUUID();
		logRequest(id, requestWrapper);
		
		byte[] body = responseWrapper.getContentAsByteArray();
		responseWrapper.copyBodyToResponse();
		logResponse(id, body, (HttpServletResponse) response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
	}
	
	private void logRequest(String id, ContentCachingRequestWrapper requestWrapper) {
		Map<String, List<String>> headers = Collections.list(
				requestWrapper.getHeaderNames()).stream().collect(
				Collectors.toMap(name -> name, (name) -> Collections.list(requestWrapper.getHeaders((String) name))));
		LOGGER.info(new StringBuilder().append("\n")
				.append("Exchange id  : ").append(id).append("\n")
				.append("URI          : ").append(requestWrapper.getRequestURI()).append("\n")
				.append("Method       : ").append(requestWrapper.getMethod()).append("\n")
				.append("Headers      : ").append(getHeaders(headers)).append("\n")
				.append("Request body : ").append(new String(
						requestWrapper.getContentAsByteArray(), Charset.defaultCharset())).append("\n").toString());
	}
	
	private void logResponse(String id, byte[] body, HttpServletResponse response) {
		Map<String, List<String>> headers = response.getHeaderNames().stream().collect(
				Collectors.toMap(name -> name, (name) -> new ArrayList<String>(response.getHeaders((String) name))));
		LOGGER.info(new StringBuilder().append("\n")
				.append("Exchange id  : ").append(id).append("\n")
				.append("Status code  : ").append(response.getStatus()).append("\n")
				.append("Status text  : ").append(HttpStatus.valueOf(response.getStatus()).getReasonPhrase()).append("\n")
				.append("Headers      : ").append(getHeaders(headers)).append("\n")
				.append("Response body: ").append(new String(body, Charset.defaultCharset())).append("\n").toString());
	}
	
	private HttpHeaders getHeaders(Map<String, List<String>> headers) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.putAll(headers);
		return HttpHeaders.readOnlyHttpHeaders(httpHeaders);
	}

}
