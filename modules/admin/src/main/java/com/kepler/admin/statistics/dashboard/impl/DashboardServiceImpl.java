package com.kepler.admin.statistics.dashboard.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.kepler.admin.domain.Period;
import com.kepler.admin.domain.ServiceAndVersion;
import com.kepler.admin.mongo.Dictionary;
import com.kepler.admin.mongo.MongoConfig;
import com.kepler.admin.mongo.impl.MongoUtils;
import com.kepler.admin.statistics.dashboard.Dashboard;
import com.kepler.admin.statistics.dashboard.DashboardService;
import com.kepler.config.PropertiesUtils;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * @author longyaokun 2015年12月17日
 */
public class DashboardServiceImpl implements DashboardService {

	/**
	 * 最大采样服务
	 */
	private static final int LIMIT = PropertiesUtils.get(DashboardServiceImpl.class.getName().toLowerCase() + ".limit", 50);

	private static final DBObject INDEX = BasicDBObjectBuilder.start(Dictionary.FIELD_PERIOD_MINUTE, 1).get();

	private final MongoConfig dashboard;

	public DashboardServiceImpl(MongoConfig dashboard) {
		super();
		this.dashboard = dashboard;
		this.dashboard.collection().index(DashboardServiceImpl.INDEX);
	}

	/**
	 * 
	 * 查询当前周期所有服务(集合)
	 * @param sort
	 * @param period
	 * @return
	 */
	private DBCursor query(String sort, int adjust) {
		BasicDBObjectBuilder query = BasicDBObjectBuilder.start();
		query.add(Dictionary.FIELD_PERIOD_MINUTE, Period.MINUTE.period() - adjust);
		return this.dashboard.collection().find(query.get()).limit(DashboardServiceImpl.LIMIT).sort(BasicDBObjectBuilder.start().add(sort, -1).get());
	}

	/**
	 * 查询指定周期指定服务
	 * 
	 * @param service
	 * @param versionAndCatalog
	 * @param period
	 * @return
	 */
	private DBObject query(String service, String versionAndCatalog, long period) {
		BasicDBObjectBuilder query = BasicDBObjectBuilder.start();
		query.add(Dictionary.FIELD_PERIOD, period);
		query.add(Dictionary.FIELD_SERVICE, service);
		query.add(Dictionary.FIELD_VERSION, versionAndCatalog);
		return DashboardServiceImpl.this.dashboard.collection().findOne(query.get());
	}

	@Override
	public List<Dashboard<? extends Object>> failed(int adjust) {
		DBCursor cursor = this.query(Dictionary.FIELD_FAILED, adjust);
		return new LongDashboards(cursor, Dictionary.FIELD_FAILED);
	}

	@Override
	public List<Dashboard<? extends Object>> total(int adjust) {
		DBCursor cursor = this.query(Dictionary.FIELD_TOTAL, adjust);
		return new LongDashboards(cursor, Dictionary.FIELD_TOTAL);
	}

	@Override
	public List<Dashboard<Double>> rtt(int adjust) {
		DBCursor cursor = this.query(Dictionary.FIELD_RTT, adjust);
		DoubleDashboards dashboards = new DoubleDashboards(cursor, Dictionary.FIELD_RTT);
		Collections.sort(dashboards, new Comparator<Dashboard<Double>>() {

			@Override
			public int compare(Dashboard<Double> o1, Dashboard<Double> o2) {
				return o1.getPeriod() - o2.getPeriod() > 0 ? -1 : 1;
			}
		});
		return dashboards;
	}

	abstract private class AbstractDashboard<T> implements Dashboard<T> {

		protected final ServiceAndVersion service;

		protected final String field;

		protected final long period;

		protected final T current;

		protected AbstractDashboard(DBObject db, String field) {
			super();
			this.field = field;
			// 当前周期值
			this.current = this.current(db, field);
			// 上一个周期(当前周期 - 周期间隔)
			this.period = MongoUtils.asLong(db, Dictionary.FIELD_PERIOD) - MongoUtils.asLong(db, Dictionary.FIELD_PERIOD_INTERVAL);
			this.service = new ServiceAndVersion(MongoUtils.asString(db, Dictionary.FIELD_SERVICE), MongoUtils.asString(db, Dictionary.FIELD_VERSION));
		}

		@Override
		public ServiceAndVersion getService() {
			return this.service;
		}

		public double getCompare() {
			return Double.valueOf(new DecimalFormat("#.00").format(this.compare()));
		}

		@Override
		public T getPeriod() {
			return this.current;
		}

		/**
		 * 计算环比
		 * 
		 * @return
		 */
		abstract protected double compare();

		/**
		 * 计算周期值
		 * 
		 * @param db
		 * @param field
		 * @return
		 */
		abstract protected T current(DBObject db, String field);
	}

	private class LongDashboards extends ArrayList<Dashboard<? extends Object>> {

		private static final long serialVersionUID = 1L;

		private LongDashboards(DBCursor cursor, String field) {
			try (DBCursor its = cursor) {
				while (its.hasNext()) {
					super.add(new LongDashboard(its.next(), field));
				}
			}
		}
	}

	private class LongDashboard extends AbstractDashboard<Long> {

		private LongDashboard(DBObject db, String field) {
			super(db, field);
		}

		protected double compare() {
			// 查询上一个周期值
			long prev = MongoUtils.asLong(DashboardServiceImpl.this.query(this.service.getService(), this.service.getVersionAndCatalog(), this.period), this.field, 0);
			return prev != 0 ? new Double(this.current) / prev - 1 : 0;
		}

		@Override
		protected Long current(DBObject db, String field) {
			return MongoUtils.asLong(db, this.field);
		}
	}

	private class DoubleDashboards extends ArrayList<Dashboard<Double>> {

		private static final long serialVersionUID = 1L;

		private DoubleDashboards(DBCursor cursor, String field) {
			try (DBCursor its = cursor) {
				while (its.hasNext()) {
					super.add(new DoubleDashboard(its.next(), field));
				}
			}
		}
	}

	private class DoubleDashboard extends AbstractDashboard<Double> {

		private DoubleDashboard(DBObject db, String field) {
			super(db, field);
		}

		protected double compare() {
			// 查询上一个周期值
			DBObject db = DashboardServiceImpl.this.query(this.service.getService(), this.service.getVersionAndCatalog(), this.period);
			double prev = MongoUtils.asLong(db, Dictionary.FIELD_TOTAL) > 0 ? MongoUtils.asDouble(db, this.field, 0d) / MongoUtils.asLong(db, Dictionary.FIELD_TOTAL) : 0;
			return prev != 0 ? this.current / prev - 1 : 0;
		}

		@Override
		protected Double current(DBObject db, String field) {
			return MongoUtils.asLong(db, Dictionary.FIELD_TOTAL) > 0 ? Double.valueOf(new DecimalFormat("#.00").format(MongoUtils.asDouble(db, this.field, 0d) / MongoUtils.asLong(db, Dictionary.FIELD_TOTAL))) : 0;
		}
	}
}