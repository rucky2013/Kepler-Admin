package com.kepler.admin.traces;

import java.util.List;
import java.util.Map;

public interface TraceService {

	@SuppressWarnings("rawtypes")
	List<Map> getTrace(String traceId);
	
}
