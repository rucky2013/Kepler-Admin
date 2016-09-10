package com.kepler.admin.resource.dependency;

import com.kepler.admin.domain.ServiceAndVersion;
import com.kepler.service.imported.ImportedService;

/**
 * 依赖服务及依赖方信息
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

	/**
	 * 服务依赖(依赖服务的Host + 被依赖服务)
	 * 
	 * @return
	 */
	public ImportedService imported();
	
	/**
	 * 被依赖服务, new ServiceAndVersion(ImportedService.service(), ImportedService.versionAndCatalog());
	 * 
	 * @return
	 */
	public ServiceAndVersion dependency();
}
