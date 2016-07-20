package com.kepler.admin.statistics.chart.impl;

import java.text.DecimalFormat;
import java.util.Iterator;

import com.kepler.admin.domain.Period;
import com.kepler.admin.mongo.Dictionary;
import com.kepler.admin.mongo.MongoConfig;
import com.kepler.admin.mongo.impl.MongoUtils;
import com.kepler.admin.statistics.Statistics;
import com.kepler.admin.statistics.chart.ChartDataset;
import com.kepler.admin.statistics.chart.ChartService;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * @author longyaokun 2015年12月21日
 */
public class ChartServiceImpl extends Statistics implements ChartService {

	private static final DBObject SORT = BasicDBObjectBuilder.start().add("$sort", BasicDBObjectBuilder.start(Dictionary.FIELD_PERIOD, 1).get()).get();

	public ChartServiceImpl(MongoConfig transferDay, MongoConfig transferHour, MongoConfig transferMinute) {
		super();
		super.configs(transferDay, transferHour, transferMinute);
	}

	private DBObject group(Period period) {
		BasicDBObjectBuilder _id = BasicDBObjectBuilder.start();
		// 服务维度
		_id.add(Dictionary.FIELD_SERVICE, "$" + Dictionary.FIELD_SERVICE);
		_id.add(Dictionary.FIELD_VERSION, "$" + Dictionary.FIELD_VERSION);
		// 周期维度
		_id.add(Dictionary.FIELD_PERIOD, "$" + Dictionary.FIELD_PERIOD);
		BasicDBObjectBuilder query = BasicDBObjectBuilder.start("_id", _id.get());
		// 统计(Sum)
		query.add(Dictionary.FIELD_RTT, BasicDBObjectBuilder.start("$sum", "$" + Dictionary.FIELD_RTT).get());
		query.add(Dictionary.FIELD_TOTAL, BasicDBObjectBuilder.start("$sum", "$" + Dictionary.FIELD_TOTAL).get());
		query.add(Dictionary.FIELD_TIMEOUT, BasicDBObjectBuilder.start("$sum", "$" + Dictionary.FIELD_TIMEOUT).get());
		query.add(Dictionary.FIELD_EXCEPTION, BasicDBObjectBuilder.start("$sum", "$" + Dictionary.FIELD_EXCEPTION).get());
		return BasicDBObjectBuilder.start().add("$group", query.get()).get();
	}

	private DBObject project(Period period) {
		BasicDBObjectBuilder query = BasicDBObjectBuilder.start();
		// 服务维度
		query.add(Dictionary.FIELD_SERVICE, "$_id." + Dictionary.FIELD_SERVICE);
		query.add(Dictionary.FIELD_VERSION, "$_id." + Dictionary.FIELD_VERSION);
		// 周期维度
		query.add(Dictionary.FIELD_PERIOD, "$_id." + Dictionary.FIELD_PERIOD);
		// 统计值
		query.add(Dictionary.FIELD_RTT, "$" + Dictionary.FIELD_RTT);
		query.add(Dictionary.FIELD_TOTAL, "$" + Dictionary.FIELD_TOTAL);
		query.add(Dictionary.FIELD_TIMEOUT, "$" + Dictionary.FIELD_TIMEOUT);
		query.add(Dictionary.FIELD_EXCEPTION, "$" + Dictionary.FIELD_EXCEPTION).get();
		return BasicDBObjectBuilder.start().add("$project", query.get()).get();
	}

	@Override
	public ChartDataset service(String service, String versionAndCatalog, Period period, int offset) {
		DBObject match = super.condition(service, versionAndCatalog, period, offset);
		DBObject group = this.group(period);
		DBObject project = this.project(period);
		// 聚合数据
		AggregationOutput aggregate = super.configs.get(period).collection().aggregate(match, group, project, ChartServiceImpl.SORT);
		return new MongoDataset(service + "-" + versionAndCatalog, period, aggregate);
	}

	@Override
	public ChartDataset instance(String sid, String service, String versionAndCatalog, Period period, int offset) {
		DBObject match = super.condition(sid, service, versionAndCatalog, period, offset);
		DBObject group = this.group(period);
		DBObject project = this.project(period);
		// 聚合数据
		AggregationOutput aggregate = super.configs.get(period).collection().aggregate(match, group, project, ChartServiceImpl.SORT);
		return new MongoDataset(service + "-" + versionAndCatalog, period, aggregate);
	}

	private class MongoDataset extends ChartDataset {

		private MongoDataset(String title, Period period, AggregationOutput output) {
			super(title);
			if (output != null && output.results() != null) {
				Iterator<DBObject> iterator = output.results().iterator();
				while (iterator.hasNext()) {
					DBObject object = iterator.next();
					// 毫秒转换为周期时间
					long time = period.convert(MongoUtils.asLong(object, Dictionary.FIELD_PERIOD));
					// 访问总量
					long total = MongoUtils.asLong(object, Dictionary.FIELD_TOTAL);
					super.total(new Object[] { time, total });
					// 错误 = 异常 + 超时
					super.error(new Object[] { time, MongoUtils.asLong(object, Dictionary.FIELD_EXCEPTION) + MongoUtils.asLong(object, Dictionary.FIELD_TIMEOUT) });
					super.elapse(new Object[] { time, (total == 0 ? 0 : Double.valueOf(new DecimalFormat("#.00").format(MongoUtils.asDouble(object, Dictionary.FIELD_RTT, 0) / total))) });
				}
			}
		}
	}
}
