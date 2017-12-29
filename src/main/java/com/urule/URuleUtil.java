package com.urule;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.bstek.urule.Utils;
import com.bstek.urule.runtime.KnowledgePackage;
import com.bstek.urule.runtime.KnowledgeSession;
import com.bstek.urule.runtime.KnowledgeSessionFactory;
import com.bstek.urule.runtime.service.KnowledgeService;

public class URuleUtil {
	public static Map<String,Object> getAnswer(Map<String,Object> params){
		KnowledgeService service = (KnowledgeService) Utils.getApplicationContext().getBean(KnowledgeService.BEAN_ID);
		KnowledgePackage knPackage;
		try {
			knPackage = service.getKnowledge("utalk/test");
			KnowledgeSession session = KnowledgeSessionFactory.newKnowledgeSession(knPackage);
			session.insert(params);
			session.fireRules();
			return session.getParameters();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new HashMap<>();
	}
}
