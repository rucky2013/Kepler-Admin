package com.kepler.admin.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kepler.admin.statistics.dashboard.Dashboard;
import com.kepler.admin.statistics.dashboard.DashboardService;

/**
 * @author longyaokun 2015年12月17日
 */
@Controller
@RequestMapping(value = "/dashboard")
public class DashboardController {

	private final DashboardService dashboard;

	public DashboardController(DashboardService dashboard) {
		super();
		this.dashboard = dashboard;
	}

	/**
	 * 错误
	 *
	 * @return
	 */
	@RequestMapping(value = "/failed", method = RequestMethod.GET)
	@ResponseBody
	public List<Dashboard<? extends Object>> failed(int adjust) {
		return this.dashboard.failed(adjust);
	}

	/**
	 * 总量
	 * 
	 * @return
	 */
	@RequestMapping(value = "/total", method = RequestMethod.GET)
	@ResponseBody
	public List<Dashboard<? extends Object>> total(int adjust) {
		return this.dashboard.total(adjust);
	}

	/**
	 * 耗时
	 * 
	 * @return
	 */
	@RequestMapping(value = "/rtt", method = RequestMethod.GET)
	@ResponseBody
	public List<Dashboard<Double>> rtt(int adjust) {
		return this.dashboard.rtt(adjust);
	}
}
