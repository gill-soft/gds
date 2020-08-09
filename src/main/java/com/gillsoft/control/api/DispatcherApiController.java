package com.gillsoft.control.api;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gillsoft.control.core.DispatcherService;
import com.gillsoft.control.service.model.MappedService;
import com.gillsoft.control.service.model.Order;

import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/dispatcher")
@ApiIgnore
public class DispatcherApiController {
	
	@Autowired
	private DispatcherService dispatcherService;
	
	@GetMapping("/services/{tripDepartureFrom}/{tripDepartureTo}")
	public List<MappedService> getMappedServices(@Validated @PathVariable @DateTimeFormat(iso = ISO.DATE) LocalDate tripDepartureFrom,
			@Validated @PathVariable @DateTimeFormat(iso = ISO.DATE) LocalDate tripDepartureTo) {
		return dispatcherService.getMappedServices(Date.from(tripDepartureFrom.atStartOfDay().toInstant(ZoneOffset.UTC)),
				Date.from(tripDepartureTo.atStartOfDay().toInstant(ZoneOffset.UTC)));
	}
	
	@GetMapping("/services/trip/{tripId}/from/{fromId}/{fromDeparture}")
	public List<Order> getFromMappedOrders(@Validated @PathVariable long tripId, @Validated @PathVariable long fromId,
			@Validated @PathVariable @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime fromDeparture) {
		return dispatcherService.getFromMappedOrders(tripId, fromId, Date.from(fromDeparture.toInstant(ZoneOffset.UTC)));
	}
	
	@GetMapping("/services/trip/{tripId}/to/{toId}/{toDeparture}")
	public List<Order> getToMappedOrders(@Validated @PathVariable long tripId, @Validated @PathVariable long toId,
			@Validated @PathVariable @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime toDeparture) {
		return dispatcherService.getToMappedOrders(tripId, toId, Date.from(toDeparture.toInstant(ZoneOffset.UTC)));
	}
	
	@GetMapping("/services/trip/{tripId}/{departure}")
	public List<Order> getTripMappedOrders(@Validated @PathVariable long tripId,
			@Validated @PathVariable @DateTimeFormat(iso = ISO.DATE) LocalDate departure) {
		return dispatcherService.getTripMappedOrders(tripId, Date.from(departure.atStartOfDay().toInstant(ZoneOffset.UTC)));
	}

}
