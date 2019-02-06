package com.gillsoft.control.service;

import java.util.List;

import com.gillsoft.ms.entity.Commission;
import com.gillsoft.ms.entity.OrderAccess;
import com.gillsoft.ms.entity.Organisation;
import com.gillsoft.ms.entity.Resource;
import com.gillsoft.ms.entity.ReturnCondition;
import com.gillsoft.ms.entity.ServiceFilter;
import com.gillsoft.ms.entity.User;

public interface MsDataService {
	
	/**
	 * Список ресурсов доступных пользователю.
	 * 
	 * @param userName
	 *            Логин (наименование) пользователя.
	 * @return Список ресурсов.
	 */
	public List<Resource> getUserResources(String userName);
	
	/**
	 * Находит и возвращает данные о пользователе по его наименованию.
	 * 
	 * @param userName
	 *            Логин (наименование) пользователя.
	 * @return Найденный пользователь с иерархией организаций, к которым он
	 *         принадлежит.
	 */
	public User getUser(String userName);
	
	/**
	 * Находит и возвращает данные о пользователе по его наименованию.
	 * 
	 * @param id
	 *            Ид пользователя.
	 * @return Найденный пользователь с иерархией организаций, к которым он
	 *         принадлежит.
	 */
	public User getUser(long id);
	
	/**
	 * Возвращает организацию, к которой принадлежит пользователь.
	 * 
	 * @param userName
	 *            Логин (наименование) пользователя.
	 * @return Объект организации.
	 */
	public Organisation getUserOrganisation(String userName);
	
	/**
	 * Возвращает список всех существующих комиссий с родетельскими объектами, к
	 * которым принадлежит комиссия.
	 * 
	 * @return Список комиссий.
	 */
	public List<Commission> getAllCommissions();
	
	/**
	 * Возвращает список всех существующих фильтров с родетельскими объектами, к
	 * которым принадлежит фильтр.
	 * 
	 * @return Список комиссий.
	 */
	public List<ServiceFilter> getAllFilters();
	
	/**
	 * Возвращает список всех существующих условий возвратов с родетельскими
	 * объектами, к которым принадлежит условие.
	 * 
	 * @return Список условий возврата.
	 */
	public List<ReturnCondition> getAllReturnConditions();
	
	/**
	 * Возвращает список всех условий доступов к заказу с родетельскими
	 * объектами, к которым принадлежит условие доступа.
	 * 
	 * @return Список условий доступа.
	 */
	public List<OrderAccess> getAllOrdersAccess();
	
}
