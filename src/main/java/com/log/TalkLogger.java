package com.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TalkLogger {
	static Logger logger = LoggerFactory.getLogger(TalkLogger.class);
	public static void traceMessage(String info){
		logger.trace(info);
	}
}
