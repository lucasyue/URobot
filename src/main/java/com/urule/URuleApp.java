package com.urule;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ImportResource;

//@SpringBootApplication
@ImportResource({"classpath:urule-console-context.xml"})
public class URuleApp {
	public static void main(String[] args) {
		SpringApplication.run(URuleApp.class,args);
	}
}