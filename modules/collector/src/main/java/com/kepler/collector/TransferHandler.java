package com.kepler.collector;

import java.util.Collection;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.kepler.annotation.Autowired;
import com.kepler.management.transfer.Feeder;
import com.kepler.management.transfer.Transfer;
import com.kepler.management.transfer.Transfers;
import com.kepler.mongo.Dictionary;
import com.kepler.mongo.MongoConfig;
import com.mongodb.BasicDBObjectBuilder;

/**
 * @author kim 2015年7月22日
 */
@Autowired
public class TransferHandler implements Feeder {

	private final MongoConfig transfers_minute;

	private final MongoConfig transfers_hour;

	private final MongoConfig transfers_day;

	public TransferHandler(MongoConfig transfers_minute, MongoConfig transfers_hour, MongoConfig transfers_day) {
		super();
		this.transfers_minute = transfers_minute;
		this.transfers_hour = transfers_hour;
		this.transfers_day = transfers_day;
	}

	@Override
	// db.transfer.minute.ensureIndex({"service":1, "version":1, "minute":1, "host_target_sid":1, "method":1, "host_local":1,"host_target_sid":1})
	// db.transfer.hour.ensureIndex({"service":1, "version":1, "hour":1, "host_target_sid":1, "method":1, "host_local":1,"host_target_sid":1})
	// db.transfer.day.ensureIndex({"service":1, "version":1, "day":1, "host_target_sid":1, "method":1, "host_local":1,"host_target_sid":1})
	public void feed(long timestamp, Collection<Transfers> transfers) {
		long offset = timestamp + TimeZone.getDefault().getOffset(timestamp);
		long minute = TimeUnit.MINUTES.convert(offset, TimeUnit.MILLISECONDS);
		long hour = TimeUnit.HOURS.convert(offset, TimeUnit.MILLISECONDS);
		long day = TimeUnit.DAYS.convert(offset, TimeUnit.MILLISECONDS);
		for (Transfers each : transfers) {
			for (Transfer transfer : each.transfers()) {
				// Query: minute, service, version, host_target_sid, method,
				this.transfers_minute.collection().update(BasicDBObjectBuilder.start().add(Dictionary.FIELD_SERVICE, each.service()).add(Dictionary.FIELD_VERSION, each.version()).add(Dictionary.FIELD_PERIOD_MINUTE, minute).add(Dictionary.FIELD_HOST_TARGET_SID, transfer.target().sid()).add(Dictionary.FIELD_METHOD, each.method()).add(Dictionary.FIELD_HOST_LOCAL, transfer.local().address()).add(Dictionary.FIELD_HOST_TARGET_SID, transfer.target().sid()).get(), BasicDBObjectBuilder.start("$set", BasicDBObjectBuilder.start(Dictionary.FIELD_SERVICE, each.service()).add(Dictionary.FIELD_VERSION, each.version()).add(Dictionary.FIELD_METHOD, each.method()).add(Dictionary.FIELD_HOST_LOCAL, transfer.local().address()).add(Dictionary.FIELD_HOST_LOCAL_TAG, transfer.local().tag()).add(Dictionary.FIELD_HOST_LOCAL_PID, transfer.local().pid()).add(Dictionary.FIELD_HOST_LOCAL_GROUP, transfer.local().group()).add(Dictionary.FIELD_HOST_TARGET, transfer.target().address()).add(Dictionary.FIELD_HOST_TARGET_PID, transfer.target().pid()).add(Dictionary.FIELD_HOST_TARGET_GROUP, transfer.target().group()).add(Dictionary.FIELD_HOST_TARGET_SID, transfer.target().sid()).add(Dictionary.FIELD_PERIOD_MINUTE, minute).add(Dictionary.FIELD_RTT, transfer.rtt()).add(Dictionary.FIELD_TIMEOUT, transfer.timeout()).add(Dictionary.FIELD_TOTAL, transfer.total()).add(Dictionary.FIELD_EXCEPTION, transfer.exception()).get()).get(), true, false);
				this.transfers_hour.collection().update(BasicDBObjectBuilder.start().add(Dictionary.FIELD_SERVICE, each.service()).add(Dictionary.FIELD_VERSION, each.version()).add(Dictionary.FIELD_PERIOD_HOUR, hour).add(Dictionary.FIELD_HOST_TARGET_SID, transfer.target().sid()).add(Dictionary.FIELD_METHOD, each.method()).add(Dictionary.FIELD_HOST_LOCAL, transfer.local().address()).add(Dictionary.FIELD_HOST_TARGET_SID, transfer.target().sid()).get(), BasicDBObjectBuilder.start("$set", BasicDBObjectBuilder.start(Dictionary.FIELD_SERVICE, each.service()).add(Dictionary.FIELD_VERSION, each.version()).add(Dictionary.FIELD_METHOD, each.method()).add(Dictionary.FIELD_HOST_LOCAL, transfer.local().address()).add(Dictionary.FIELD_HOST_LOCAL_TAG, transfer.local().tag()).add(Dictionary.FIELD_HOST_LOCAL_PID, transfer.local().pid()).add(Dictionary.FIELD_HOST_LOCAL_GROUP, transfer.local().group()).add(Dictionary.FIELD_HOST_TARGET, transfer.target().address()).add(Dictionary.FIELD_HOST_TARGET_PID, transfer.target().pid()).add(Dictionary.FIELD_HOST_TARGET_GROUP, transfer.target().group()).add(Dictionary.FIELD_HOST_TARGET_SID, transfer.target().sid()).add(Dictionary.FIELD_PERIOD_HOUR, hour).get()).add("$inc", BasicDBObjectBuilder.start(Dictionary.FIELD_RTT, transfer.rtt()).add(Dictionary.FIELD_TIMEOUT, transfer.timeout()).add(Dictionary.FIELD_TOTAL, transfer.total()).add(Dictionary.FIELD_EXCEPTION, transfer.exception()).get()).get(), true, false);
				this.transfers_day.collection().update(BasicDBObjectBuilder.start().add(Dictionary.FIELD_SERVICE, each.service()).add(Dictionary.FIELD_VERSION, each.version()).add(Dictionary.FIELD_PERIOD_DAY, day).add(Dictionary.FIELD_HOST_TARGET_SID, transfer.target().sid()).add(Dictionary.FIELD_METHOD, each.method()).add(Dictionary.FIELD_HOST_LOCAL, transfer.local().address()).add(Dictionary.FIELD_HOST_TARGET_SID, transfer.target().sid()).get(), BasicDBObjectBuilder.start("$set", BasicDBObjectBuilder.start(Dictionary.FIELD_SERVICE, each.service()).add(Dictionary.FIELD_VERSION, each.version()).add(Dictionary.FIELD_METHOD, each.method()).add(Dictionary.FIELD_HOST_LOCAL, transfer.local().address()).add(Dictionary.FIELD_HOST_LOCAL_TAG, transfer.local().tag()).add(Dictionary.FIELD_HOST_LOCAL_PID, transfer.local().pid()).add(Dictionary.FIELD_HOST_LOCAL_GROUP, transfer.local().group()).add(Dictionary.FIELD_HOST_TARGET, transfer.target().address()).add(Dictionary.FIELD_HOST_TARGET_PID, transfer.target().pid()).add(Dictionary.FIELD_HOST_TARGET_GROUP, transfer.target().group()).add(Dictionary.FIELD_HOST_TARGET_SID, transfer.target().sid()).add(Dictionary.FIELD_PERIOD_DAY, day).get()).add("$inc", BasicDBObjectBuilder.start(Dictionary.FIELD_RTT, transfer.rtt()).add(Dictionary.FIELD_TIMEOUT, transfer.timeout()).add(Dictionary.FIELD_TOTAL, transfer.total()).add(Dictionary.FIELD_EXCEPTION, transfer.exception()).get()).get(), true, false);
			}
		}
	}
}
