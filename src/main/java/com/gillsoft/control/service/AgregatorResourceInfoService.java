package com.gillsoft.control.service;

import java.util.List;

import com.gillsoft.model.request.ResourceRequest;
import com.gillsoft.model.response.ResourceMethodResponse;
import com.gillsoft.model.response.ResourceResponse;

public interface AgregatorResourceInfoService {

	/**
	 * Возвращает информацию о ресурсах указанных в request.
	 * 
	 * @param request
	 *            Запрос получения информации о ресурсе.
	 * @return Информация о ресурсах.
	 */
	public List<ResourceResponse> getInfo(List<ResourceRequest> request);

	/**
	 * Возвращает список доступных методов ресурсов указанных в request.
	 * 
	 * @param request
	 *            Запрос получения информации о ресурсе.
	 * @return Список методов ресурсов.
	 */
	public List<ResourceMethodResponse> getAvailableMethods(List<ResourceRequest> request);

}
