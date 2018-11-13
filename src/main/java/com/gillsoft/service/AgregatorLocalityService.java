package com.gillsoft.service;

import java.util.List;

import com.gillsoft.model.request.LocalityRequest;
import com.gillsoft.model.response.LocalityResponse;

public interface AgregatorLocalityService {

	/**
	 * Возвращает список всех пунктов ресурса указанных ресурсов.
	 * 
	 * @param request
	 *            Запрос получения пунктов.
	 * @return Список пунктов.
	 */
	public List<LocalityResponse> getAll(List<LocalityRequest> request);
	
	/**
	 * Возвращает список пунктов ресурса, которые используются для поиска в
	 * указанных ресурсах.
	 * 
	 * @param request
	 *            Запрос получения пунктов.
	 * @return Список пунктов.
	 */
	public List<LocalityResponse> getUsed(List<LocalityRequest> request);
	
	/**
	 * Возвращает привязку пунктов, из которых можно выехать и в которые можно
	 * приехать для ресурсов указынных в request.
	 * 
	 * @param request
	 *            Запрос получения привязки.
	 * @return Привязка пунктов.
	 */
	public List<LocalityResponse> getBinding(List<LocalityRequest> request);
	
}
