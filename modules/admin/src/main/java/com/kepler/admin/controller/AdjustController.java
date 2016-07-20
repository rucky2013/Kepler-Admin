package com.kepler.admin.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kepler.admin.resource.config.Config;
import com.kepler.admin.resource.config.ConfigFinder;
import com.kepler.admin.resource.instance.Instance;
import com.kepler.admin.resource.instance.InstanceFinder;
import com.kepler.admin.service.AdjustService;

/**
 * @author longyaokun 2015年12月17日
 */
@Controller
@RequestMapping(value = "/adjust")
public class AdjustController {

	private final AdjustService adjust;

	/**
	 * 搜索动态配置
	 */
	private final ConfigFinder finder4config;

	/**
	 * 搜索服务(实例)
	 */
	private final InstanceFinder finder4instance;

	public AdjustController(AdjustService adjust, ConfigFinder finder4config, InstanceFinder finder4instance) {
		super();
		this.adjust = adjust;
		this.finder4config = finder4config;
		this.finder4instance = finder4instance;
	}

	/**
	 * 获取指定SID + Service / Version服务实例详细信息
	 * @param sid
	 * @param service
	 * @param versionAndCatalog
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public Instance detail(String sid, String service, String versionAndCatalog) {
		return this.finder4instance.sid(sid, service, versionAndCatalog);
	}

	/**
	 * 单独修改优先级(SID + Service + Version)
	 * 
	 * @param sid
	 * @param service
	 * @param versionAndCatalog
	 * @param priority
	 * @throws Exception
	 */
	@RequestMapping(value = "/priority", method = RequestMethod.POST)
	@ResponseBody
	public void priority(String sid, String service, String versionAndCatalog, int priority) throws Exception {
		this.adjust.priority(sid, service, versionAndCatalog, priority);
	}

	/**
	 * 单独修改Tag(SID + Service + Version)
	 * 
	 * @param sid
	 * @param service
	 * @param versionAndCatalog
	 * @param tag
	 * @throws Exception
	 */
	@RequestMapping(value = "/tag", method = RequestMethod.POST)
	@ResponseBody
	public void tag(String sid, String service, String versionAndCatalog, String tag) throws Exception {
		this.adjust.tag(sid, service, versionAndCatalog, tag);
	}

	/**
	 * 批量修改优先级
	 * 
	 * @param service
	 * @param versionAndCatalog
	 * @param priority
	 * @throws Exception
	 */
	@RequestMapping(value = "/priorities", method = RequestMethod.POST)
	@ResponseBody
	public void priority(String service, String versionAndCatalog, int priority) throws Exception {
		this.adjust.priority(service, versionAndCatalog, priority);
	}

	/**
	 * 批量修改标签
	 * 
	 * @param service
	 * @param versionAndCatalog
	 * @param tag
	 * @throws Exception
	 */
	@RequestMapping(value = "/tags", method = RequestMethod.POST)
	@ResponseBody
	public void tag(String service, String versionAndCatalog, String tag) throws Exception {
		this.adjust.tag(service, versionAndCatalog, tag);
	}

	/**
	 * 获取动态参数
	 * 
	 * @param sid
	 * @return
	 */
	@RequestMapping(value = "/config", method = RequestMethod.GET)
	@ResponseBody
	public Config get(String sid) {
		return this.finder4config.sid(sid);
	}

	/**
	 * 指定动态参数
	 * 
	 * @param configs
	 * @throws Exception
	 */
	@RequestMapping(value = "/config", method = RequestMethod.POST)
	@ResponseBody
	public void set(@RequestParam Map<String, String> configs) throws Exception {
		this.adjust.config(configs.remove("sid"), configs);
	}
}