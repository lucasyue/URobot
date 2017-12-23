package com.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scienjus.smartqq.model.GroupMessage;
import com.scienjus.smartqq.model.GroupUser;

public class TraceLogger {
	static Logger logger = LoggerFactory.getLogger(TraceLogger.class);
	public static void traceMessage(GroupUser gUser, GroupMessage msg){
		long uin = gUser.getUin();
		String content = msg.getContent();
		String nick = gUser.getNick();
		String card = gUser.getCard();
		String info = card+"," +nick + ">" + content + "," + msg.getUserId() + ","+ uin;
		logger.trace(info);
	}
}
