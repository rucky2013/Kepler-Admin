package com.kepler.admin.statistics.chart;

/**
 * @author longyaokun
 *
 */
public interface StatusService {

	/**
	 * SID指定的动态状态聚合
	 * 
	 * @param sid
	 * @param offset 偏移量
	 * @return
	 */
	public StatusDataset status(String sid, int offset);
}
