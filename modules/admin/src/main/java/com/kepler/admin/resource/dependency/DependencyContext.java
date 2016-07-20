package com.kepler.admin.resource.dependency;

/**
 * 依赖变更监听
 * 
 * @author kim 2016年1月2日
 */
public interface DependencyContext {

	public DependencyContext insert(DependencyService dependency);

	public DependencyContext remove(String path);
}
