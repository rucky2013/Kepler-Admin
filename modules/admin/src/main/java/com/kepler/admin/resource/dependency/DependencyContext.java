package com.kepler.admin.resource.dependency;

/**
 * 服务依赖上下文
 * 
 * @author kim 2016年1月2日
 */
public interface DependencyContext {

	/**
	 * 加入服务依赖(内含指定路径)
	 * 
	 * @param dependency
	 * @return
	 */
	public DependencyContext insert(DependencyService dependency);

	/**
	 * 移除指定路径对应的服务依赖
	 * 
	 * @param path
	 * @return
	 */
	public DependencyContext remove(String path);
}
