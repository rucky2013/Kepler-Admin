package com.kepler.admin.generic.impl;

import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kepler.admin.generic.GenericHistory;
import com.kepler.admin.generic.GenericRequest;
import com.kepler.admin.mongo.Dictionary;
import com.kepler.admin.mongo.MongoConfig;
import com.kepler.admin.mongo.impl.MongoUtils;
import com.kepler.service.Service;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * @author KimShen
 *
 */
public class DefaultHistory implements GenericHistory {

	private static final String FIELD_DATAS = "datas";

	private static final String FIELD_CREATED = "created";

	private static final String FIELD_CATALOG = "catalog";

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final DBObject SORT = BasicDBObjectBuilder.start(DefaultHistory.FIELD_CREATED, -1).get();

	private final MongoConfig config;

	public DefaultHistory(MongoConfig config) {
		super();
		this.config = config;
	}

	@Override
	public GenericRequest get(Service service, String method) throws Exception {
		BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
		builder.add(Dictionary.FIELD_METHOD, method);
		builder.add(Dictionary.FIELD_SERVICE, service.service());
		builder.add(Dictionary.FIELD_VERSION, service.version());
		builder.add(DefaultHistory.FIELD_CATALOG, service.catalog());
		DBObject request = this.config.collection().find(builder.get()).sort(DefaultHistory.SORT).one();
		return request != null ? DefaultHistory.MAPPER.readValue(MongoUtils.asString(request, DefaultHistory.FIELD_DATAS), DefaultRequest.class) : null;
	}

	@Override
	public void set(GenericRequest request) throws Exception {
		BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
		builder.add(DefaultHistory.FIELD_CREATED, new Date());
		builder.add(Dictionary.FIELD_METHOD, request.getMethod());
		builder.add(Dictionary.FIELD_SERVICE, request.metadata().service());
		builder.add(Dictionary.FIELD_VERSION, request.metadata().version());
		builder.add(DefaultHistory.FIELD_CATALOG, request.metadata().catalog());
		builder.add(DefaultHistory.FIELD_DATAS, DefaultHistory.MAPPER.writeValueAsString(request));
		this.config.collection().save(builder.get());
	}
}
