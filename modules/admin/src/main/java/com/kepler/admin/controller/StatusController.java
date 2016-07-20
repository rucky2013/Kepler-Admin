package com.kepler.admin.controller;

import java.util.Collection;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kepler.admin.resource.status.TerminalStatus;
import com.kepler.admin.resource.status.TerminalStatusFinder;

/**
 * @author kim 2016年1月2日
 */
@Controller
@RequestMapping(value = "/status")
public class StatusController {

	private final TerminalStatusFinder finder;

	public StatusController(TerminalStatusFinder finder) {
		super();
		this.finder = finder;
	}
	
	/**
	 * Group对应主机状态集合(含客户端)
	 * 
	 * @param group
	 * @return
	 */
	@RequestMapping(value = "/group", method = RequestMethod.GET)
	@ResponseBody
	public Collection<TerminalStatus> group(String group) {
		return this.finder.group(group);
	}

	/**
	 * Group集合(含服务及客户端)
	 * 
	 * @param group
	 * @return
	 */
	@RequestMapping(value = "/groups", method = RequestMethod.GET)
	@ResponseBody
	public Collection<String> groups() {
		return this.finder.groups();
	}

	/**
	 * 主机状态
	 * 
	 * @param sid
	 * @return
	 */
	@RequestMapping(value = "/sid", method = RequestMethod.GET)
	@ResponseBody
	public TerminalStatus sid(String sid) {
		return this.finder.sid(sid);
	}
}
