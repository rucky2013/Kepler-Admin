package com.kepler.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kepler.admin.generic.GenericHistory;
import com.kepler.admin.generic.GenericRequest;
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

	private final GenericService generic;

	private final GenericHistory hisotry;

	private final HeadersContext headers;

	public GenericController(GenericService generic, GenericHistory hisotry, HeadersContext headers) {
		super();
		this.generic = generic;
		this.headers = headers;
		this.hisotry = hisotry;
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public DefaultResponse generic(@RequestBody DefaultRequest defaultRequest) throws Throwable {
		long start = System.currentTimeMillis();
		try {
			// 指定Tag
			this.headers.get().put(Host.TAG_KEY, defaultRequest.getTag());
			// 发送请求
			DefaultResponse defaultResponse = new DefaultResponse(this.generic.invoke(defaultRequest.metadata(), defaultRequest.getMethod(), defaultRequest.getClasses(), defaultRequest.getDatas()), System.currentTimeMillis() - start);
			// 记录历史
			this.hisotry.set(defaultRequest);
			return defaultResponse;
		} catch (Throwable e) {
			return new DefaultResponse(e.getMessage(), System.currentTimeMillis() - start);
		}
	}

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public GenericRequest generic(String service, String version, String catalog, String method) throws Throwable {
		return this.hisotry.get(new Service(service, version, catalog), method);
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

		public DefaultResponse(String throwable, long elapse) {
			super();
			this.throwable = throwable;
			this.elapse = elapse;
			this.response = null;
		}

		public DefaultResponse(Object response, long elapse) {
			super();
			this.response = response;
			this.elapse = elapse;
			this.throwable = null;
		}

		public String getThrowable() {
			return this.throwable;
		}

		public Object getResponse() {
			return this.response;
		}

		public String getTrace() {
			return TraceContext.trace();
		}

		public long getElapse() {
			return this.elapse;
		}
	}
}
