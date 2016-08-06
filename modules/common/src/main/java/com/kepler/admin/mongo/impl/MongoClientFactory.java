package com.kepler.admin.mongo.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.CollectionUtils;

import com.kepler.org.apache.commons.lang.StringUtils;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class MongoClientFactory implements FactoryBean<DB> {

	private static final String HOST_SEPARATOE = ",";

	private static final String HOST_PORT_SEPARATOE = ":";

	private final String username;

	private final String password;

	private final String host;

	private final String db;

	private MongoClient client;

	public MongoClientFactory(String db, String host, String username, String password) {
		super();
		this.db = db;
		this.host = host;
		this.username = username;
		this.password = password;
	}

	@Override
	public DB getObject() throws Exception {
		// 服务地址
		List<ServerAddress> addresses = this.address();
		// 权鉴凭证
		List<MongoCredential> credentials = this.credentials(this.username, this.db, this.password);
		this.client = this.create(addresses, credentials);
		// 获取实际DB
		return this.client.getDB(this.db);
	}

	public void destroy() {
		this.client.close();
	}

	/**
	 * 创建Client
	 * 
	 * @param address 服务器地址
	 * @param credentials 权鉴凭证
	 * @return
	 */
	private MongoClient create(List<ServerAddress> address, List<MongoCredential> credentials) {
		return CollectionUtils.isEmpty(credentials) ? new MongoClient(address) : new MongoClient(address, credentials);
	}

	/**
	 * 创建权鉴信息
	 * 
	 * @param username
	 * @param db
	 * @param password
	 * @return
	 */
	private List<MongoCredential> credentials(String username, String db, String password) {
		return this.useAuthentication() ? Arrays.asList(MongoCredential.createCredential(username, db, password.toCharArray())) : null;
	}

	private List<ServerAddress> address() throws Exception {
		List<String> hosts = Arrays.asList(this.host.split(MongoClientFactory.HOST_SEPARATOE));
		List<ServerAddress> addresses = new ArrayList<>();
		for (String host : hosts) {
			addresses.add(this.generate(host));
		}
		return addresses;
	}

	private ServerAddress generate(String host) throws Exception {
		String[] hostAndPort = host.split(MongoClientFactory.HOST_PORT_SEPARATOE);
		return new ServerAddress(hostAndPort[0], Integer.valueOf(hostAndPort[1]));
	}

	/**
	 * 是否需要权鉴
	 * 
	 * @return
	 */
	private boolean useAuthentication() {
		return StringUtils.isNotEmpty(this.username);
	}

	@Override
	public Class<DB> getObjectType() {
		return DB.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}
}
