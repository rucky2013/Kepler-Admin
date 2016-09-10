package com.kepler.admin.controller;

import java.util.Collection;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kepler.admin.domain.ServiceAndVersion;
import com.kepler.admin.resource.dependency.DependencyApps;
import com.kepler.admin.resource.dependency.DependencyCluster;
import com.kepler.admin.resource.dependency.DependencyFinder;
import com.kepler.admin.resource.dependency.DependencyInstance;

/**
 * @author kim
 *
 * 2016年3月4日
 */
@Controller
@RequestMapping(value = "/dependency")
public class DependencyController {

	private final DependencyFinder finder;

	public DependencyController(DependencyFinder finder) {
		super();
		this.finder = finder;
	}

	/**
	 * 依赖指定服务的Group/App
	 * 
	 * @param service
	 * @param versionAndCatalog
	 * @return
	 */
	@RequestMapping(value = "/exported/apps", method = RequestMethod.GET)
	@ResponseBody
	public Collection<DependencyApps> exported(String service, String versionAndCatalog) {
		return this.finder.exported(new ServiceAndVersion(service, versionAndCatalog));
	}

	/**
	 * 依赖指定服务,并属于指定Group,App的实例集合
	 * 
	 * @param service
	 * @param versionAndCatalog
	 * @param group
	 * @param app
	 * @return
	 */
	@RequestMapping(value = "/exported/instances", method = RequestMethod.GET)
	@ResponseBody
	public Collection<DependencyInstance> exported(String service, String versionAndCatalog, String group, String app) {
		return this.finder.exported(new ServiceAndVersion(service, versionAndCatalog), group, app);
	}

	/**
	 * 指定SID依赖的服务集群
	 * 
	 * @param sid
	 * @return
	 */
	@RequestMapping(value = "/imported", method = RequestMethod.GET)
	@ResponseBody
	public Collection<DependencyCluster> imported(String sid) {
		return this.finder.imported(sid);
	}
}
