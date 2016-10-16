package com.kepler.admin.collector.transfer;

import java.util.Collection;

import com.kepler.admin.domain.Period;
import com.kepler.admin.mongo.Dictionary;
import com.kepler.admin.mongo.MongoConfig;
import com.kepler.admin.transfer.Feeder;
import com.kepler.admin.transfer.Transfer;
import com.kepler.admin.transfer.Transfers;
import com.kepler.annotation.Autowired;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DBObject;

/**
 * 分时维度
 * 
 * @author kim 2015年7月22日
 */
@Autowired
public class TransferHandler implements Feeder {

	// Period, Service ,Version, Host_local_sid, Host_target_sid, Host_local_sid, Method
	private static final DBObject INDEX = BasicDBObjectBuilder.start().add(Dictionary.FIELD_PERIOD, 1).add(Dictionary.FIELD_SERVICE, 1).add(Dictionary.FIELD_VERSION, 1).add(Dictionary.FIELD_HOST_TARGET_SID, 1).add(Dictionary.FIELD_HOST_LOCAL_SID, 1).add(Dictionary.FIELD_METHOD, 1).get();

	private final MongoConfig transfers4minute;

	private final MongoConfig transfers4hour;

	private final MongoConfig transfers4day;

	public TransferHandler(MongoConfig transfers4minute, MongoConfig transfers4hour, MongoConfig transfers4day) {
		super();
		this.transfers4minute = transfers4minute;
		this.transfers4hour = transfers4hour;
		this.transfers4day = transfers4day;
	}

	public void init() {
		this.transfers4minute.collection().index(TransferHandler.INDEX);
		this.transfers4hour.collection().index(TransferHandler.INDEX);
		this.transfers4day.collection().index(TransferHandler.INDEX);
	}

	/**
	 * Update (RTT, 调用量, 超时, 异常)
	 * 
	 * @param transfer
	 * @return
	 */
	private DBObject update(Transfer transfer) {
		BasicDBObjectBuilder report = BasicDBObjectBuilder.start();
		report.add(Dictionary.FIELD_RTT, transfer.rtt());
		report.add(Dictionary.FIELD_TOTAL, transfer.total());
		report.add(Dictionary.FIELD_TIMEOUT, transfer.timeout());
		report.add(Dictionary.FIELD_EXCEPTION, transfer.exception());
		return BasicDBObjectBuilder.start().add("$inc", report.get()).get();
	}

	/**
	 * 更新查询周期后提交
	 * 
	 * @param period
	 * @param query
	 * @param update
	 * @param batch 批量提交
	 */
	private void update4period(long period, DBObject query, DBObject update, BulkWriteOperation batch) {
		// 更新查询周期
		batch.find(BasicDBObjectBuilder.start(query.toMap()).add(Dictionary.FIELD_PERIOD, period).get()).upsert().updateOne(update);
	}

	/**
	 * Service / Version / Method
	 * 
	 * @param transfers
	 * @return
	 */
	private DBObject query(Transfers transfers) {
		// Query (Service, Version, Method)
		BasicDBObjectBuilder query = BasicDBObjectBuilder.start();
		query.add(Dictionary.FIELD_SERVICE, transfers.service());
		query.add(Dictionary.FIELD_VERSION, transfers.version());
		query.add(Dictionary.FIELD_METHOD, transfers.method());
		return query.get();
	}

	@Override
	public void feed(Collection<Transfers> transfers) {
		// 开启Batch
		BulkWriteOperation batch4minute = this.transfers4minute.collection().bulkWrite();
		BulkWriteOperation batch4hour = this.transfers4hour.collection().bulkWrite();
		BulkWriteOperation batch4day = this.transfers4day.collection().bulkWrite();
		// 提交并产生任务后执行批量
		if (this.submit(transfers, batch4minute, batch4hour, batch4day)) {
			batch4minute.execute();
			batch4hour.execute();
			batch4day.execute();
		}
	}

	/**
	 * 提交批量任务
	 * 
	 * @param timestamp
	 * @param transfers
	 * @param batch4minute
	 * @param batch4hour
	 * @param batch4day
	 * @return
	 */
	private boolean submit(Collection<Transfers> transfers, BulkWriteOperation batch4minute, BulkWriteOperation batch4hour, BulkWriteOperation batch4day) {
		// 是否产生任务(默认False)
		boolean submited = false;
		for (Transfers each : transfers) {
			DBObject query = this.query(each);
			for (Transfer transfer : each.transfers()) {
				// 更新发送主机和接收主机
				query.put(Dictionary.FIELD_HOST_TARGET_SID, transfer.target().sid());
				query.put(Dictionary.FIELD_HOST_LOCAL_SID, transfer.local().sid());
				DBObject update = this.update(transfer);
				// 更新周期为分钟
				this.update4period(Period.MINUTE.period(transfer.timestamp()), query, update, batch4minute);
				// 更新周期为小时
				this.update4period(Period.HOUR.period(transfer.timestamp()), query, update, batch4hour);
				// 更新周期为每天
				this.update4period(Period.DAY.period(transfer.timestamp()), query, update, batch4day);
				submited = true;
			}
		}
		return submited;
	}
}
