package com.kepler.admin.mongo.impl;

import java.net.UnknownHostException;
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

	private static final String HOST_SEPARATOE =",";
	
	private static final String HOST_PORT_SEPARATOE =":";
	
	// 配置项开始
	private String host;
	
	private String username;
	
	private String password;
	
	private String db;
	// 配置项结束

	private MongoClient client;

	@Override
	public DB getObject() throws Exception {
		List<ServerAddress> serverAddresses = this.getAddesses();
		List<MongoCredential> credentials = this.createCredentials(username, db, password);
		client = this.createMongoClient(serverAddresses, credentials);
		return client.getDB(db);
	}

	private MongoClient createMongoClient(List<ServerAddress> serverAddresses, List<MongoCredential> credentials) {
		if (serverAddresses.size() > 1) {
			if (CollectionUtils.isEmpty(credentials)) {
				return new MongoClient(serverAddresses);
			}
			return new MongoClient(serverAddresses, credentials);
		} else {
			if (CollectionUtils.isEmpty(credentials)) {
				return new MongoClient(serverAddresses.get(0));
			}
			return new MongoClient(serverAddresses.get(0), credentials);
		}
	}

	private List<MongoCredential> createCredentials(String username, String db, String password) {
		if (this.useAuthentication()) {
			return Arrays.asList(MongoCredential.createCredential(username, db, password.toCharArray()));
		} else {
			return null;
		}
	}

	private List<ServerAddress> getAddesses() throws NumberFormatException, UnknownHostException {
		List<String> hosts = Arrays.asList(this.host.split(HOST_SEPARATOE));
		List<ServerAddress> addresses = new ArrayList<>();
		for (String host : hosts) {
			addresses.add(this.getAddress(host));
		}
		return addresses;
	}

	private ServerAddress getAddress(String host) throws NumberFormatException, UnknownHostException {
		String[] hostAndPort = host.split(HOST_PORT_SEPARATOE);
		return new ServerAddress(hostAndPort[0], Integer.valueOf(hostAndPort[1]));
	}

	public void destroy() {
		client.close();
	}

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

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDb() {
		return db;
	}

	public void setDb(String db) {
		this.db = db;
	}
}
