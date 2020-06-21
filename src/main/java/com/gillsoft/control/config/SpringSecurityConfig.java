package com.gillsoft.control.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import com.gillsoft.security.config.SecurityConfig;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig extends SecurityConfig {
	
	@Bean
	@Override
	protected AuthenticationManager authenticationManager() throws Exception {
		return super.authenticationManager();
	}

}
