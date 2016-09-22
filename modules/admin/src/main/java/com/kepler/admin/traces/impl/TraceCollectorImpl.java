package com.kepler.admin.traces.impl;

import java.util.ArrayList;
import java.util.List;

import com.kepler.admin.domain.Period;
import com.kepler.admin.mongo.Dictionary;
import com.kepler.admin.mongo.MongoConfig;
import com.kepler.admin.mongo.impl.MongoUtils;
import com.kepler.admin.traces.Trace;
import com.kepler.admin.traces.TraceCollector;
import com.kepler.config.PropertiesUtils;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * @author KimShen
 *
 */
public class TraceCollectorImpl implements TraceCollector {

	/**
	 * 周期范围最大值, 防止过大数据造成的OOM
	 */
	private static final int MAX = PropertiesUtils.get(TraceCollectorImpl.class.getName().toLowerCase() + ".max", 60 * 24 * 15);

	private final MongoConfig mongo;

	public TraceCollectorImpl(MongoConfig mongo) {
		this.mongo = mongo;
	}

	@Override
	public List<Trace> causes(String service, String versionAndCatalog, String method, Period period, int offset, int length) {
		long offset_start = Period.SECOND.period(period, period.period() - offset);
		long offset_end = Period.SECOND.period(period, period.period() - offset + Math.min(length, TraceCollectorImpl.MAX));
		BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
		builder.add(Dictionary.FIELD_PERIOD, BasicDBObjectBuilder.start().add("$gte", offset_start).add("$lte", offset_end).get());
		builder.add(Dictionary.FIELD_SERVICE, service).add(Dictionary.FIELD_VERSION, versionAndCatalog).add(Dictionary.FIELD_METHOD, method);
		return new TraceCauses(this.mongo.collection().find(builder.get()));
	}

	private class TraceCauses extends ArrayList<Trace> {

		private static final long serialVersionUID = 1L;

		private TraceCauses(DBCursor cursor) {
			try (DBCursor iterator = cursor) {
				while (iterator.hasNext()) {
					super.add(new MongoTrace(iterator.next()));
				}
			}
		}
	}

	private class MongoTrace implements Trace {

		private final DBObject ob;

		private MongoTrace(DBObject ob) {
			super();
			this.ob = ob;
		}

		@Override
		public String getTrace() {
			return MongoUtils.asString(this.ob, Dictionary.FIELD_TRACE);
		}

		@Override
		public String getCause() {
			return MongoUtils.asString(this.ob, Dictionary.FIELD_CAUSE);
		}

		@Override
		public String getService() {
			return MongoUtils.asString(this.ob, Dictionary.FIELD_SERVICE);
		}

		@Override
		public String getMethod() {
			return MongoUtils.asString(this.ob, Dictionary.FIELD_METHOD);
		}

		@Override
		public String getVersionAndCatalog() {
			return MongoUtils.asString(this.ob, Dictionary.FIELD_VERSION);
		}
	}
}
