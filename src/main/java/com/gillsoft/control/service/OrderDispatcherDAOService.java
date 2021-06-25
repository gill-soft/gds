package com.gillsoft.control.service;

import java.util.Date;
import java.util.List;

import com.gillsoft.control.service.model.ManageException;
import com.gillsoft.control.service.model.MappedService;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.TripDateServices;

public interface OrderDispatcherDAOService {
	
	/**
	 * Возвращает список смапленных сервисов на рейды, в которых они участвуют.
	 * 
	 * @param carrierId
	 *            Ид перевозчика.
	 * @param tripDepartureFrom
	 *            Дата отправления (начало выборки).
	 * @param tripDepartureTo
	 *            Дата отправления (конец выборки).
	 * @return Список смапленных сервисов.
	 * @throws ManageException
	 */
	public List<MappedService> getMappedServices(long carrierId, Date tripDepartureFrom, Date tripDepartureTo) throws ManageException;
	
	/**
	 * Возвращает список рейдов с колличеством пассажиров по каждому рейду.
	 * 
	 * @param carrierId
	 *            Ид перевозчика.
	 * @param tripDepartureFrom
	 *            Дата отправления (начало выборки).
	 * @param tripDepartureTo
	 *            Дата отправления (конец выборки).
	 * @return Список рейдов с колличеством пассажиров.
	 * @throws ManageException
	 */
	public List<TripDateServices> getGroupedServices(long carrierId, Date tripDepartureFrom, Date tripDepartureTo) throws ManageException;
	
	/**
	 * Возвращает список заказов, которые участвуют в указанном рейсе и пункте
	 * отправления на период дат отправления.
	 * 
	 * @param carrierId
	 *            Ид перевозчика.
	 * @param tripId
	 *            Ид рейса.
	 * @param fromId
	 *            Ид пункта отправления.
	 * @param fromDepartureFrom
	 *            Дата отправления (начало выборки).
	 * @param fromDepartureTo
	 *            Дата отправления (конец выборки).
	 * @return Список заказов.
	 * @throws ManageException
	 */
	public List<Order> getFromMappedOrders(long carrierId, long tripId, long fromId, Date fromDepartureFrom, Date fromDepartureTo) throws ManageException;
	
	/**
	 * Возвращает список заказов, которые участвуют в указанном рейсе и пункте
	 * прибытия на период дат прибытия.
	 * 
	 * @param carrierId
	 *            Ид перевозчика.
	 * @param tripId
	 *            Ид рейса.
	 * @param fromId
	 *            Ид пункта отправления.
	 * @param toArrivalFrom
	 *            Дата прибытия (начало выборки).
	 * @param toArrivalTo
	 *            Дата прибытия (конец выборки).
	 * @return Список заказов.
	 * @throws ManageException
	 */
	public List<Order> getToMappedOrders(long carrierId, long tripId, long toId, Date toArrivalFrom, Date toArrivalTo) throws ManageException;
	
	/**
	 * Возвращает список заказов, которые участвуют в указанном рейде.
	 * 
	 * @param carrierId
	 *            Ид перевозчика.
	 * @param tripId
	 *            Ид рейса.
	 * @param departure
	 *            Дата рейда.
	 * @return Список заказов.
	 * @throws ManageException
	 */
	public List<Order> getTripMappedOrders(long carrierId, long tripId, Date departure) throws ManageException;

}
