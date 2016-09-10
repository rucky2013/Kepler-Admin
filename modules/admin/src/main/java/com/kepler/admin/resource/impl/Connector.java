package com.kepler.admin.resource.impl;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

import com.kepler.admin.resource.config.ConfigContext;
import com.kepler.admin.resource.config.impl.ConfigImpl;
import com.kepler.admin.resource.dependency.DependencyContext;
import com.kepler.admin.resource.dependency.impl.DependencyServiceImpl;
import com.kepler.admin.resource.instance.InstanceContext;
import com.kepler.admin.resource.instance.impl.InstanceImpl;
import com.kepler.admin.resource.terminal.TerminalStatusContext;
import com.kepler.admin.resource.terminal.impl.TerminalStatusImpl;
import com.kepler.host.HostStatus;
import com.kepler.serial.Serials;
import com.kepler.service.imported.ImportedService;
import com.kepler.zookeeper.ZkContext;
import com.kepler.zookeeper.ZkSerial;

/**
 * Curator -> 本地Mapping桥
 * 
 * @author kim 2015年12月16日
 */
public class Connector {

	private static final Log LOGGER = LogFactory.getLog(Connector.class);

	/**
	 * 监听依赖状态变化
	 */
	private final DependencyListener listener4dependecy = new DependencyListener();

	/**
	 * 监听服务实例变化
	 */
	private final InstanceListener listener4instance = new InstanceListener();

	/**
	 * 监听服务配置变化
	 */
	private final ConfigListener listener4config = new ConfigListener();

	/**
	 * 监听主机状态变化
	 */
	private final StatusListener listener4status = new StatusListener();

	/**
	 * 对应DependencyListener
	 */
	private final DependencyContext context4dependnecy;

	/**
	 * 对应ServiceListener
	 */
	private final InstanceContext context4service;

	/**
	 * 对应ConfigListener
	 */
	private final ConfigContext context4config;

	/**
	 * 对应StatusListener
	 */
	private final TerminalStatusContext context4status;

	private final TreeCache cache4dependecy;

	private final TreeCache cache4instance;

	private final TreeCache cache4config;

	private final TreeCache cache4status;

	private final Serials serials;

	public Connector(Serials serials, CuratorFramework client, InstanceContext context4service, TerminalStatusContext context4status, ConfigContext context4config, DependencyContext context4dependnecy) {
		super();
		this.cache4dependecy = new TreeCache(client, ZkContext.ROOT + ZkContext.DEPENDENCY);
		this.cache4config = new TreeCache(client, ZkContext.ROOT + ZkContext.CONFIG);
		this.cache4status = new TreeCache(client, ZkContext.ROOT + ZkContext.STATUS);
		this.cache4instance = new TreeCache(client, ZkContext.ROOT);
		this.context4dependnecy = context4dependnecy;
		this.context4service = context4service;
		this.context4config = context4config;
		this.context4status = context4status;
		this.serials = serials;
	}

	public void init() throws Exception {
		// 绑定并启动
		this.cache4dependecy.getListenable().addListener(this.listener4dependecy);
		this.cache4instance.getListenable().addListener(this.listener4instance);
		this.cache4config.getListenable().addListener(this.listener4config);
		this.cache4status.getListenable().addListener(this.listener4status);
		this.cache4dependecy.start();
		this.cache4instance.start();
		this.cache4config.start();
		this.cache4status.start();
	}

	public void destroy() {
		// 关闭并释放
		this.cache4dependecy.close();
		this.cache4instance.close();
		this.cache4config.close();
		this.cache4status.close();
	}

	/**
	 * 用于监听Status
	 * 
	 * @author KimShen
	 *
	 */
	private class StatusListener implements TreeCacheListener {

		public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
			switch (event.getType()) {
			case NODE_ADDED:
				// 加载主机静态信息
				if (event.getData().getData().length != 0) {
					Connector.this.context4status.insert(new TerminalStatusImpl(event.getData().getPath(), Connector.this.serials.def4input().input(event.getData().getData(), HostStatus.class)));
				}
				break;
			case NODE_UPDATED:
				// 更新主机静态信息
				if (event.getData().getData().length != 0) {
					Connector.this.context4status.update(new TerminalStatusImpl(event.getData().getPath(), Connector.this.serials.def4input().input(event.getData().getData(), HostStatus.class)));
				}
				break;
			case NODE_REMOVED:
				// 删除主机静态信息
				Connector.this.context4status.remove(event.getData().getPath());
				break;
			default:
				Connector.LOGGER.warn("Nothing for " + event + " ... ");
			}
		}
	}

	/**
	 * 用于监听Config
	 * 
	 * @author KimShen
	 *
	 */
	private class ConfigListener implements TreeCacheListener {

		@SuppressWarnings("unchecked")
		public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
			switch (event.getType()) {
			case NODE_ADDED:
				// 加载主机配置信息
				if (event.getData().getData().length != 0) {
					Connector.this.context4config.insert(new ConfigImpl(event.getData().getPath(), Connector.this.serials.def4input().input(event.getData().getData(), Map.class)));
				}
				break;
			case NODE_UPDATED:
				// 更新主机配置信息
				if (event.getData().getData().length != 0) {
					Connector.this.context4config.update(new ConfigImpl(event.getData().getPath(), Connector.this.serials.def4input().input(event.getData().getData(), Map.class)));
				}
				break;
			case NODE_REMOVED:
				// 删除主机配置信息
				Connector.this.context4config.remove(event.getData().getPath());
				break;
			default:
				Connector.LOGGER.warn("Nothing for " + event + " ... ");
			}
		}
	}

	/**
	 * 用于监听服务实例
	 * 
	 * @author KimShen
	 *
	 */
	private class InstanceListener implements TreeCacheListener {

		public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
			switch (event.getType()) {
			case NODE_ADDED:
				// 加载服务实例
				if (event.getData().getData().length != 0) {
					Connector.this.context4service.insert(new InstanceImpl(event.getData().getPath(), Connector.this.serials.def4input().input(event.getData().getData(), ZkSerial.class)));
				}
				break;
			case NODE_UPDATED:
				// 更新服务配置
				if (event.getData().getData().length != 0) {
					Connector.this.context4service.update(new InstanceImpl(event.getData().getPath(), Connector.this.serials.def4input().input(event.getData().getData(), ZkSerial.class)));
				}
				break;
			case NODE_REMOVED:
				// 删除
				Connector.this.context4service.remove(event.getData().getPath());
				break;
			default:
				Connector.LOGGER.warn("Nothing for " + event + " ... ");
			}
		}
	}

	/**
	 * 用于监听服务依赖
	 * 
	 * @author KimShen
	 *
	 */
	private class DependencyListener implements TreeCacheListener {

		public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
			switch (event.getType()) {
			case NODE_ADDED:
				// 加载服务依赖
				if (event.getData().getData().length != 0) {
					Connector.this.context4dependnecy.insert(new DependencyServiceImpl(event.getData().getPath(), Connector.this.serials.def4input().input(event.getData().getData(), ImportedService.class)));
				}
				break;
			case NODE_UPDATED:
				// Nothing
				break;
			case NODE_REMOVED:
				// 删除
				Connector.this.context4dependnecy.remove(event.getData().getPath());
				break;
			default:
				Connector.LOGGER.warn("Nothing for " + event + " ... ");
			}
		}
	}
}
