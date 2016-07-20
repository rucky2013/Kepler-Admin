package com.kepler.admin.resource.dependency.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.kepler.admin.domain.ServiceAndVersion;
import com.kepler.admin.resource.dependency.DependencyApps;
import com.kepler.admin.resource.dependency.DependencyContext;
import com.kepler.admin.resource.dependency.DependencyFinder;
import com.kepler.admin.resource.dependency.DependencyInstance;
import com.kepler.admin.resource.dependency.DependencyService;
import com.kepler.admin.resource.status.TerminalStatusFinder;
import com.kepler.org.apache.commons.lang.builder.EqualsBuilder;
import com.kepler.org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author kim
 *
 * 2016年3月4日
 */
public class DependencyContextImpl implements DependencyFinder, DependencyContext {

	private static final Collection<ServiceAndVersion> EMPTY_SERVICE = new ArrayList<ServiceAndVersion>();

	private static final Collection<DependencyApps> EMPTY_DEPENDENCY = new ArrayList<DependencyApps>();

	private final Map<String, DependencyService> paths = new HashMap<String, DependencyService>();

	private final Exported exported = new Exported();

	private final Imported imported = new Imported();

	private final TerminalStatusFinder finder;

	public DependencyContextImpl(TerminalStatusFinder finder) {
		super();
		this.finder = finder;
	}

	/**
	 * @param dependency
	 * @return
	 */
	private Applications app(DependencyService dependency) {
		Groups groups = this.exported.groups(dependency.dependency());
		Group group = groups.group(dependency.imported().group());
		return group.apps(dependency.imported().name());
	}

	public Collection<ServiceAndVersion> imported(String sid) {
		Collection<ServiceAndVersion> services = this.imported.get(sid);
		// Not return Null
		return services != null ? services : DependencyContextImpl.EMPTY_SERVICE;
	}

	@Override
	public Collection<DependencyApps> exported(ServiceAndVersion service) {
		Collection<DependencyApps> dependencies = this.exported.exported(service);
		// Not return Null
		return dependencies != null ? dependencies : DependencyContextImpl.EMPTY_DEPENDENCY;
	}

	public Collection<DependencyInstance> exported(ServiceAndVersion service, String group, String application) {
		Groups groups = this.exported.groups(service);
		Applications applications = groups.get(group).apps(application);
		List<DependencyInstance> apps = new ArrayList<DependencyInstance>();
		for (String sid : applications.refs()) {
			apps.add(new DependencyInstanceImpl(sid, this.finder.sid(sid).getHost()));
		}
		return apps;
	}

	@Override
	public synchronized DependencyContext insert(DependencyService dependency) {
		// Imported
		this.imported.insert(dependency.imported().sid(), dependency.dependency());
		// Exported
		this.app(dependency).ref(dependency.imported().sid());
		this.paths.put(dependency.path(), dependency);
		return this;
	}

	@Override
	public synchronized DependencyContext remove(String path) {
		DependencyService dependency = this.paths.remove(path);
		// Exported
		this.app(dependency).free(dependency.imported().sid());
		// Imported
		this.imported.delete(dependency.imported().sid(), dependency.dependency());
		return this;
	}

	private class Imported extends HashMap<String, Set<ServiceAndVersion>> {

		private static final long serialVersionUID = 1L;

		public Imported insert(String sid, ServiceAndVersion service) {
			Set<ServiceAndVersion> services = super.get(sid);
			// 不存在则创建
			services = services != null ? services : new HashSet<ServiceAndVersion>();
			services.add(service);
			super.put(sid, services);
			return this;
		}

		public Imported delete(String sid, ServiceAndVersion service) {
			Set<ServiceAndVersion> services = super.get(sid);
			services.remove(service);
			// 级联删除, 如果SID对应服务已经全部离线则删除SID
			if (services.isEmpty()) {
				super.remove(sid);
			}
			return this;
		}
	}

	/**
	 * 服务 / Group组映射
	 * 
	 * @author kim
	 *
	 * 2016年3月4日
	 */
	private class Exported extends HashMap<ServiceAndVersion, Groups> {

		private static final long serialVersionUID = 1L;

		/**
		 * 获取服务的被依赖
		 * 
		 * @param service
		 * @return
		 */
		public Collection<DependencyApps> exported(ServiceAndVersion service) {
			List<DependencyApps> dependencies = new ArrayList<DependencyApps>();
			for (Group group : this.groups(service).values()) {
				dependencies.add(new GroupDependency(group));
			}
			return dependencies;
		}

		/**
		 * 获取Service对应的业务分组集合
		 * 
		 * @param service
		 * @return
		 */
		public Groups groups(ServiceAndVersion service) {
			Groups groups = super.get(service);
			// 不存在则创建
			groups = groups != null ? groups : new Groups(service, this);
			super.put(service, groups);
			return groups;
		}

		/**
		 * 释放对应服务
		 * 
		 * @param service
		 * @return
		 */
		public Exported free(ServiceAndVersion service) {
			super.remove(service);
			return this;
		}
	}

	/**
	 * Group name / Group
	 * 
	 * @author kim
	 *
	 * 2016年3月4日
	 */
	private class Groups extends HashMap<String, Group> {

		private static final long serialVersionUID = 1L;

		private final ServiceAndVersion service;

		private final Exported imported;

		private Groups(ServiceAndVersion service, Exported imported) {
			super();
			this.service = service;
			this.imported = imported;
		}

		public Group group(String group) {
			Group matcher = super.get(group);
			// 不存在则创建
			matcher = matcher != null ? matcher : new Group(group, this);
			super.put(group, matcher);
			return matcher;
		}

		/**
		 * 释放Groups
		 * 
		 * @return
		 */
		public Groups free(String group) {
			// 级联删除, 释放Group, 如果对应Groups(服务所拥有的业务分组集合)已经为空则释放Groups
			super.remove(group);
			if (super.isEmpty()) {
				this.imported.free(this.service);
			}
			return this;
		}

		public int hashCode() {
			return new HashCodeBuilder(17, 37).append(this.service).append(this.imported).toHashCode();
		}

		public boolean equals(Object obj) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}
	}

	/**
	 * App name / App
	 * 
	 * @author kim
	 *
	 * 2016年3月4日
	 */
	private class Group extends HashMap<String, Applications> {

		private static final long serialVersionUID = 1L;

		private final Groups groups;

		private final String group;

		/**
		 * @param group Group名称
		 * @param groups
		 */
		private Group(String group, Groups groups) {
			super();
			this.groups = groups;
			this.group = group;
		}

		public String group() {
			return this.group;
		}

		public Applications apps(String name) {
			Applications app = super.get(name);
			// 不存在在创建
			app = app != null ? app : new Applications(this, name);
			super.put(name, app);
			return app;
		}

		public Group free(String app) {
			// 释放APP
			super.remove(app);
			// 级联删除, 如果当前Group已经不存在任何APP释放Group
			if (super.isEmpty()) {
				this.groups.free(this.group);
			}
			return this;
		}

		public int hashCode() {
			return new HashCodeBuilder(17, 37).append(this.groups).append(this.group).toHashCode();
		}

		public boolean equals(Object obj) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}
	}

	private class Applications {

		private final Set<String> refs = new HashSet<String>();

		private final Group group;

		private final String name;

		/**
		 * @param group 所属分组
		 * @param name APP名称
		 */
		private Applications(Group group, String name) {
			super();
			this.group = group;
			this.name = name;
		}

		public Set<String> refs() {
			return this.refs;
		}

		/**
		 * APP引用增加
		 * 
		 * @return
		 */
		public Applications ref(String sid) {
			this.refs.add(sid);
			return this;
		}

		/**
		 * APP引用减少
		 * 
		 * @return
		 */
		public Applications free(String sid) {
			this.refs.remove(sid);
			// 级联删除, 没有引用则触发回收
			if (this.refs.isEmpty()) {
				this.group.free(this.name);
			}
			return this;
		}

		public int hashCode() {
			return new HashCodeBuilder(17, 37).append(this.group).append(this.name).toHashCode();
		}

		public boolean equals(Object obj) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}
	}

	private class DependencyInstanceImpl implements DependencyInstance {

		private final String host;

		private final String sid;

		private DependencyInstanceImpl(String sid, String host) {
			super();
			this.sid = sid;
			this.host = host;
		}

		@Override
		public String getSid() {
			return this.sid;
		}

		@Override
		public String getHost() {
			return this.host;
		}
	}

	private class GroupDependency implements DependencyApps {

		private final Collection<String> apps;

		private final String group;

		private GroupDependency(Group group) {
			super();
			this.group = group.group();
			this.apps = group.keySet();
		}

		public String getGroup() {
			return this.group;
		}

		@Override
		public Collection<String> getApps() {
			return this.apps;
		}
	}
}
