package com.kepler.admin.collector.trace;

import java.util.List;

import com.kepler.annotation.Broadcast;
import com.kepler.annotation.Internal;
import com.kepler.annotation.Service;
import com.kepler.host.Host;
import com.kepler.trace.TraceCause;

/**
 * @author KimShen
 *
 */
@Service(version = "0.0.1")
@Internal
public interface TraceBroadcast {

	@Broadcast
	public void broadcast(Host host, List<TraceCause> cause);
}
