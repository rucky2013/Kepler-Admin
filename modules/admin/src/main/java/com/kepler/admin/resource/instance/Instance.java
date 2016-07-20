package com.kepler.admin.resource.instance;

import com.kepler.admin.domain.ServiceAndVersion;
import com.kepler.service.ServiceInstance;

/**
 * 存储节点基本信息
 * 
 * @author kim 2015年12月16日
 */
public interface Instance {

	public String getSid();

	public String getTag();

	/**
	 * ZK Path
	 * 
	 * @return
	 */
	public String getPath();

	public String getHost();

	/**
	 * 业务分组
	 * 
	 * @return
	 */
	public String getGroup();

	public int getPriority();

	public ServiceAndVersion getService();

	/**
	 * 服务节点(Not Get)
	 * 
	 * @return
	 */
	public ServiceInstance instance();
}
