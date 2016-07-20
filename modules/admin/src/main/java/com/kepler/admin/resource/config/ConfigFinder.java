package com.kepler.admin.resource.config;

/**
 * @author kim 2015年12月26日
 */
public interface ConfigFinder {

	/**
	 * 通过SID获取动态参数
	 * 
	 * @param sid
	 * @return
	 */
	public Config sid(String sid);
}
