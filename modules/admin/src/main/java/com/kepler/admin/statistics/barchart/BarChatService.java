package com.kepler.admin.statistics.barchart;

import java.util.List;

import com.kepler.admin.domain.Period;

/**
 * @author longyaokun
 * @date 2016年3月21日
 *
 */
public interface BarChatService {

	public List<BarChartData> statistics4Clients(String service, String versionAndCatalog, String method, Period periodType, long period);
}
