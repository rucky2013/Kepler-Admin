package com.kepler.admin.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kepler.admin.domain.Period;
import com.kepler.admin.statistics.barchart.BarChartData;
import com.kepler.admin.statistics.barchart.BarChatService;

/**
 * @author longyaokun
 * @date 2016年3月21日
 *
 */
@Controller
@RequestMapping(value = "/barchart")
public class BarChartController {

	private final BarChatService barChatService;

	public BarChartController(BarChatService barChatService) {
		this.barChatService = barChatService;
	}
	
	@RequestMapping(value = "/statistics4Clients", method = RequestMethod.GET)
	@ResponseBody
	public List<BarChartData> statistics4Clients(String service, String versionAndCatalog, String method, Period period){
		return this.barChatService.statistics4Clients(service, versionAndCatalog, method, period, period.equals(Period.MINUTE) ? period.period() - 1 : period.period());
	}
}
