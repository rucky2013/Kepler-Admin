package com.kepler.admin.resource.config.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kepler.ack.impl.AckFuture;
import com.kepler.ack.impl.AckTimeOutImpl;
import com.kepler.admin.resource.config.Config;
import com.kepler.admin.resource.config.ConfigContext;
import com.kepler.admin.resource.config.ConfigFinder;
import com.kepler.admin.trace.impl.TraceTask;
import com.kepler.connection.reject.AddressReject;
import com.kepler.connection.reject.DefaultRejectContext;
import com.kepler.connection.reject.ServiceReject;
import com.kepler.host.Host;
import com.kepler.host.impl.DefaultHostContext;
import com.kepler.id.impl.DefaultIDGenerators;
import com.kepler.invoker.forkjoin.impl.ForkJoinInvoker;
import com.kepler.invoker.impl.BroadcastInvoker;
import com.kepler.invoker.impl.CompeteInvoker;
import com.kepler.invoker.impl.DemoteInvoker;
import com.kepler.invoker.impl.MainInvoker;
import com.kepler.mock.impl.DefaultMockerContext;
import com.kepler.promotion.impl.DefaultPromotion;
import com.kepler.serial.SerialID;
import com.kepler.token.impl.AccessTokenContext;
import com.kepler.trace.Trace;

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

	/**
	 * Key集合
	 */
	private final List<String> keys = new ArrayList<String>();

	public ConfigContextImpl() {
		this.keys.add(AccessTokenContext.TOKEN_PROFILE_KEY);
		this.keys.add(DefaultIDGenerators.GENERATOR_KEY);
		this.keys.add(MainInvoker.THRESHOLD_ENABLED_KEY);
		this.keys.add(DefaultRejectContext.REJECT_KEY);
		this.keys.add(DefaultHostContext.ROUTING_KEY);
		this.keys.add(DefaultMockerContext.MOCK_KEY);
		this.keys.add(AckTimeOutImpl.DEMOTION_KEY);
		this.keys.add(BroadcastInvoker.CANCEL_KEY);
		this.keys.add(DefaultPromotion.ELAPSE_KEY);
		this.keys.add(DefaultPromotion.TIMES_KEY);
		this.keys.add(MainInvoker.THRESHOLD_KEY);
		this.keys.add(AddressReject.ADDRESS_KEY);
		this.keys.add(CompeteInvoker.CANCEL_KEY);
		this.keys.add(ForkJoinInvoker.TAGS_KEY);
		this.keys.add(ServiceReject.REJECT_KEY);
		this.keys.add(DemoteInvoker.DEMOTE_KEY);
		this.keys.add(AckFuture.TIMEOUT_KEY);
		this.keys.add(TraceTask.ENABLED_KEY);
		this.keys.add(SerialID.SERIAL_KEY);
		this.keys.add(Trace.ENABLED_KEY);
		this.keys.add(Host.TAG_KEY);
		Collections.sort(keys, String.CASE_INSENSITIVE_ORDER);
	}

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

	public Collection<String> keys() {
		return this.keys;
	}
}
