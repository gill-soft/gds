package com.gillsoft.control.service;

import java.util.List;

import com.gillsoft.ms.entity.Attribute;
import com.gillsoft.ms.entity.Commission;
import com.gillsoft.ms.entity.ConnectionDiscount;
import com.gillsoft.ms.entity.OrderAccess;
import com.gillsoft.ms.entity.Organisation;
import com.gillsoft.ms.entity.Resource;
import com.gillsoft.ms.entity.ResourceConnection;
import com.gillsoft.ms.entity.ResourceFilter;
import com.gillsoft.ms.entity.ResourceParams;
import com.gillsoft.ms.entity.ReturnCondition;
import com.gillsoft.ms.entity.ServiceFilter;
import com.gillsoft.ms.entity.TicketLayout;
import com.gillsoft.ms.entity.Trip;
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
	 * Возвращает организацию.
	 * 
	 * @param id
	 *            Ид организации.
	 * @return Объект организации.
	 */
	public Organisation getOrganisation(long id);
	
	/**
	 * Возвращает список всех организаций.
	 * 
	 * @return Список объектов организации.
	 */
	public List<Organisation> getAllOrganisations();
	
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
	 * Возвращает список всех существующих макетов билетов с родетельскими
	 * объектами, к которым принадлежит макет.
	 * 
	 * @return Список макетов билетов.
	 */
	public List<TicketLayout> getAllTicketLayouts();
	
	/**
	 * Возвращает список всех условий доступов к заказу с родетельскими
	 * объектами, к которым принадлежит условие доступа.
	 * 
	 * @return Список условий доступа.
	 */
	public List<OrderAccess> getAllOrdersAccess();
	
	/**
	 * Возвращает список всех условий фильтрации ресурсов с родетельскими
	 * объектами, к которым принадлежит условие.
	 * 
	 * @return Список условий фильтрации ресурсов.
	 */
	public List<ResourceFilter> getAllResourceFilters();
	
	/**
	 * Возвращает список всех условий стыковок ресурсов с родетельскими
	 * объектами, к которым принадлежит условие.
	 * 
	 * @return Список условий стыковок ресурсов.
	 */
	public List<ResourceConnection> getAllResourceConnections();
	
	/**
	 * Возвращает список всех скидок на стыковки между ресурсами с родетельскими
	 * объектами, к которым принадлежит скидка.
	 * 
	 * @return Список скидок на стыковки ресурсов.
	 */
	public List<ConnectionDiscount> getAllResourceConnectionDiscounts();
	
	/**
	 * Возвращает список всех параметров доступа к ресурсам заказов с
	 * родетельскими объектами, к которым принадлежит параметр.
	 * 
	 * @return Список параметров доступа к ресурсам заказов.
	 */
	public List<ResourceParams> getAllResourceParamsWithParent();
	
	/**
	 * Возвращает список всех возможных аттрибутов.
	 * 
	 * @return Список возможных аттрибутов.
	 */
	public List<Attribute> getAllAttributes();
	
	/**
	 * Возвращает по указанному ид с дочерними элементами.
	 * 
	 * @return Рейс с дочерними элементами.
	 */
	public Trip getTripWithChilds(long id);
	
}
