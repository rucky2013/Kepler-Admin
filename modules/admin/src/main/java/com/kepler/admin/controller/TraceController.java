package com.kepler.admin.controller;

import java.util.List;

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

	private final TraceService trace;

	public TraceController(TraceService trace) {
		super();
		this.trace = trace;
	}

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public List<?> methods(String traceId) {
		return trace.trace(traceId);
	}
}