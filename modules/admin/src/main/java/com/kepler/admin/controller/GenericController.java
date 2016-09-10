package com.kepler.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kepler.admin.generic.GenericRequest;
import com.kepler.admin.generic.GenericTemplate;
import com.kepler.admin.generic.impl.DefaultRequest;
import com.kepler.generic.reflect.GenericService;
import com.kepler.header.HeadersContext;
import com.kepler.header.impl.TraceContext;
import com.kepler.host.Host;
import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
@Controller
@RequestMapping(value = "/generic")
public class GenericController {

	private final GenericTemplate template;

	private final GenericService generic;

	private final HeadersContext headers;

	public GenericController(GenericTemplate template, GenericService generic, HeadersContext headers) {
		super();
		this.generic = generic;
		this.headers = headers;
		this.template = template;
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public DefaultResponse generic(@RequestBody DefaultRequest request) {
		// 调用起始时间
		long start = System.currentTimeMillis();
		try {
			// 指定Tag
			this.headers.get().put(Host.TAG_KEY, request.getTag());
			// 发送请求
			DefaultResponse response = new DefaultResponse(this.generic.invoke(request.metadata(), request.getMethod(), request.getClasses(), request.getDatas()), start);
			// 记录历史(仅服务正常返回时)
			this.template.set(request);
			return response;
		} catch (Throwable e) {
			return new DefaultResponse(e.getMessage(), start);
		}
	}

	/**
	 * 获取指定服务的历史调用信息
	 * 
	 * @param service
	 * @param version
	 * @param catalog
	 * @param method
	 * @return
	 * @throws Throwable
	 */
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public GenericRequest template(String service, String version, String catalog, String method) {
		return this.template.get(new Service(service, version, catalog), method);
	}

	/**
	 * 用于响应
	 * 
	 * @author KimShen
	 *
	 */
	public static class DefaultResponse {

		private final String throwable;

		private final Object response;

		private final long elapse;

		public DefaultResponse(String throwable, long start) {
			super();
			this.elapse = System.currentTimeMillis() - start;
			this.throwable = throwable;
			this.response = null;
		}

		public DefaultResponse(Object response, long start) {
			super();
			this.elapse = System.currentTimeMillis() - start;
			this.response = response;
			this.throwable = null;
		}

		public String getThrowable() {
			return this.throwable;
		}

		public Object getResponse() {
			return this.response;
		}

		public String getTrace() {
			return TraceContext.get();
		}

		public long getElapse() {
			return this.elapse;
		}
	}
}
