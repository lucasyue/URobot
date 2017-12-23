package com.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemindLogger {
	static Logger logger = LoggerFactory.getLogger(RemindLogger.class);
	public static void traceMessage(String info){
		logger.trace(info);
	}
}
