package com.kepler.admin.resource.instance;

import java.util.Collection;

import com.kepler.admin.domain.ServiceAndVersion;

/**
 * @author kim 2015年12月16日
 */
public interface InstanceFinder {

	/**
	 * 服务标签集合
	 * 
	 * @return
	 */
	public Collection<String> tags();

	/**
	 * 服务业务分组集合
	 * 
	 * @return
	 */
	public Collection<String> groups();

	/**
	 * 服务Service + 版本集合
	 * 
	 * @return
	 */
	public Collection<ServiceAndVersion> service4versions();

	/**
	 * 获取SID对应的服务集合
	 * 
	 * @param sid
	 * @return
	 */
	public Collection<Instance> sid(String sid);

	/**
	 * 获取Tag对应的服务集合
	 * 
	 * @param tag
	 * @return
	 */
	public Collection<Instance> tag(String tag);

	/**
	 * 获取业务分组对应的服务集合
	 * 
	 * @param group
	 * @return
	 */
	public Collection<Instance> group(String group);

	/**
	 * 获取Service + 版本服务集合
	 * 
	 * @param service
	 * @param versionAndCatalog
	 * @return
	 */
	public Collection<Instance> service4version(String service, String versionAndCatalog);

	/**
	 * ZK路径对应的服务实例
	 * 
	 * @param path
	 * @return
	 */
	public Instance path(String path);

	/**
	 * 获取SID绑定Host, Host发布多个Service
	 * 
	 * @param sid
	 * @param service
	 * @param versionAndCatalog
	 * @return
	 */
	public Instance sid(String sid, String service, String versionAndCatalog);
}
