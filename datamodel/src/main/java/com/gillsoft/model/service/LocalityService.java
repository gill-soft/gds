package com.gillsoft.model.service;

import java.util.List;
import java.util.Map;

import com.gillsoft.model.Locality;
import com.gillsoft.model.request.LocalityRequest;

public interface LocalityService {

	/**
	 * Возвращает список всех пунктов ресурса.
	 * 
	 * @param request
	 *            Запрос получения пунктов.
	 * @return Список пунктов.
	 */
	public List<Locality> getAll(LocalityRequest request);
	
	/**
	 * Возвращает список пунктов ресурса, которые используются для поиска.
	 * 
	 * @param request
	 *            Запрос получения пунктов.
	 * @return Список пунктов.
	 */
	public List<Locality> getUsed(LocalityRequest request);
	
	/**
	 * Возвращает привязку пунктов, из которых можно выехать и в которые можно
	 * приехать.
	 * 
	 * @param request
	 *            Запрос получения привязки.
	 * @return Привязка пунктов.
	 */
	public Map<String, List<String>> getBinding(LocalityRequest request);
	
}
