package com.gillsoft.control.api;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.gillsoft.control.service.model.PrintOrderWrapper;

@Controller
public class PrintController {

	@PostMapping("/order/print")
	public String printOrder(@RequestBody PrintOrderWrapper orderWrapper, Model model) {
		model.addAttribute("order", orderWrapper.getOrder());
		return orderWrapper.getTicketLayout();
	}
	
}
