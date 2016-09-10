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
		this.path.remove(config.getPath());
		this.sid.remove(config.getSid());
		return this;
	}

	@Override
	public Config sid(String sid) {
		return this.sid.get(sid);
	}

	@Override
	public ConfigContextImpl remove(String path) {
		synchronized (path.intern()) {
			return this.remove(this.path.get(path));
		}
	}

	@Override
	public ConfigContextImpl insert(Config config) {
		synchronized (config.getPath().intern()) {
			this.path.put(config.getPath(), config);
			this.sid.put(config.getSid(), config);
		}
		return this;
	}

	@Override
	public ConfigContextImpl update(Config config) {
		synchronized (config.getPath().intern()) {
			// Insert after Delete
			return this.remove(this.path.get(config.getPath())).insert(config);
		}
	}
}
