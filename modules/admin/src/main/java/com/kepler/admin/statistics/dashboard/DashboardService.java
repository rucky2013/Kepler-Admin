package com.kepler.admin.statistics.dashboard;

import java.util.List;

/**
 * @author longyaokun 2015年12月17日
 */
public interface DashboardService {

	public List<Dashboard<? extends Object>> failed(int adjust);

	public List<Dashboard<? extends Object>> total(int adjust);

	public List<Dashboard<Double>> rtt(int adjust);
}
