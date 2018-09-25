package com.gillsoft.model.service;

import com.gillsoft.model.request.ResourceParams;

public interface ResourceService extends ResourceInfoService {

	/**
	 * Проверка доступности сервиса.
	 * 
	 * @return Результат.
	 */
	public boolean isAvailable();

	/**
	 * Создает подключение к ресурсу по полученным параметрам и инициализирует
	 * сервис.
	 * 
	 * @param params
	 *            Параметры подключения к ресурсу.
	 */
	public void applayParams(ResourceParams params);

	/**
	 * Возвращает сервис работы с пунктами ресурса.
	 * 
	 * @return Сервис.
	 */
	public LocalityService getLocalityService();

	/**
	 * Возвращает поиска инвентаря ресурса.
	 * 
	 * @return Сервис.
	 */
	public TripSearchService getSearchService();

	/**
	 * Возвращает сервис работы с заказами в ресурсе.
	 * 
	 * @return Сервис.
	 */
	public OrderService getOrderService();

	/**
	 * Возвращает сервис работы с дополнительными функциями ресурса.
	 * 
	 * @return Сервис.
	 */
	public AdditionalService getAdditionalService();
	
	
	/**
	 * Возвращает сервис работы с функциями расписания ресурса.
	 * 
	 * @return Сервис.
	 */
	public ScheduleService getScheduleService();

}
