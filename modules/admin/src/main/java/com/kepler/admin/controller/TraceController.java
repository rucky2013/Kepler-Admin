package com.kepler.admin.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kepler.admin.traces.TraceService;

/**
 * @author zhangjiehao 2016年3月17日
 */
@Controller
@RequestMapping(value = "/traces")
public class TraceController {

	private TraceService traceService;

	public TraceController(TraceService traceService) {
		super();
		this.traceService = traceService;
	}

	/**
	 * SID + Service + Version对应服务实例每个方法在指定周期内的统计
	 * @param sid
	 * @param service
	 * @param versionAndCatalog
	 * @param period
	 * @param offset
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public List<Map> methods(String traceId) {
		return traceService.getTrace(traceId);
	}


}