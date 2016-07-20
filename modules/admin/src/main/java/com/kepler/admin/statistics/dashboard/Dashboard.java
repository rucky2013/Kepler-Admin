package com.kepler.admin.statistics.dashboard;

import com.kepler.admin.domain.ServiceAndVersion;

/**
 * @author kim
 *
 * 2016年3月5日
 */
public interface Dashboard<T> {

	public ServiceAndVersion getService();

	/**
	 * 环比
	 * 
	 * @return
	 */
	public double getCompare();

	/**
	 * 当前周期值
	 * 
	 * @return
	 */
	public T getPeriod();
}
