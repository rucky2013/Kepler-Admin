package com.kepler.admin.domain;

import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.kepler.KeplerLocalException;

/**
 * 周期
 * 
 * @author kim 2015年12月21日
 */
public enum Period {

	DAY, HOUR, MINUTE, SECOND, MILLISECOND;

	/**
	 * 指定时间数转换为当前周期(同时计算时区偏移量)
	 * 
	 * @param date
	 * @return
	 */
	public long period(Date date) {
		return this.period(date.getTime());
	}

	/**
	 * 指定毫秒数转换为当前周期(同时计算时区偏移量)
	 * 
	 * @param millis
	 * @return
	 */
	public long period(long millis) {
		switch (this) {
		case DAY:
			return TimeUnit.DAYS.convert(millis + TimeZone.getDefault().getOffset(millis), TimeUnit.MILLISECONDS);
		case HOUR:
			return TimeUnit.HOURS.convert(millis + TimeZone.getDefault().getOffset(millis), TimeUnit.MILLISECONDS);
		case MINUTE:
			return TimeUnit.MINUTES.convert(millis + TimeZone.getDefault().getOffset(millis), TimeUnit.MILLISECONDS);
		case SECOND:
			return TimeUnit.SECONDS.convert(millis + TimeZone.getDefault().getOffset(millis), TimeUnit.MILLISECONDS);
		case MILLISECOND:
			return TimeUnit.MILLISECONDS.convert(millis + TimeZone.getDefault().getOffset(millis), TimeUnit.MILLISECONDS);
		default:
			throw new KeplerLocalException("Unkonw period: " + this);
		}
	}

	/**
	 * 当前毫秒数转换为当前周期(同时计算时区偏移量)
	 * 
	 * @return
	 */
	public long period() {
		return this.period(System.currentTimeMillis());
	}

	/**
	 * 周期转换为毫秒值
	 * 
	 * @param period
	 * @return
	 */
	public long convert(long period) {
		switch (this) {
		case MILLISECOND:
			return period;
		case SECOND:
			return period * 1000;
		case MINUTE:
			return period * 60 * 1000;
		case HOUR:
			return period * 60 * 60 * 1000;
		case DAY:
			return period * 24 * 60 * 60 * 1000;
		default:
			throw new KeplerLocalException("Unkonw period: " + this);
		}
	}
}
