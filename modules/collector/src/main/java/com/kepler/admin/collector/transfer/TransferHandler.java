package com.kepler.admin.collector.transfer;

import java.util.Collection;
import java.util.List;

import com.kepler.admin.transfer.Feeder;
import com.kepler.admin.transfer.Transfers;
import com.kepler.annotation.Autowired;

/**
 * @author kim 2015年7月22日
 */
@Autowired
public class TransferHandler implements Feeder {

	private final List<Feeder> feeders;

	public TransferHandler(List<Feeder> feeders) {
		super();
		this.feeders = feeders;
	}

	@Override
	public void feed(long timestamp, Collection<Transfers> transfers) {
		for (Feeder each : this.feeders) {
			each.feed(timestamp, transfers);
		}
	}
}
