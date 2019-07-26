package com.gillsoft.control.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.gillsoft.security.WhiteList;

public class CustomWhiteList extends WhiteList {
	
	private static final String[] AUTH_WHITELIST = {
            "/**/order/print",
            "/**/generate/**"
    };
	
	@Override
	public String[] getMethods() {
		List<String> methodsList = new ArrayList<>();
		methodsList.addAll(Arrays.asList(super.getMethods()));
		methodsList.addAll(Arrays.asList(AUTH_WHITELIST));
		return methodsList.toArray(new String[] {});
	}

}
