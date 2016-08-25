package com.kepler.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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

	private final HeadersContext headers;

	public GenericController(GenericService generic, HeadersContext headers) {
		super();
		this.generic = generic;
		this.headers = headers;
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Response generic(@RequestBody Request request) throws Throwable {
		StopWatch watch = this.watch();
		try {
			this.headers.get().put(Host.TAG_KEY, request.getTag());
			return new Response(this.generic.invoke(request.getMetadata(), request.getMethod(), request.getClasses(), request.getDatas()), watch.getTotalTimeMillis());
		} catch (Throwable e) {
			return new Response(e.getMessage(), watch.getTotalTimeMillis());
		}
	}

	/**
	 * 启动秒表
	 * 
	 * @return
	 */
	private StopWatch watch() {
		StopWatch watch = new StopWatch();
		watch.start();
		return watch;
	}

	/**
	 * 接收Request
	 * 
	 * @author KimShen
	 *
	 */
	public static class Request {

		private String tag;

		private String method;

		private String service;

		private String version;

		private String catalog;

		private Object[] datas;

		private String[] classes;

		public String getTag() {
			return this.tag;
		}

		public void setTag(String tag) {
			this.tag = tag;
		}

		public String getMethod() {
			Assert.notNull(this.method, "Method can not be null");
			return this.method;
		}

		public void setMethod(String method) {
			this.method = method;
		}

		public String getService() {
			Assert.notNull(this.service, "Service can not be null");
			return this.service;
		}

		public void setService(String service) {
			this.service = service;
		}

		public String getVersion() {
			Assert.notNull(this.version, "Version can not be null");
			return this.version;
		}

		public void setVersion(String version) {
			this.version = version;
		}

		public String getCatalog() {
			Assert.notNull(this.catalog, "Catalog can not be null");
			return this.catalog;
		}

		public void setCatalog(String catalog) {
			this.catalog = catalog;
		}

		public Object[] getDatas() {
			return this.datas;
		}

		public void setDatas(Object[] datas) {
			this.datas = datas;
		}

		public String[] getClasses() {
			return this.classes;
		}

		public void setClasses(String[] classes) {
			this.classes = classes;
		}

		public Service getMetadata() {
			return new Service(this.getService(), this.getVersion(), this.getCatalog());
		}
	}

	/**
	 * 用于响应
	 * 
	 * @author KimShen
	 *
	 */
	public static class Response {

		private final String throwable;

		private final Object response;

		private final long elapse;

		public Response(String throwable, long elapse) {
			super();
			this.throwable = throwable;
			this.elapse = elapse;
			this.response = null;
		}

		public Response(Object response, long elapse) {
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
