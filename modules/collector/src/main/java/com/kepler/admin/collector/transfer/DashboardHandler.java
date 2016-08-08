package com.kepler.admin.collector.transfer;

import java.util.Collection;

import com.kepler.admin.domain.Period;
import com.kepler.admin.mongo.Dictionary;
import com.kepler.admin.mongo.MongoConfig;
import com.kepler.admin.transfer.Feeder;
import com.kepler.admin.transfer.Transfer;
import com.kepler.admin.transfer.Transfers;
import com.kepler.config.PropertiesUtils;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DBObject;

/**
 * 大盘
 * 
 * @author kim 2015年7月22日
 */
public class DashboardHandler implements Feeder {

	// Period, Service ,Version
	private static final DBObject INDEX = BasicDBObjectBuilder.start().add(Dictionary.FIELD_PERIOD, 1).add(Dictionary.FIELD_SERVICE, 1).add(Dictionary.FIELD_VERSION, 1).get();

	/**
	 * 分段, 影响数据切片数量及精细度.
	 */
	private static final int INTERVAL = PropertiesUtils.get(DashboardHandler.class.getName().toLowerCase() + ".interval", 2);

	private final MongoConfig dashboard;

	public DashboardHandler(MongoConfig dashboard) {
		super();
		this.dashboard = dashboard;
	}

	public void init() {
		this.dashboard.collection().index(DashboardHandler.INDEX);
	}

	/**
	 * 周期所处分钟跨度
	 * 
	 * @param period
	 * @return
	 */
	private long[] minutes(long period) {
		long[] minutes = new long[DashboardHandler.INTERVAL];
		for (int index = DashboardHandler.INTERVAL; index > 0; index--) {
			minutes[index - 1] = period;
			period--;
		}
		return minutes;
	}

	private DBObject query(long period, Transfers transfers) {
		// Query (周期 + Service + Version)
		BasicDBObjectBuilder query = BasicDBObjectBuilder.start();
		query.add(Dictionary.FIELD_PERIOD, period);
		query.add(Dictionary.FIELD_SERVICE, transfers.service());
		query.add(Dictionary.FIELD_VERSION, transfers.version());
		return query.get();
	}

	private DBObject update(long[] minutes, Statistics report) {
		// Update (RTT(除以Total), 访问量, 异常数量)
		BasicDBObjectBuilder update = BasicDBObjectBuilder.start();
		update.add(Dictionary.FIELD_FAILED, report.failed());
		update.add(Dictionary.FIELD_TOTAL, report.total());
		update.add(Dictionary.FIELD_RTT, report.rtt());
		// 递增统计, 如果为新条目则创建分钟间隔数据
		return BasicDBObjectBuilder.start().add("$inc", update.get()).add("$setOnInsert", BasicDBObjectBuilder.start(Dictionary.FIELD_PERIOD_INTERVAL, DashboardHandler.INTERVAL).add(Dictionary.FIELD_PERIOD_MINUTE, minutes).get()).get();
	}

	@Override
	public void feed(long timestamp, Collection<Transfers> transfers) {
		// 周期(区间)
		long period = (Period.MINUTE.period(timestamp) / DashboardHandler.INTERVAL + 1) * DashboardHandler.INTERVAL;
		// 检查集合, Bluk必须存在操作才允许提交
		if (!transfers.isEmpty()) {
			BulkWriteOperation batch = this.dashboard.collection().bulkWrite();
			for (Transfers each : transfers) {
				batch.find(this.query(period, each)).upsert().updateOne(this.update(this.minutes(period), new Statistics(each)));
			}
			// 使用默认WriteConcern
			batch.execute();
		}
	}

	/**
	 * 统计数值
	 * 
	 * @author KimShen
	 *
	 */
	private class Statistics {

		private double rtt;

		private long total;

		private long failed;

		private Statistics(Transfers transfers) {
			for (Transfer transfer : transfers.transfers()) {
				this.rtt(transfer.rtt());
				this.total(transfer.total());
				// Timeout + Exception均统计为Failed
				this.failed(transfer.timeout()).failed(transfer.exception());
			}
		}

		private Statistics rtt(double rtt) {
			this.rtt += rtt;
			return this;
		}

		private Statistics total(long total) {
			this.total += total;
			return this;
		}

		private Statistics failed(long failed) {
			this.failed += failed;
			return this;
		}

		/**
		 * RTT平均值
		 * 
		 * @return
		 */
		public double rtt() {
			// 分母判断
			return this.total() == 0 ? 0 : this.rtt;
		}

		public long total() {
			return this.total;
		}

		public long failed() {
			return this.failed;
		}
	}
}
