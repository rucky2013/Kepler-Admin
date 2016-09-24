package com.kepler.admin.resource.terminal.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.kepler.admin.resource.terminal.TerminalStatus;
import com.kepler.admin.resource.terminal.TerminalStatusContext;
import com.kepler.admin.resource.terminal.TerminalStatusFinder;

/**
 * @author kim 2015年12月26日
 */
public class TerminalStatusContextImpl implements TerminalStatusFinder, TerminalStatusContext {

	// 业务分组对应的终端状态集合
	private final Map<String, List<TerminalStatus>> group = new HashMap<String, List<TerminalStatus>>();

	// ZK路径对应的终端状态
	private final Map<String, TerminalStatus> path = new HashMap<String, TerminalStatus>();

	// SID对应的终端状态
	private final Map<String, TerminalStatus> sid = new HashMap<String, TerminalStatus>();

	// 无效的终端
	private final UnvalidTerminalStatus unvalid = new UnvalidTerminalStatus();

	private final Object lock = new Object();

	/**
	 * 工具方法
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
	 * 工具方法, 如果Map中指定List为空则进行删除(由调用方进行同步)
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

	public Collection<TerminalStatus> application(String group, String application) {
		List<TerminalStatus> matched = new ArrayList<TerminalStatus>();
		for (TerminalStatus terminal : this.group(group)) {
			if (terminal.getApplication().equals(application)) {
				// 仅加载符合应用名称的终端
				matched.add(terminal);
			}
		}
		return matched;
	}

	public Collection<String> applications(String group) {
		Set<String> applications = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		for (TerminalStatus terminal : this.group(group)) {
			applications.add(terminal.getApplication());
		}
		return applications;
	}

	public Collection<String> groups() {
		TreeSet<String> groups = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		groups.addAll(this.group.keySet());
		return groups;
	}

	@Override
	public TerminalStatus sid(String sid) {
		TerminalStatus terminal = this.sid.get(sid);
		return terminal != null ? terminal : this.unvalid;
	}

	@Override
	// 强一致
	public TerminalStatusContextImpl insert(TerminalStatus status) {
		synchronized (this.lock) {
			this.upsert(this.group, status.getGroup(), status);
			this.path.put(status.getPath(), status);
			this.sid.put(status.getSid(), status);
			return this;
		}
	}

	@Override
	public TerminalStatusContextImpl update(TerminalStatus status) {
		synchronized (this.lock) {
			return this.remove(status.getPath()).insert(status);
		}
	}

	@Override
	public TerminalStatusContextImpl remove(String path) {
		synchronized (this.lock) {
			// 获取Path对应Status并从SID集合删除
			TerminalStatus status = this.sid.remove(this.path.remove(path).getSid());
			this.remove(this.group, status.getGroup(), status);
			return this;
		}
	}

	/**
	 * 无效的终端(未上传信息)
	 * 
	 * @author KimShen
	 *
	 */
	private class UnvalidTerminalStatus implements TerminalStatus {

		private static final long serialVersionUID = 1L;

		@Override
		public String getSid() {
			return "";
		}

		@Override
		public String getPid() {
			return "";
		}

		@Override
		public String getHost() {
			return "Unvalid host";
		}

		@Override
		public String getGroup() {
			return "";
		}

		@Override
		public String getApplication() {
			return "";
		}

		@Override
		public Map<String, Object> getStatus() {
			return null;
		}

		@Override
		public String getPath() {
			return "";
		}
	}
}
