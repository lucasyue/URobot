package com.urule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

import com.bstek.urule.Utils;
import com.bstek.urule.runtime.service.KnowledgeService;

@SpringBootApplication
@ImportResource({"classpath:urule-console-context.xml"})
public class URuleApp {
	public static void main(String[] args) {
		KnowledgeService service = (KnowledgeService) Utils.getApplicationContext().getBean(KnowledgeService.BEAN_ID);
		
		SpringApplication.run(URuleApp.class,args);
	}
}