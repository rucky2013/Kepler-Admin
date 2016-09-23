package com.kepler.admin.collector.trace;

import java.util.List;

import com.kepler.admin.domain.Period;
import com.kepler.admin.mongo.Dictionary;
import com.kepler.admin.mongo.MongoConfig;
import com.kepler.admin.trace.Feeder;
import com.kepler.annotation.Autowired;
import com.kepler.trace.TraceCause;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.BulkWriteOperation;

/**
 * @author KimShen
 *
 */
@Autowired
public class TraceHandler implements Feeder {

	private final MongoConfig trace;

	public TraceHandler(MongoConfig trace) {
		super();
		this.trace = trace;
	}

	@Override
	public void feed(List<TraceCause> cause) {
		// 开启Batch
		BulkWriteOperation batch = this.trace.collection().bulkWrite();
		for (TraceCause each : cause) {
			BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
			builder.add(Dictionary.FIELD_PERIOD, Period.SECOND.period(each.timestamp())).add(Dictionary.FIELD_SERVICE, each.service().service()).add(Dictionary.FIELD_VERSION, each.service().versionAndCatalog()).add(Dictionary.FIELD_METHOD, each.method());
			builder.add(Dictionary.FIELD_HOST_LOCAL, each.host());
			builder.add(Dictionary.FIELD_TRACE, each.trace());
			builder.add(Dictionary.FIELD_CAUSE, each.cause());
			batch.insert(builder.get());
		}
		batch.execute();
	}
}
