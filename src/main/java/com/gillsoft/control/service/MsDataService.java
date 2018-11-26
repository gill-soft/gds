package com.gillsoft.control.service;

import java.util.List;

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
	 * Возвращает список всех существующих комиссий с родетельскими обїектами, к
	 * которым принадлежит комиссия.
	 * 
	 * @return Список комиссий.
	 */
	public List<Commission> getAllCommissions();
	
}
