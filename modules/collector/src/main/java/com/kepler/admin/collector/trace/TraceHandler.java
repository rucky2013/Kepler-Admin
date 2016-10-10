package com.kepler.admin.collector.trace;

import java.util.List;

import com.kepler.admin.domain.Period;
import com.kepler.admin.mongo.Dictionary;
import com.kepler.admin.mongo.MongoConfig;
import com.kepler.admin.trace.Feeder;
import com.kepler.annotation.Autowired;
import com.kepler.host.Host;
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
	public void feed(Host host, List<TraceCause> cause) {
		long current = System.currentTimeMillis();
		// 开启Batch
		BulkWriteOperation batch = this.trace.collection().bulkWrite();
		for (TraceCause each : cause) {
			BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
			builder.add(Dictionary.FIELD_SERVICE, each.service().service()).add(Dictionary.FIELD_VERSION, each.service().versionAndCatalog()).add(Dictionary.FIELD_METHOD, each.method());
			// 附加数据 主机 分组 名称
			builder.add(Dictionary.FIELD_HOST_LOCAL, host.address()).add(Dictionary.FIELD_HOST_LOCAL_NAME, host.name()).add(Dictionary.FIELD_HOST_LOCAL_GROUP, host.group());
			// 发生时间及入库时间
			builder.add(Dictionary.FIELD_PERIOD, Period.SECOND.period(each.timestamp())).add(Dictionary.FIELD_CURRENT, current);
			// 附加数据 原因
			builder.add(Dictionary.FIELD_TRACE, each.trace()).add(Dictionary.FIELD_CAUSE, each.cause());
			batch.insert(builder.get());
		}
		batch.execute();
	}
}
