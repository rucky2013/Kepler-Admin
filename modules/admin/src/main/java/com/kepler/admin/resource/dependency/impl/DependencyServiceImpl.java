package com.kepler.admin.resource.dependency.impl;

import com.kepler.admin.domain.ServiceAndVersion;
import com.kepler.admin.resource.dependency.DependencyService;
import com.kepler.org.apache.commons.lang.builder.ToStringBuilder;
import com.kepler.service.imported.ImportedService;

/**
 * @author kim
 *
 * 2016年3月4日
 */
public class DependencyServiceImpl implements DependencyService {

	private final String path;

	private final ImportedService imported;

	private final ServiceAndVersion service;

	public DependencyServiceImpl(String path, ImportedService imported) {
		super();
		this.path = path;
		this.imported = imported;
		this.service = new ServiceAndVersion(imported.service(), imported.versionAndCatalog());
	}

	@Override
	public String path() {
		return this.path;
	}

	@Override
	public ImportedService imported() {
		return this.imported;
	}

	public ServiceAndVersion dependency() {
		return this.service;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
