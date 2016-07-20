package com.kepler.admin.statistics.method;

import java.util.Collection;
import java.util.List;

import com.kepler.admin.domain.Period;

/**
 * @author kim 2015年12月21日
 */
public interface MethodInvokerService {

	/**
	 * SID + Service + Version维度方法访问质量
	 * 
	 * @param sid
	 * @param service
	 * @param versionAndCatalog
	 * @param period
	 * @param offset
	 * @return
	 */
	public Collection<MethodInvoker> methods(String sid, String service, String versionAndCatalog, Period period, int offset);

	/**
	 * Service + Version维度方法访问质量
	 * 
	 * @param service
	 * @param versionAndCatalog
	 * @param period
	 * @param offset
	 * @return
	 */
	public Collection<MethodInvoker> methods4Service(String service, String versionAndCatalog, Period period, int offset);
	
	/**
	 * group 维度的方法访问质量
	 * 
	 * @param group
	 * @param period
	 * @param offset
	 * @param sortBy
	 * @return
	 */
	public List<MethodInvoker> methods4Group(String group, Period period, int offset, SortBy sortBy);
}
