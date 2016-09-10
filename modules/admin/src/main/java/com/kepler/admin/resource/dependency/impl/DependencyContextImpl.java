package com.kepler.admin.resource.dependency.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.CollectionUtils;

import com.kepler.admin.domain.ServiceAndVersion;
import com.kepler.admin.resource.dependency.DependencyApps;
import com.kepler.admin.resource.dependency.DependencyCluster;
import com.kepler.admin.resource.dependency.DependencyContext;
import com.kepler.admin.resource.dependency.DependencyFinder;
import com.kepler.admin.resource.dependency.DependencyInstance;
import com.kepler.admin.resource.dependency.DependencyService;
import com.kepler.admin.resource.instance.Instance;
import com.kepler.admin.resource.instance.InstanceFinder;
import com.kepler.admin.resource.terminal.TerminalStatusFinder;
import com.kepler.org.apache.commons.lang.builder.EqualsBuilder;
import com.kepler.org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author kim
 *
 * 2016年3月4日
 */
public class DependencyContextImpl implements DependencyFinder, DependencyContext {

	private static final Collection<DependencyApps> EMPTY_DEPENDENCY = Collections.unmodifiableList(new ArrayList<DependencyApps>());

	private final Map<String, DependencyService> paths = new HashMap<String, DependencyService>();

	private final ClusterComparator comparator = new ClusterComparator();

	// 依赖指定服务的业务分组及应用信息
	private final Exported exported = new Exported();

	// 指定实例(SID)已依赖服务
	private final Imported imported = new Imported();

	private final TerminalStatusFinder terminal;

	private final InstanceFinder instance;

	public DependencyContextImpl(TerminalStatusFinder terminal, InstanceFinder instance) {
		super();
		this.terminal = terminal;
		this.instance = instance;
	}

	/**
	 * 获取服务依赖相关业务分组
	 * 
	 * @param dependency
	 * @return
	 */
	private Applications app(DependencyService dependency) {
		// 获取依赖指定服务的业务分组集合, 并将应用名称加入到业务分组集合中指定的(DependencyService所属的服务)业务分组中
		Groups groups = this.exported.groups(dependency.dependency());
		Group group = groups.group(dependency.imported().group());
		return group.apps(dependency.imported().name());
	}

	public Collection<DependencyCluster> imported(String sid) {
		List<DependencyCluster> clusters = new ArrayList<DependencyCluster>();
		// 指定实例(SID)已依赖服务
		Collection<ServiceAndVersion> services = this.imported.get(sid);
		for (ServiceAndVersion service : services) {
			clusters.add(new DependencyClusterImpl(service, this.instance.service4version(service.getService(), service.getVersionAndCatalog())));
		}
		// 排序
		Collections.sort(clusters, this.comparator);
		return clusters;
	}

	@Override
	public Collection<DependencyApps> exported(ServiceAndVersion service) {
		// 获取指定服务被依赖服务
		Collection<DependencyApps> dependencies = this.exported.exported(service);
		// Not return Null
		return dependencies != null ? dependencies : DependencyContextImpl.EMPTY_DEPENDENCY;
	}

	public Collection<DependencyInstance> exported(ServiceAndVersion service, String group, String app) {
		// 获取依赖指定服务的业务分组集合
		Groups groups = this.exported.groups(service);
		Applications applications = groups.get(group).apps(app);
		// 通过应用SID加载实例
		List<DependencyInstance> apps = new ArrayList<DependencyInstance>();
		for (String sid : applications.refs()) {
			apps.add(new DependencyInstanceImpl(sid, this.terminal.sid(sid).getHost()));
		}
		return apps;
	}

	@Override
	public synchronized DependencyContext insert(DependencyService dependency) {
		// Imported(指定实例)已依赖服务, SID-依赖的服务信息 
		this.imported.insert(dependency.imported().sid(), dependency.dependency());
		// Exported(获取服务依赖相关业务分组并加入)
		this.app(dependency).ref(dependency.imported().sid());
		this.paths.put(dependency.path(), dependency);
		return this;
	}

	@Override
	public synchronized DependencyContext remove(String path) {
		DependencyService dependency = this.paths.remove(path);
		// Exported释放依赖指定服务的业务分组及其应用
		this.app(dependency).free(dependency.imported().sid());
		// Imported
		this.imported.delete(dependency.imported().sid(), dependency.dependency());
		return this;
	}

	/**
	 * 保存实例依赖的服务
	 * 
	 * @author KimShen
	 *
	 */
	private class Imported extends HashMap<String, Set<ServiceAndVersion>> {

		private static final long serialVersionUID = 1L;

		/**
		 * 指定服务加入实例依赖服务列表
		 * 
		 * @param sid
		 * @param service
		 * @return
		 */
		public Imported insert(String sid, ServiceAndVersion service) {
			Set<ServiceAndVersion> services = super.get(sid);
			// 不存在则创建
			services = services != null ? services : new HashSet<ServiceAndVersion>();
			services.add(service);
			super.put(sid, services);
			return this;
		}

		/**
		 * 指定服务从实例依赖服务列表中删除
		 * 
		 * @param sid
		 * @param service
		 * @return
		 */
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
			// 从分组加载
			List<DependencyApps> dependencies = new ArrayList<DependencyApps>();
			// 获取依赖指定服务的业务分组集合(Group/App)
			for (Group group : this.groups(service).values()) {
				dependencies.add(new GroupDependency(group));
			}
			return dependencies;
		}

		/**
		 * 获取依赖指定服务的业务分组集合
		 * 
		 * @param service
		 * @return
		 */
		public Groups groups(ServiceAndVersion service) {
			// Group/App
			Groups groups = super.get(service);
			// 获取服务对应分组, 不存在则创建
			groups = groups != null ? groups : new Groups(service, this);
			super.put(service, groups);
			return groups;
		}

		/**
		 * 释放依赖指定服务的业务分组集合(服务下线)
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
	 * 业务分组集合, Group name / Group
	 * 
	 * @author kim
	 *
	 * 2016年3月4日
	 */
	private class Groups extends HashMap<String, Group> {

		private static final long serialVersionUID = 1L;

		private final ServiceAndVersion service;

		private final Exported imported;

		/**
		 * @param service
		 * @param imported 用于回调
		 */
		private Groups(ServiceAndVersion service, Exported imported) {
			super();
			this.service = service;
			this.imported = imported;
		}

		/**
		 * 获取业务分组集合中指定的业务分组
		 * 
		 * @param group
		 * @return
		 */
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
			// 级联删除, 释放Group, 如果对应Groups已经为空则释放Groups
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
	 * 业务分组, App name / App
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
		 * @param groups 用于回调
		 */
		private Group(String group, Groups groups) {
			super();
			this.groups = groups;
			this.group = group;
		}

		public String group() {
			return this.group;
		}

		/**
		 * 获取业务分组中的应用(名称)集合
		 * 
		 * @param name
		 * @return
		 */
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

	/**
	 * 业务分组中APP集合
	 * 
	 * @author KimShen
	 *
	 */
	private class Applications {

		private final Set<String> refs = new HashSet<String>();

		private final Group group;

		private final String name;

		/**
		 * @param group 所属分组, 用于回调
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

	/**
	 * 依赖服务的实例
	 * 
	 * @author KimShen
	 *
	 */
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

	/**
	 * 业务分组及其所含APP
	 * 
	 * @author KimShen
	 *
	 */
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

	/**
	 * 服务集群依赖
	 * 
	 * @author KimShen
	 *
	 */
	private class DependencyClusterImpl implements DependencyCluster {

		private final Set<String> groups = new HashSet<String>();

		private final ServiceAndVersion service;

		private final int cluster;

		public DependencyClusterImpl(ServiceAndVersion service, Collection<Instance> instances) {
			super();
			this.service = service;
			// 依赖的服务未上线
			if (CollectionUtils.isEmpty(instances)) {
				this.cluster = 0;
			} else {
				this.cluster = instances.size();
				for (Instance instance : instances) {
					this.groups.add(instance.getGroup());
				}
			}
		}

		@Override
		public int getCluster() {
			return this.cluster;
		}

		@Override
		public Set<String> getGroups() {
			return this.groups;
		}

		@Override
		public ServiceAndVersion getService() {
			return this.service;
		}
	}

	/**
	 * 集群排序
	 * 
	 * @author KimShen
	 *
	 */
	private class ClusterComparator implements Comparator<DependencyCluster> {

		private final Comparator<String> group = String.CASE_INSENSITIVE_ORDER;

		@Override
		public int compare(DependencyCluster o1, DependencyCluster o2) {
			return this.group.compare(o1.getGroups().toString(), o2.getGroups().toString());
		}
	}
}
