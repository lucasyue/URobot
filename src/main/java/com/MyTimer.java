package com;

import java.util.Date;

public class MyTimer {
	private Date begin;
	private int secondsTimeout;

	public MyTimer() {
		this.secondsTimeout = 30;
		this.begin = new Date();
	}

	public MyTimer(int secondsTimeout) {
		this.secondsTimeout = secondsTimeout;
		this.begin = new Date();
	}

	public boolean isTimeout() {
		Date now = new Date();
		long times = now.getTime() - begin.getTime();
		if (times * 1000 > secondsTimeout) {
			begin = new Date();
			return true;
		}
		return false;
	}

	public void restart() {
		begin = new Date();
	}
}
