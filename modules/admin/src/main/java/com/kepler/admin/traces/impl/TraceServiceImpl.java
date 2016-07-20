package com.kepler.admin.traces.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;
import org.xerial.snappy.Snappy;

import com.kepler.admin.mongo.MongoConfig;
import com.kepler.admin.traces.TraceService;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class TraceServiceImpl implements TraceService {

	private final MongoConfig mongo;
	
	private static final Log LOGGER = LogFactory.getLog(TraceService.class);
	
	private static final String REQUEST = "request";
	
	private static final String RESPONSE = "response";
	
	private static final String SPAN = "span";
	
	private static final String PARENT_SPAN = "parentSpan";
	
	private static final String CHILDREN = "children";
	
	private static final String TRACE = "trace";

	private static final String USE_SNAPPY = "useSnappy";
	
	private static final String START_TIME = "startTime";

	private class SpanOrder implements Comparator<DBObject> {

		@Override
		public int compare(DBObject o1, DBObject o2) {
			long startTime1 = o1.get(START_TIME) instanceof Date ? ((Date)o1.get(START_TIME)).getTime() : (long)o1.get(START_TIME);
			long startTime2 = o2.get(START_TIME) instanceof Date ? ((Date)o2.get(START_TIME)).getTime() : (long)o2.get(START_TIME);
			return (int) (startTime1 - startTime2);
		}

	}
	
	private final SpanOrder spanOrder = new SpanOrder();

	public TraceServiceImpl(MongoConfig mongo) {
		this.mongo = mongo;
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public List<Map> getTrace(String traceId) {
		List<DBObject> spans = mongo.collection().find(new BasicDBObject(TRACE, traceId)).toArray();
		List<DBObject> rootSpans = new ArrayList<>();
		List<Map> result = new ArrayList<>();
		Map<String, List<DBObject>> spanMap = new HashMap<>();
		for (DBObject span : spans) {
			decodeSnappy(span);
			if (span.get(PARENT_SPAN) == null) {
				rootSpans.add(span);
				continue;
			}
			List<DBObject> children = spanMap.get((String) span.get(PARENT_SPAN));
			if (children == null) {
				children = new ArrayList<>();
			}
			children.add(span);
			spanMap.put((String) span.get(PARENT_SPAN), children);
		}
		if (CollectionUtils.isEmpty(rootSpans)) {
			return null;
		}
		for (DBObject rootSpan : rootSpans) {
			buildSpanTree(rootSpan, spanMap);
			result.add(rootSpan.toMap());
		}
		
		return result;
	}

	private void decodeSnappy(DBObject span) {
		if (span.get(USE_SNAPPY) == null || ((Boolean)span.get(USE_SNAPPY) == false)) {
			return;
		}
		try {
			if (span.get(REQUEST) != null) {
				span.put(REQUEST, Snappy.uncompressString((byte[])span.get(REQUEST)));
			}
			if (span.get(RESPONSE) != null) {
				span.put(RESPONSE, Snappy.uncompressString((byte[])span.get(RESPONSE)));
			}
			
		} catch (IOException e) {
			LOGGER.error("Snappy解压失败", e);
		}
	}

	private void buildSpanTree(DBObject trace, Map<String, List<DBObject>> spanMap) {
		List<DBObject> children = new ArrayList<>();
		trace.put(CHILDREN, children);
		String spanId = (String) trace.get(SPAN);
		List<DBObject> spans = spanMap.get(spanId);
		if (spans == null) {
			return;
		}
		for (DBObject span : spans) {
			buildSpanTree(span, spanMap);
			children.add(span);
		}
		Collections.sort(children, spanOrder);
	}
	
}
