package com.gillsoft.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;

@RestController
@RequestMapping("/trip/search")
@Api(tags = { "Trip search" }, produces = "application/json")
public class TripSearchApiController {

}
