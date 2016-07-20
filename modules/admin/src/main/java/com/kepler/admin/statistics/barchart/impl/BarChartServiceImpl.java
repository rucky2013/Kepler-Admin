package com.kepler.admin.statistics.barchart.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.kepler.admin.domain.Period;
import com.kepler.admin.mongo.Dictionary;
import com.kepler.admin.mongo.MongoConfig;
import com.kepler.admin.mongo.impl.MongoUtils;
import com.kepler.admin.resource.status.TerminalStatusFinder;
import com.kepler.admin.statistics.Statistics;
import com.kepler.admin.statistics.barchart.BarChartData;
import com.kepler.admin.statistics.barchart.BarChatService;
import com.kepler.admin.statistics.barchart.Series;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * @author longyaokun
 * @date 2016年3月21日
 *
 */
public class BarChartServiceImpl extends Statistics implements BarChatService {

	private final TerminalStatusFinder finder;

	public BarChartServiceImpl(MongoConfig transferDay, MongoConfig transferHour, MongoConfig transferMinute, TerminalStatusFinder finder) {
		super();
		super.configs(transferDay, transferHour, transferMinute);
		this.finder = finder;
	}

	@Override
	public List<BarChartData> statistics4Clients(String service, String versionAndCatalog, String method, Period periodType, long period) {
		return new MongoDataSet(super.configs.get(periodType).collection().aggregate(
		        this.match(service, versionAndCatalog, method, period), this.group(), this.project()));
	}

	private DBObject match(String service, String versionAndCatalog, String method, long period) {
		BasicDBObjectBuilder query = BasicDBObjectBuilder.start();
		query.add(Dictionary.FIELD_SERVICE, service);
		query.add(Dictionary.FIELD_VERSION, versionAndCatalog);
		query.add(Dictionary.FIELD_METHOD, method);
		query.add(Dictionary.FIELD_PERIOD, period);
		return BasicDBObjectBuilder.start().add("$match", query.get()).get();
	}

	private DBObject group() {
		BasicDBObjectBuilder _id = BasicDBObjectBuilder.start();
		_id.add(Dictionary.FIELD_SERVICE, "$" + Dictionary.FIELD_SERVICE);
		_id.add(Dictionary.FIELD_VERSION, "$" + Dictionary.FIELD_VERSION);
		_id.add(Dictionary.FIELD_HOST_LOCAL_SID, "$" + Dictionary.FIELD_HOST_LOCAL_SID);
		BasicDBObjectBuilder query = BasicDBObjectBuilder.start("_id", _id.get());
		// 统计(Sum)
		query.add(Dictionary.FIELD_RTT, BasicDBObjectBuilder.start("$sum", "$" + Dictionary.FIELD_RTT).get());
		query.add(Dictionary.FIELD_TOTAL, BasicDBObjectBuilder.start("$sum", "$" + Dictionary.FIELD_TOTAL).get());
		query.add(Dictionary.FIELD_TIMEOUT, BasicDBObjectBuilder.start("$sum", "$" + Dictionary.FIELD_TIMEOUT).get());
		query.add(Dictionary.FIELD_EXCEPTION, BasicDBObjectBuilder.start("$sum", "$" + Dictionary.FIELD_EXCEPTION).get());
		return BasicDBObjectBuilder.start().add("$group", query.get()).get();
	}

	private DBObject project() {
		BasicDBObjectBuilder query = BasicDBObjectBuilder.start();
		query.add(Dictionary.FIELD_SERVICE, "$_id." + Dictionary.FIELD_SERVICE);
		query.add(Dictionary.FIELD_VERSION, "$_id." + Dictionary.FIELD_VERSION);
		query.add(Dictionary.FIELD_HOST_LOCAL_SID, "$_id." + Dictionary.FIELD_HOST_LOCAL_SID);
		// 统计值
		query.add(Dictionary.FIELD_RTT, "$" + Dictionary.FIELD_RTT);
		query.add(Dictionary.FIELD_TOTAL, "$" + Dictionary.FIELD_TOTAL);
		query.add(Dictionary.FIELD_TIMEOUT, "$" + Dictionary.FIELD_TIMEOUT);
		query.add(Dictionary.FIELD_EXCEPTION, "$" + Dictionary.FIELD_EXCEPTION).get();
		return BasicDBObjectBuilder.start().add("$project", query.get()).get();
	}

	class MongoDataSet extends ArrayList<BarChartData> {

        private static final long serialVersionUID = 1L;

		public MongoDataSet(AggregationOutput output) {
			super();
			BarChartData pv = new BarChartData();
			BarChartData error = new BarChartData();
			BarChartData rtt = new BarChartData();
			
			List<Long> pvs = new ArrayList<Long>();
			List<Long> errors = new ArrayList<Long>();
			List<Double> rtts = new ArrayList<Double>();
			
			Iterator<DBObject> iterator = output.results().iterator();
			while (iterator.hasNext()) {
				DBObject current = iterator.next();
				String host = BarChartServiceImpl.this.finder.sid(
				        MongoUtils.asString(current, Dictionary.FIELD_HOST_LOCAL_SID)).getHost();
				pv.addCategory(host);
				error.addCategory(host);
				rtt.addCategory(host);
				long total = MongoUtils.asLong(current, Dictionary.FIELD_TOTAL);
				pvs.add(total);
				errors.add(MongoUtils.asLong(current, Dictionary.FIELD_EXCEPTION)
				        + MongoUtils.asLong(current, Dictionary.FIELD_TIMEOUT));
				rtts.add((total == 0 ? 0 : Double.valueOf(new DecimalFormat("#.00").format(MongoUtils.asDouble(current,
				        Dictionary.FIELD_RTT, 0) / total))));
			}
			pv.addSeries(new Series("pv", pvs.toArray()));
			error.addSeries(new Series("error", errors.toArray()));
			rtt.addSeries(new Series("rtt", rtts.toArray()));
			super.add(pv);
			super.add(error);
			super.add(rtt);
		}
	}

}
