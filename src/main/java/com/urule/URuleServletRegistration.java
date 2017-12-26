package com.urule;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.bstek.urule.console.servlet.URuleServlet;

@Component
public class URuleServletRegistration {
	@Bean
	public ServletRegistrationBean registerURuleServlet(){
		return new ServletRegistrationBean(new URuleServlet(),"/urule/*");
	}
}