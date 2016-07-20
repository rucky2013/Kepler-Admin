package com.kepler.admin.resource.instance.impl;

import java.util.Collection;
import java.util.HashSet;

import com.kepler.admin.domain.ServiceAndVersion;
import com.kepler.admin.resource.instance.Instance;

/**
 * 从Instance去重Service + Version
 * 
 * @author kim
 *
 * 2016年3月8日
 */
public class InstanceServices extends HashSet<ServiceAndVersion> {

	private static final long serialVersionUID = 1L;

	public InstanceServices(Collection<Instance> instances) {
		if (instances != null) {
			for (Instance each : instances) {
				super.add(each.getService());
			}
		}
	}
}