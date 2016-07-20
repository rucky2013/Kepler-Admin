package com.kepler.admin.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.FactoryBean;

import com.kepler.admin.resource.impl.Connector;
import com.kepler.config.PropertiesUtils;
import com.kepler.zookeeper.ZkFactory;

/**
 * Curator工厂 
 * 
 * @author kim 2015年12月17日
 */
public class CuratorFactory implements FactoryBean<CuratorFramework> {

	/**
	 * Curator基础重连间隔(指数回退)
	 */
	private static final int INTERVAL = PropertiesUtils.get(Connector.class.getName().toLowerCase() + ".retry", 100);

	private final CuratorFramework client = CuratorFrameworkFactory.newClient(ZkFactory.HOST, new ExponentialBackoffRetry(CuratorFactory.INTERVAL, Integer.MAX_VALUE));

	@Override
	public CuratorFramework getObject() throws Exception {
		this.client.start();
		return this.client;
	}

	public void destroy() {
		this.client.close();
	}

	@Override
	public Class<?> getObjectType() {
		return CuratorFramework.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}
