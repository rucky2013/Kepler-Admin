package com.kepler.admin.statistics.method;

import java.util.Comparator;

import com.kepler.admin.mongo.Dictionary;

/**
 * @author longyaokun
 * @date 2016年7月12日
 */
public enum SortBy {

	RTT, TOTAL, EXCEPTION, TIMEOUT;

	public String sortBy() {
		switch (this) {
		case RTT:
			return Dictionary.FIELD_RTT;
		case TOTAL:
			return Dictionary.FIELD_TOTAL;
		case EXCEPTION:
			return Dictionary.FIELD_EXCEPTION;
		case TIMEOUT:
			return Dictionary.FIELD_TIMEOUT;
		default:
			return "";
		}
	}

	public Comparator<MethodInvoker> comparator() {
		switch (this) {
		case RTT:
			return new Comparator<MethodInvoker>() {

				@Override
				public int compare(MethodInvoker o1, MethodInvoker o2) {
					return o2.getRtt() - o1.getRtt() > 0 ? 1 : -1;
				}
			};
		case TOTAL:
			return new Comparator<MethodInvoker>() {

				@Override
				public int compare(MethodInvoker o1, MethodInvoker o2) {
					return o2.getTotal() - o1.getTotal() > 0 ? 1 : -1;
				}
			};
		case EXCEPTION:
			return new Comparator<MethodInvoker>() {

				@Override
				public int compare(MethodInvoker o1, MethodInvoker o2) {
					return o2.getException() - o1.getException() > 0 ? 1 : -1;
				}
			};
		case TIMEOUT:
			return new Comparator<MethodInvoker>() {

				@Override
				public int compare(MethodInvoker o1, MethodInvoker o2) {
					return o2.getTimeout() - o1.getTimeout() > 0 ? 1 : -1;
				}
			};
		default:
			return new Comparator<MethodInvoker>() {

				@Override
				public int compare(MethodInvoker o1, MethodInvoker o2) {
					return o2.getRtt() - o1.getRtt() > 0 ? 1 : -1;
				}
			};
		}
	}
}
