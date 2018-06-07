package com.gillsoft.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gillsoft.model.Method;
import com.gillsoft.model.Resource;
import com.gillsoft.model.request.ResourceRequest;
import com.gillsoft.store.ResourceStore;

@RestController
@RequestMapping(value = "/resource")
public class ResourceController {

	@Autowired
	private ResourceStore store;

	/**
	 * Метод получения информации о ресурсе.
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping
	public Resource getResource(@Validated @RequestBody ResourceRequest request) {
		return store.getResourceService(request.getParams()).getInfo();
	}

	/**
	 * Метод получения списка доступного функционала ресурса.
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/method")
	public List<Method> getMethods(@Validated @RequestBody ResourceRequest request) {
		return store.getResourceService(request.getParams())
				.getAvailableMethods();
	}

}
