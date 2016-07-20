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
	 * 获取指定服务被依赖
	 * 
	 * @param service
	 * @return
	 */
	public Collection<DependencyApps> exported(ServiceAndVersion service);

	/**
	 * 获取依赖指定服务, 指定Group, 指定App的实例依赖
	 * 
	 * @param service
	 * @param group
	 * @param application
	 * @return
	 */
	public Collection<DependencyInstance> exported(ServiceAndVersion service, String group, String application);

	/**
	 * 获取指定SID的依赖
	 * 
	 * @param sid
	 * @return
	 */
	public Collection<ServiceAndVersion> imported(String sid);
}
