package com.gillsoft.control.service;

public interface AgregatorService {
	
	/**
	 * Возвращает сервис получения информации о ресурсе.
	 * 
	 * @return Сервис.
	 */
	public AgregatorResourceInfoService getResourceInfoService();
	
	/**
	 * Возвращает сервис работы с пунктами ресурса.
	 * 
	 * @return Сервис.
	 */
	public AgregatorLocalityService getLocalityService();

	/**
	 * Возвращает поиска инвентаря ресурса.
	 * 
	 * @return Сервис.
	 */
	public AgregatorTripSearchService getSearchService();

	/**
	 * Возвращает сервис работы с заказами в ресурсе.
	 * 
	 * @return Сервис.
	 */
	public AgregatorOrderService getOrderService();

	/**
	 * Возвращает сервис работы с дополнительными функциями ресурса.
	 * 
	 * @return Сервис.
	 */
	public AgregatorAdditionalSearchService getAdditionalService();
	
	
	/**
	 * Возвращает сервис работы с функциями расписания ресурса.
	 * 
	 * @return Сервис.
	 */
	public AgregatorScheduleService getScheduleService();

}
