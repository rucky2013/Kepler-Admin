package com.kepler.admin.statistics.barchart;

/**
 * @author longyaokun
 * @date 2016年3月21日
 *
 */
public class Series {

	private final String name;
	private final Object[] data;

	public Series(String name, Object[] data) {
		this.name = name;
		this.data = data;
	}

	public Object[] getData() {
		return this.data;
	}

	public String getName() {
		return this.name;
	}
}
