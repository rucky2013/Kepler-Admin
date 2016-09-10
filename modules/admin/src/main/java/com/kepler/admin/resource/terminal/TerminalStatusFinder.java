package com.kepler.admin.resource.terminal;

import java.util.Collection;

/**
 * @author kim 2016年1月4日
 */
public interface TerminalStatusFinder {

	/**
	 * 业务分组对应终端状态集合
	 * 
	 * @param group
	 * @return
	 */
	public Collection<TerminalStatus> group(String group);

	/**
	 * 业务分组集合(含客户端)
	 * 
	 * @param group
	 * @return
	 */
	public Collection<String> groups();

	/**
	 * SID对应终端状态
	 * 
	 * @param sid
	 * @return
	 */
	public TerminalStatus sid(String sid);
}
