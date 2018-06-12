package com.gillsoft.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.gillsoft.model.RestException;

@ControllerAdvice
@RestController
public class RestControllerAdvice {
	
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public RestException allExceptions(Exception e) {
		e.printStackTrace();
		return new RestException(e.getClass().getName(), e.getMessage());
	}

}
