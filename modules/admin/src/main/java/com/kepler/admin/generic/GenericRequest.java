package com.kepler.admin.generic;

import com.kepler.service.Service;

/**
 * 泛化请求
 * 
 * @author KimShen
 *
 */
public interface GenericRequest {

	public String getTag();
	
	public Service metadata();

	public String getMethod();

	public String getService();
	
	public String getVersion();
	
	public String getCatalog();
	
	public Object[] getDatas();

	public String[] getClasses();
}
