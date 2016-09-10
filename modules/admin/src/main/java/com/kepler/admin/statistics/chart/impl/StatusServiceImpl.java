package com.kepler.admin.statistics.chart.impl;

import java.util.List;

import com.kepler.admin.domain.Period;
import com.kepler.admin.mongo.Dictionary;
import com.kepler.admin.mongo.MongoConfig;
import com.kepler.admin.mongo.impl.MongoUtils;
import com.kepler.admin.resource.terminal.TerminalStatusFinder;
import com.kepler.admin.statistics.chart.StatusDataset;
import com.kepler.admin.statistics.chart.StatusService;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * @author longyaokun
 * 
 */
public class StatusServiceImpl implements StatusService {

	private static final DBObject SORT = BasicDBObjectBuilder.start(Dictionary.FIELD_PERIOD, 1).get();

	private final TerminalStatusFinder finder;
	
	private final MongoConfig status;

	public StatusServiceImpl(MongoConfig status, TerminalStatusFinder finder) {
		this.status = status;
		this.finder = finder;
	}

	@Override
	public StatusDataset status(String sid, int offset) {
		BasicDBObjectBuilder query = BasicDBObjectBuilder.start();
		// 周期 + SID
		query.add(Dictionary.FIELD_HOST_LOCAL_SID, sid);
		query.add(Dictionary.FIELD_PERIOD, BasicDBObjectBuilder.start("$gte", Period.MINUTE.period() - offset).get());
		// 按周期排序
		return new MongoDataset(sid, this.status.collection().find(query.get()).sort(StatusServiceImpl.SORT));
	}

	private class MongoDataset extends StatusDataset {

		@SuppressWarnings("unchecked")
		private MongoDataset(String sid, DBCursor cursor) {
			super(sid);
			try (DBCursor iterator = cursor) {
				while (iterator.hasNext()) {
					DBObject current = iterator.next();
					DBObject status = MongoUtils.asDBObject(current, Dictionary.FIELD_STATUS);
					// 周期时间
					long time = Period.MINUTE.convert(MongoUtils.asLong(current, Dictionary.FIELD_PERIOD));
					this.memory(time, status);
					this.thread(time, status);
					this.traffic(time, status);
					// GC
					this.gc(time, status, (List<String>) StatusServiceImpl.this.finder.sid(sid).getStatus().get("gc_names"));
					super.loadAverage(new Object[] { time, MongoUtils.asDouble(status, "running_loadaverage", 0) });
					super.request(new Object[] { time, MongoUtils.asLong(status, "request", 0) });
				}
			}
		}

		private void gc(long time, DBObject status, List<String> gcs) {
			for (int index = 0; index < gcs.size(); index++) {
				super.gc(new Object[] { time, MongoUtils.asLong(status, "gc_" + gcs.get(index).toLowerCase() + "_time", 0) }, gcs.get(index));
			}
		}

		private void traffic(long time, DBObject status) {
			super.trafficInput(new Object[] { time, MongoUtils.asLong(status, "traffic_input", 0) });
			super.trafficOutput(new Object[] { time, MongoUtils.asLong(status, "traffic_output", 0) });
		}

		private void memory(long time, DBObject status) {
			super.memoryHeap(new Object[] { time, MongoUtils.asLong(status, "memory_heap_used", 0) / (1024 * 1024) });
			super.memoryNonHeap(new Object[] { time, MongoUtils.asLong(status, "memory_nonheap_used", 0) / (1024 * 1024) });
			super.memoryFree(new Object[] { time, (MongoUtils.asLong(status, "memory_heap_max", 0) - MongoUtils.asLong(status, "memory_heap_used", 0)) / (1024 * 1024) });
		}

		private void thread(long time, DBObject status) {
			super.thread4vm(new Object[] { time, MongoUtils.asLong(status, "thread_active", 0) });
			super.thread4stacks(new Object[] { time, MongoUtils.asLong(status, "thread_stacks", 0) });
			super.thread4kepler(new Object[] { time, MongoUtils.asLong(status, "thread_framework_active", 0) });
		}
	}
}
