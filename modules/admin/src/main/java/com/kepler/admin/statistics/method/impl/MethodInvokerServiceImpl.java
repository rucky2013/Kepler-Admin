package com.kepler.admin.statistics.method.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.kepler.admin.domain.Period;
import com.kepler.admin.domain.ServiceAndVersion;
import com.kepler.admin.mongo.Dictionary;
import com.kepler.admin.mongo.MongoConfig;
import com.kepler.admin.mongo.impl.MongoUtils;
import com.kepler.admin.resource.instance.InstanceFinder;
import com.kepler.admin.resource.instance.impl.InstanceServices;
import com.kepler.admin.statistics.Statistics;
import com.kepler.admin.statistics.method.MethodInvoker;
import com.kepler.admin.statistics.method.MethodInvokerService;
import com.kepler.admin.statistics.method.SortBy;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * @author longyaokun 2015年12月21日
 */
public class MethodInvokerServiceImpl extends Statistics implements MethodInvokerService {

	private final InstanceFinder instanceFinder;
	
	public MethodInvokerServiceImpl(MongoConfig transferDay, MongoConfig transferHour, MongoConfig transferMinute, InstanceFinder instanceFinder) {
		super();
		super.configs(transferDay, transferHour, transferMinute);
		this.instanceFinder = instanceFinder;
	}

	@Override
	public List<MethodInvoker> methods4Service(String service, String versionAndCatalog, Period period, int offset) {
		DBObject condition = super.condition(service, versionAndCatalog, period, offset);
		// Group 聚合ID
		BasicDBObjectBuilder _id = BasicDBObjectBuilder.start();
		_id.add(Dictionary.FIELD_SERVICE, "$" + Dictionary.FIELD_SERVICE);
		_id.add(Dictionary.FIELD_VERSION, "$" + Dictionary.FIELD_VERSION);
		_id.add(Dictionary.FIELD_METHOD, "$" + Dictionary.FIELD_METHOD);
		// Group SUM
		BasicDBObjectBuilder builder4group = BasicDBObjectBuilder.start();
		builder4group.add("_id", _id.get());
		builder4group.add(Dictionary.FIELD_RTT, new BasicDBObject("$sum", "$" + Dictionary.FIELD_RTT));
		builder4group.add(Dictionary.FIELD_TOTAL, new BasicDBObject("$sum", "$" + Dictionary.FIELD_TOTAL));
		builder4group.add(Dictionary.FIELD_TIMEOUT, new BasicDBObject("$sum", "$" + Dictionary.FIELD_TIMEOUT));
		builder4group.add(Dictionary.FIELD_EXCEPTION, new BasicDBObject("$sum", "$" + Dictionary.FIELD_EXCEPTION));
		DBObject group = BasicDBObjectBuilder.start("$group", builder4group.get()).get();
		// Group Project
		BasicDBObjectBuilder builder4project = BasicDBObjectBuilder.start();
		builder4project.add(Dictionary.FIELD_SERVICE, "$_id." + Dictionary.FIELD_SERVICE);
		builder4project.add(Dictionary.FIELD_VERSION, "$_id." + Dictionary.FIELD_VERSION);
		builder4project.add(Dictionary.FIELD_METHOD, "$_id." + Dictionary.FIELD_METHOD);
		builder4project.add(Dictionary.FIELD_RTT, "$" + Dictionary.FIELD_RTT);
		builder4project.add(Dictionary.FIELD_TOTAL, "$" + Dictionary.FIELD_TOTAL);
		builder4project.add(Dictionary.FIELD_TIMEOUT, "$" + Dictionary.FIELD_TIMEOUT);
		builder4project.add(Dictionary.FIELD_EXCEPTION, "$" + Dictionary.FIELD_EXCEPTION);
		DBObject project = BasicDBObjectBuilder.start("$project", builder4project.get()).get();
		return new MongoMethods(offset, super.configs.get(period).collection().aggregate(condition, group, project));
	}

	@Override
	public List<MethodInvoker> methods(String sid, String service, String versionAndCatalog, Period period, int offset) {
		DBObject condition = super.condition(sid, service, versionAndCatalog, period, offset);
		// Group 聚合ID
		BasicDBObjectBuilder _id = BasicDBObjectBuilder.start();
		_id.add(Dictionary.FIELD_METHOD, "$" + Dictionary.FIELD_METHOD);
		_id.add(Dictionary.FIELD_SERVICE, "$" + Dictionary.FIELD_SERVICE);
		_id.add(Dictionary.FIELD_VERSION, "$" + Dictionary.FIELD_VERSION);
		_id.add(Dictionary.FIELD_HOST_TARGET_SID, "$" + Dictionary.FIELD_HOST_TARGET_SID);
		// Group SUM
		BasicDBObjectBuilder builder4group = BasicDBObjectBuilder.start();
		builder4group.add("_id", _id.get());
		builder4group.add(Dictionary.FIELD_RTT, new BasicDBObject("$sum", "$" + Dictionary.FIELD_RTT));
		builder4group.add(Dictionary.FIELD_TOTAL, new BasicDBObject("$sum", "$" + Dictionary.FIELD_TOTAL));
		builder4group.add(Dictionary.FIELD_TIMEOUT, new BasicDBObject("$sum", "$" + Dictionary.FIELD_TIMEOUT));
		builder4group.add(Dictionary.FIELD_EXCEPTION, new BasicDBObject("$sum", "$" + Dictionary.FIELD_EXCEPTION));
		DBObject group = BasicDBObjectBuilder.start("$group", builder4group.get()).get();
		// Group Project
		BasicDBObjectBuilder builder4project = BasicDBObjectBuilder.start();
		builder4project.add(Dictionary.FIELD_SERVICE, "$_id." + Dictionary.FIELD_SERVICE);
		builder4project.add(Dictionary.FIELD_VERSION, "$_id." + Dictionary.FIELD_VERSION);
		builder4project.add(Dictionary.FIELD_HOST_TARGET_SID, "$_id." + Dictionary.FIELD_HOST_TARGET_SID);
		builder4project.add(Dictionary.FIELD_METHOD, "$_id." + Dictionary.FIELD_METHOD);
		builder4project.add(Dictionary.FIELD_RTT, "$" + Dictionary.FIELD_RTT);
		builder4project.add(Dictionary.FIELD_TOTAL, "$" + Dictionary.FIELD_TOTAL);
		builder4project.add(Dictionary.FIELD_TIMEOUT, "$" + Dictionary.FIELD_TIMEOUT);
		builder4project.add(Dictionary.FIELD_EXCEPTION, "$" + Dictionary.FIELD_EXCEPTION);
		DBObject project = BasicDBObjectBuilder.start("$project", builder4project.get()).get();
		return new MongoMethods(offset, super.configs.get(period).collection().aggregate(condition, group, project));
	}

	private class MongoMethods extends ArrayList<MethodInvoker> {

		private static final long serialVersionUID = 1L;

		private MongoMethods(int offset, AggregationOutput output) {
			if (output != null && output.results() != null) {
				Iterator<DBObject> iterator = output.results().iterator();
				while (iterator.hasNext()) {
					DBObject object = iterator.next();
					String service = MongoUtils.asString(object, Dictionary.FIELD_SERVICE);
					String version = MongoUtils.asString(object, Dictionary.FIELD_VERSION);
					String name = MongoUtils.asString(object, Dictionary.FIELD_METHOD);
					long total = MongoUtils.asLong(object, Dictionary.FIELD_TOTAL) / (1 + offset);
					long timeout = MongoUtils.asLong(object, Dictionary.FIELD_TIMEOUT) / (1 + offset);
					long exception = MongoUtils.asLong(object, Dictionary.FIELD_EXCEPTION) / (1 + offset);
					double rtt = total == 0 ? 0 : Double.valueOf(new DecimalFormat("#.00").format(MongoUtils.asDouble(object, Dictionary.FIELD_RTT, 0) / (1 + offset) / total));
					super.add(new MethodInvoker(service, version, name, total, timeout, exception, rtt));
				}
			}
		}
	}

	@Override
	public List<MethodInvoker> methods4Group(String group, Period period, int offset, SortBy sortBy) {
		List<MethodInvoker> methods4Group = new ArrayList<>();
		for(ServiceAndVersion serviceAndVersion : new InstanceServices(this.instanceFinder.group(group))) {
			List<MethodInvoker> methods4Service = this.methods4Service(serviceAndVersion.getService(), serviceAndVersion.getVersionAndCatalog(), period, offset);
			methods4Group.addAll(methods4Service);
		}
		Collections.sort(methods4Group, sortBy.comparator());
		return methods4Group;
	}
}
