package com.kepler.admin.resource.status.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.kepler.admin.resource.status.TerminalStatus;
import com.kepler.admin.resource.status.TerminalStatusContext;
import com.kepler.admin.resource.status.TerminalStatusFinder;

/**
 * @author kim 2015年12月26日
 */
public class TerminalStatusContextImpl implements TerminalStatusFinder, TerminalStatusContext {

	private final Map<String, List<TerminalStatus>> group = new HashMap<String, List<TerminalStatus>>();

	private final Map<String, TerminalStatus> path = new HashMap<String, TerminalStatus>();

	private final Map<String, TerminalStatus> sid = new HashMap<String, TerminalStatus>();

	/**
	 * 增加Group对应Status
	 * 
	 * @param condition
	 * @param key
	 * @param status
	 */
	private void upsert(Map<String, List<TerminalStatus>> condition, String key, TerminalStatus status) {
		List<TerminalStatus> statuses = condition.get(key);
		condition.put(key, (statuses = statuses != null ? statuses : new ArrayList<TerminalStatus>()));
		statuses.add(status);
	}

	/**
	 * 删除Group对应Status
	 * 
	 * @param condition
	 * @param key
	 * @param status
	 */
	private void remove(Map<String, List<TerminalStatus>> condition, String key, TerminalStatus status) {
		List<TerminalStatus> statuses = condition.get(key);
		statuses.remove(status);
		// 级联删除, 如果集合为空则删除对应集合
		if (statuses.isEmpty()) {
			condition.remove(key);
		}
	}

	public Collection<TerminalStatus> group(String group) {
		return this.group.get(group);
	}

	public Collection<String> groups() {
		TreeSet<String> groups = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		groups.addAll(this.group.keySet());
		return groups;
	}

	@Override
	public TerminalStatus sid(String sid) {
		return this.sid.get(sid);
	}

	@Override
	public synchronized TerminalStatusContextImpl insert(TerminalStatus status) {
		this.upsert(this.group, status.getGroup(), status);
		this.path.put(status.getPath(), status);
		this.sid.put(status.getSid(), status);
		return this;
	}

	@Override
	public synchronized TerminalStatusContextImpl update(TerminalStatus status) {
		return this.remove(status.getPath()).insert(status);
	}

	@Override
	public synchronized TerminalStatusContextImpl remove(String path) {
		// 获取Path对应Status并从SID集合删除
		TerminalStatus status = this.sid.remove(this.path.remove(path).getSid());
		this.remove(this.group, status.getGroup(), status);
		return this;
	}
}
