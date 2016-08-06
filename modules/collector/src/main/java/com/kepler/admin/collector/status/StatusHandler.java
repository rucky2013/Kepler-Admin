package com.kepler.admin.collector.status;

import java.util.Map;

import com.kepler.admin.domain.Period;
import com.kepler.admin.mongo.Dictionary;
import com.kepler.admin.mongo.MongoConfig;
import com.kepler.admin.status.Feeder;
import com.kepler.annotation.Autowired;
import com.kepler.host.Host;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * 动态状态收集
 * 
 * @author kim 2015年7月22日
 */
@Autowired
public class StatusHandler implements Feeder {

	/**
	 * 索引, Peroid - SID
	 */
	private static final DBObject INDEX = BasicDBObjectBuilder.start().add(Dictionary.FIELD_PERIOD, 1).add(Dictionary.FIELD_HOST_LOCAL_SID, 1).get();

	private final MongoConfig status;

	public StatusHandler(MongoConfig status) {
		super();
		this.status.collection().index(StatusHandler.INDEX);
		this.status = status;
	}

	public void feed(Host host, Map<String, Object> status) {
		// Query (触发周期 + SID)
		BasicDBObjectBuilder query = BasicDBObjectBuilder.start();
		query.add(Dictionary.FIELD_PERIOD, Period.MINUTE.period());
		query.add(Dictionary.FIELD_HOST_LOCAL_SID, host.sid());
		// Update (Status)
		BasicDBObjectBuilder update = BasicDBObjectBuilder.start();
		update.add(Dictionary.FIELD_STATUS, status);
		// 每次更新覆盖指定周期主机的Status
		this.status.collection().update(query.get(), BasicDBObjectBuilder.start("$set", update.get()).get(), true, false);
	}
}
