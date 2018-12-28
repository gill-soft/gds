package com.gillsoft.control.service;

import java.util.List;

import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.Status;
import com.gillsoft.ms.entity.ReturnCondition;
import com.gillsoft.ms.entity.Commission;
import com.gillsoft.ms.entity.Organisation;
import com.gillsoft.ms.entity.Resource;
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
	 * Возвращает список всех существующих условий возвратов с родетельскими
	 * объектами, к которым принадлежит условие.
	 * 
	 * @return Список условий возврата.
	 */
	public List<ReturnCondition> getAllReturnConditions();
	
	/**
	 * Проверяет доступна ли операция по переводу позиций заказа в указанный
	 * статус newStatus для текущего пользователя. Если newStatus = null, то
	 * проверяется доступен ли заказ пользователю для поиска.
	 * 
	 * @param order
	 *            Заказ.
	 * @param newStatus
	 *            Статус, в который необходимо перевести заказ.
	 * @return true - если операция доступна текущему пользователю.
	 */
	public boolean isOrderAvailable(Order order, Status newStatus);
	
}
