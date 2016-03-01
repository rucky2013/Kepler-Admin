package com.kepler.collector;

import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.kepler.annotation.Autowired;
import com.kepler.host.Host;
import com.kepler.management.status.Feeder;
import com.kepler.mongo.Dictionary;
import com.kepler.mongo.MongoConfig;
import com.mongodb.BasicDBObjectBuilder;

/**
 * @author kim 2015年7月22日
 */
@Autowired
public class StatusHandler implements Feeder {

	private final MongoConfig status;

	public StatusHandler(MongoConfig status) {
		super();
		this.status = status;
	}

	// db.status.ensureIndex({"host_local_sid":1, "minute":1})
	public void feed(Host host, Map<String, Object> status) {
		long minute = TimeUnit.MINUTES.convert(System.currentTimeMillis() + TimeZone.getDefault().getOffset(System.currentTimeMillis()), TimeUnit.MILLISECONDS);
		this.status.collection().update(BasicDBObjectBuilder.start().add(Dictionary.FIELD_HOST_LOCAL_SID, host.sid()).add(Dictionary.FIELD_PERIOD_MINUTE, minute).get(), BasicDBObjectBuilder.start("$set", BasicDBObjectBuilder.start().add(Dictionary.FIELD_HOST_LOCAL, host.address()).add(Dictionary.FIELD_HOST_LOCAL_SID, host.sid()).add(Dictionary.FIELD_HOST_LOCAL_PID, host.pid()).add(Dictionary.FIELD_HOST_LOCAL_TAG, host.tag()).add(Dictionary.FIELD_HOST_LOCAL_GROUP, host.group()).add(Dictionary.FIELD_STATUS, status).add(Dictionary.FIELD_PERIOD_MINUTE, minute).get()).get(), true, false);
	}
}
