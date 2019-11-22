package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.control.api.RequestValidateException;
import com.gillsoft.model.Customer;
import com.gillsoft.model.Method;
import com.gillsoft.model.MethodType;
import com.gillsoft.model.RequiredField;
import com.gillsoft.model.ServiceItem;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.request.TripDetailsRequest;
import com.gillsoft.model.response.RequiredResponse;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class OrderRequestValidator {
	
	@Autowired
	private TripSearchController searchController;
	
	public void validateOrderRequest(OrderRequest request) {
		StringBuilder errorMsg = new StringBuilder();
		
		// проверяем кастомеров
		if (request.getCustomers() == null
				|| request.getCustomers().isEmpty()) {
			errorMsg.append("Empty customers\n");
		}
		// проверяем сервисы
		if (request.getServices() == null
				|| request.getServices().isEmpty()) {
			errorMsg.append("Empty services\n");
		}
		for (int i = 0; i < request.getServices().size(); i++) {
			ServiceItem item = request.getServices().get(i);
			
			// проверяем сегменты
			if (item.getSegment() == null) {
				errorMsg.append("Empty segment. Service index = ").append(i).append("\r\n");
			}
			if (item.getSegment().getId() == null) {
				errorMsg.append("Segment part is present but empty id property. Service index = ").append(i).append("\r\n");
			}
			// проверяем кастомеров
			if (item.getCustomer() == null) {
				errorMsg.append("Empty customer\n");
			}
			if (item.getCustomer().getId() == null) {
				errorMsg.append("Customer part is present but empty id property. Service index = ").append(i).append("\r\n");
			}
			// проверяем места
			if (item.getSeat() != null
					&& item.getSeat().getId() == null) {
				errorMsg.append("Seat part is present but empty id property. Service index = ").append(i).append("\r\n");
			}
			// проверяем тариф
			if (item.getPrice() != null) {
				if (item.getPrice().getTariff() == null) {
					errorMsg.append("Price part is present but empty tariff part. Service index = ").append(i).append("\r\n");
				}
				if (item.getPrice().getTariff().getId() == null) {
					errorMsg.append("Tariff part is present but empty id property. Service index = ").append(i).append("\r\n");
				}
			}
		}
		if (errorMsg.length() > 0) {
			throw new RequestValidateException(errorMsg.toString().trim());
		}
	}
	
	public void validateRequiredFields(OrderRequest request) {
		List<TripDetailsRequest> requests = new ArrayList<>();
		for (ServiceItem item : request.getServices()) {
			String tripId = item.getSegment().getId();
			try {
				requests.add(searchController.createTripDetailsRequest(tripId, null, Method.SEARCH_TRIP_REQUIRED, MethodType.GET));
			} catch (Exception e) {
			}
		}
		Set<String> checked = new HashSet<>();
		StringBuilder errorMsg = new StringBuilder();
		List<RequiredResponse> responses = searchController.getRequiredFields(requests);
		for (RequiredResponse response : responses) {
			if (response.getError() == null
					&& response.getFields() != null) {
				
				// ищем рейс, к которому относится ответ
				for (TripDetailsRequest detailsRequest : requests) {
					if (Objects.equals(response.getId(), detailsRequest.getId())) {
						for (ServiceItem item : request.getServices()) {
							String tripId = new TripIdModel().create(item.getSegment().getId()).getId();
							if (Objects.equals(tripId, detailsRequest.getTripId())) {
								Customer customer = request.getCustomers().get(item.getCustomer().getId());
								if (customer == null) {
									errorMsg.append("Customer ").append(item.getCustomer().getId()).append(" is absent\r\n");
									break;
								}
								// валидируем поля
								for (RequiredField field : response.getFields()) {
									switch (field) {
									case BIRTHDAY:
										if (customer.getBirthday() == null) {
											addError(checked, errorMsg, customer, RequiredField.BIRTHDAY);
										}
										break;
									case CITIZENSHIP:
										if (customer.getCitizenship() == null) {
											addError(checked, errorMsg, customer, RequiredField.CITIZENSHIP);
										}																		
										break;
									case DOCUMENT_NUMBER:
										if (customer.getDocumentNumber() == null) {
											addError(checked, errorMsg, customer, RequiredField.DOCUMENT_NUMBER);
										}
										break;
									case DOCUMENT_SERIES:
										if (customer.getDocumentSeries() == null) {
											addError(checked, errorMsg, customer, RequiredField.DOCUMENT_SERIES);
										}
										break;
									case DOCUMENT_TYPE:
										if (customer.getDocumentType() == null) {
											addError(checked, errorMsg, customer, RequiredField.DOCUMENT_TYPE);
										}
										break;
									case EMAIL:
										if (customer.getEmail() == null) {
											addError(checked, errorMsg, customer, RequiredField.EMAIL);
										}
										break;
									case GENDER:
										if (customer.getGender() == null) {
											addError(checked, errorMsg, customer, RequiredField.GENDER);
										}
										break;
									case NAME:
										if (customer.getName() == null) {
											addError(checked, errorMsg, customer, RequiredField.NAME);
										}
										break;
									case PATRONYMIC:
										if (customer.getPatronymic() == null) {
											addError(checked, errorMsg, customer, RequiredField.PATRONYMIC);
										}
										break;
									case PHONE:
										if (customer.getPhone() == null) {
											addError(checked, errorMsg, customer, RequiredField.PHONE);
										}								
										break;
									case SEAT:
										if (item.getSeat() == null) {
											if (!checked.contains(item.getSegment().getId() + ";" + RequiredField.SEAT)) {
												checked.add(item.getSegment().getId() + ";" + RequiredField.SEAT);
												errorMsg.append("Empty SEAT for segment ").append(item.getSegment().getId()).append("\r\n");
											}
										}
										break;
									case SURNAME:
										if (customer.getSurname() == null) {
											addError(checked, errorMsg, customer, RequiredField.SURNAME);
										}
										break;
									case TARIFF:
										if (item.getPrice() == null) {
											if (!checked.contains(item.getSegment().getId() + ";" + RequiredField.TARIFF)) {
												checked.add(item.getSegment().getId() + ";" + RequiredField.TARIFF);
												errorMsg.append("Empty TARIFF for segment ").append(item.getSegment().getId()).append("\r\n");
											}
										}
										break;
									default:
										break;
									}
								}
								break;
							}
						}
						break;
					}
				}
			}
		}
		if (errorMsg.length() > 0) {
			throw new RequestValidateException(errorMsg.toString().trim());
		}
	}
	
	private void addError(Set<String> checked, StringBuilder errorMsg, Customer customer, RequiredField field) {
		if (!checked.contains(customer.getId() + ";" + field)) {
			checked.add(customer.getId() + ";" + field);
			errorMsg.append("Empty ").append(field.name()).append(" for customer ").append(customer.getId()).append("\r\n");
		}
	}

}
