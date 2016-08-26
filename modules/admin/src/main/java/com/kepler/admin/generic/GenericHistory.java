package com.kepler.admin.generic;

import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
public interface GenericHistory {

	/**
	 * 获取历史
	 * 
	 * @param service
	 * @param method
	 * @return
	 */
	public GenericRequest get(Service service, String method) throws Exception;

	/**
	 * 推送数据
	 * 
	 * @param request
	 */
	public void set(GenericRequest request) throws Exception;
}
