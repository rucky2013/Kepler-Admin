package com.kepler.admin.resource.dependency;

import java.util.Set;

import com.kepler.admin.domain.ServiceAndVersion;

/**
 * 依赖的服务集群
 * 
 * @author KimShen
 *
 */
public interface DependencyCluster {

	/**
	 * 依赖服务的集群数量
	 * 
	 * @return
	 */
	public int getCluster();

	/**
	 * 依赖服务集群所属分组集合
	 * 
	 * @return
	 */
	public Set<String> getGroups();

	/**
	 * 依赖服务集群所属服务
	 * @return
	 */
	public ServiceAndVersion getService();
}
