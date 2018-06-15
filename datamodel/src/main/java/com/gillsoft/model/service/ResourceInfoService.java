package com.gillsoft.model.service;

import java.util.List;

import com.gillsoft.model.Method;
import com.gillsoft.model.Ping;
import com.gillsoft.model.Resource;

public interface ResourceInfoService {

	/**
	 * Проверка доступности ресурса.
	 * 
	 * @param id
	 *            ИД проверки.
	 * @return Элемент проверки содержащий id как подтверждение доступности.
	 */
	public Ping ping(String id);

	/**
	 * Возвращает информацию о ресурсе.
	 * 
	 * @return Информация о ресурсе.
	 */
	public Resource getInfo();

	/**
	 * Возвращает список доступных методов ресурса.
	 * 
	 * @return Список методов.
	 */
	public List<Method> getAvailableMethods();

}
