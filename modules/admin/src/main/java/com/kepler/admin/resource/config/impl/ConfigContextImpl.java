package com.kepler.admin.resource.config.impl;

import java.util.HashMap;
import java.util.Map;

import com.kepler.admin.resource.config.Config;
import com.kepler.admin.resource.config.ConfigContext;
import com.kepler.admin.resource.config.ConfigFinder;

/**
 * @author kim 2015年12月26日
 */
public class ConfigContextImpl implements ConfigFinder, ConfigContext {

	/**
	 * Path维度
	 */
	private final Map<String, Config> path = new HashMap<String, Config>();

	/**
	 * SID维度
	 */
	private final Map<String, Config> sid = new HashMap<String, Config>();

	private ConfigContextImpl remove(Config config) {
		this.sid.remove(config.getSid());
		this.path.remove(config.getPath());
		return this;
	}

	@Override
	public Config sid(String sid) {
		return this.sid.get(sid);
	}

	@Override
	public synchronized ConfigContextImpl remove(String path) {
		return this.remove(this.path.get(path));
	}

	@Override
	public synchronized ConfigContextImpl insert(Config config) {
		this.sid.put(config.getSid(), config);
		this.path.put(config.getPath(), config);
		return this;
	}

	@Override
	public synchronized ConfigContextImpl update(Config config) {
		return this.remove(this.path.get(config.getPath())).insert(config);
	}
}
