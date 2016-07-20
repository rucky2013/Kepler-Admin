package com.kepler.admin.statistics;

import java.util.HashMap;
import java.util.Map;

import com.kepler.admin.domain.Period;
import com.kepler.admin.mongo.Dictionary;
import com.kepler.admin.mongo.MongoConfig;
import com.kepler.config.PropertiesUtils;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * @author kim 2015年12月21日
 */
abstract public class Statistics {

	/**
	 * 周期范围最大值
	 */
	private static final int MAX = PropertiesUtils.get(Statistics.class.getName().toLowerCase() + ".max", 240);

	/**
	 * 周期对应数据源
	 */
	protected final Map<Period, MongoConfig> configs = new HashMap<Period, MongoConfig>();

	protected void configs(MongoConfig transferDay, MongoConfig transferHour, MongoConfig transferMinute) {
		// 数据源
		this.configs.put(Period.DAY, transferDay);
		this.configs.put(Period.HOUR, transferHour);
		this.configs.put(Period.MINUTE, transferMinute);
	}

	private BasicDBObjectBuilder condition4base(BasicDBObjectBuilder condition, String service, String versionAndCatalog, Period period, int offset) {
		// 时间偏移量 - 至今 (For index)
		condition.add(Dictionary.FIELD_PERIOD, BasicDBObjectBuilder.start("$gte", period.period() - Math.min(Statistics.MAX, offset)).get());
		// 服务 Version
		condition.add(Dictionary.FIELD_SERVICE, BasicDBObjectBuilder.start("$eq", service).get());
		condition.add(Dictionary.FIELD_VERSION, BasicDBObjectBuilder.start("$eq", versionAndCatalog).get());
		return condition;
	}

	/**
	 * SID + Service + Version + 周期偏移
	 * 
	 * @param sid
	 * @param service
	 * @param versionAndCatalog
	 * @param period
	 * @param offset
	 * @return
	 */
	protected DBObject condition(String sid, String service, String versionAndCatalog, Period period, int offset) {
		BasicDBObjectBuilder condition = this.condition4base(BasicDBObjectBuilder.start(), service, versionAndCatalog, period, offset);
		condition.add(Dictionary.FIELD_HOST_TARGET_SID, BasicDBObjectBuilder.start("$eq", sid).get());
		return BasicDBObjectBuilder.start().add("$match", condition.get()).get();
	}

	/**
	 * Service + Version + 周期偏移
	 *  
	 * @param service
	 * @param versionAndCatalog
	 * @param period
	 * @param offset
	 * @return
	 */
	protected DBObject condition(String service, String versionAndCatalog, Period period, int offset) {
		BasicDBObjectBuilder condition = this.condition4base(BasicDBObjectBuilder.start(), service, versionAndCatalog, period, offset);
		return BasicDBObjectBuilder.start().add("$match", condition.get()).get();
	}
}
