package com.gillsoft.control.api;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.gillsoft.control.service.model.PrintOrderWrapper;
import com.gillsoft.model.response.OrderResponse;
import com.gillsoft.util.StringUtil;
import com.ibm.icu.text.Transliterator;

@Controller
public class PrintController {

	@PostMapping("/order/print")
	public String printOrder(@RequestBody PrintOrderWrapper orderWrapper, Model model) {
		model.addAttribute("order", orderWrapper.getOrder());
		try {
			model.addAttribute("translitOrder", transform(orderWrapper.getOrder()));
		} catch (IOException e) {
		}
		return orderWrapper.getTicketLayout();
	}
	
	private OrderResponse transform(OrderResponse order) throws IOException {
		String value = StringUtil.objectToJsonString(order);
		String[] list = { "Ukrainian-Latin/BGN", "Russian-Latin/BGN" };
		for (String s : list) {
			Transliterator trans = Transliterator.getInstance(s);
			value = trans.transform(value);
		}
		return StringUtil.jsonStringToObject(OrderResponse.class, value);
	}
	
}
