package com.kepler.admin.resource.dependency;

import java.util.Collection;

import com.kepler.admin.domain.ServiceAndVersion;

/**
 * 服务依赖查询
 * 
 * @author kim
 *
 * 2016年3月4日
 */
public interface DependencyFinder {

	/**
	 * 获取依赖指定服务, 并属于指定Group/App的实例
	 * 
	 * @param service
	 * @param group
	 * @param app
	 * @return
	 */
	public Collection<DependencyInstance> exported(ServiceAndVersion service, String group, String app);

	/**
	 * 获取依赖指定服务的列表
	 * 
	 * @param service
	 * @return
	 */
	public Collection<DependencyApps> exported(ServiceAndVersion service);

	/**
	 * 获取指定SID的依赖集群
	 * 
	 * @param sid
	 * @return
	 */
	public Collection<DependencyCluster> imported(String sid);
}
