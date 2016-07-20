package com.kepler.admin.resource.dependency;

import java.util.Collection;

/**
 * 分组下属的App集合
 * 
 * @author kim
 *
 * 2016年3月4日
 */
public interface DependencyApps {

	public String getGroup();

	public Collection<String> getApps();
}
