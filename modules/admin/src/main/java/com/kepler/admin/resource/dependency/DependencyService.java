package com.kepler.admin.resource.dependency;

import com.kepler.admin.domain.ServiceAndVersion;
import com.kepler.service.imported.ImportedService;

/**
 * 依赖服务
 * 
 * @author kim
 *
 * 2016年3月4日
 */
public interface DependencyService  {

	/**
	 * ZK Path
	 * 
	 * @return
	 */
	public String path();

	public ImportedService imported();
	
	public ServiceAndVersion dependency();
}
