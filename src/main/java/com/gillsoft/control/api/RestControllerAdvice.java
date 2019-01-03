package com.gillsoft.control.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.gillsoft.model.RestError;

@ControllerAdvice
@RestController
public class RestControllerAdvice {
	
	private static Logger LOGGER = LogManager.getLogger(RestControllerAdvice.class);
	
	@ExceptionHandler(NoDataFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ResponseBody
	public RestError validateExceptions(NoDataFoundException e) {
		LOGGER.error("No data found exception: " + e.getMessage());
		return e.createRestError();
	}
	
	@ExceptionHandler(OperationLockedException.class)
	@ResponseStatus(HttpStatus.LOCKED)
	@ResponseBody
	public RestError lockExceptions(OperationLockedException e) {
		LOGGER.error("Operation locked exception: " + e.getMessage());
		return e.createRestError();
	}
	
	@ExceptionHandler(RequestValidateException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public RestError validateExceptions(RequestValidateException e) {
		LOGGER.error("Validate exception: " + e.getMessage());
		return e.createRestError();
	}
	
	@ExceptionHandler(MethodUnavalaibleException.class)
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	@ResponseBody
	public RestError unavalaibleExceptions(MethodUnavalaibleException e) {
		LOGGER.error("Method unavalaible exception: " + e.getMessage());
		return e.createRestError();
	}
	
	@ExceptionHandler(ResourceUnavailableException.class)
	@ResponseStatus(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS)
	@ResponseBody
	public RestError resourceExceptions(ResourceUnavailableException e) {
		LOGGER.error("Resource unavalaible exception: " + e.getMessage());
		return e.createRestError();
	}
	
	@ExceptionHandler(ApiException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public RestError apiExceptions(ApiException e) {
		LOGGER.error("Api exception: " + e.getMessage());
		return e.createRestError();
	}
	
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public RestError allExceptions(Exception e) {
		LOGGER.error("Uncatchable error", e);
		return new RestError(e.getClass().getName(), e.getMessage());
	}

}
