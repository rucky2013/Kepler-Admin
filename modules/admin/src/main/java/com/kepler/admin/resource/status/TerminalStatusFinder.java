package com.kepler.admin.resource.status;

import java.util.Collection;

/**
 * @author kim 2016年1月4日
 */
public interface TerminalStatusFinder {

	/**
	 * Group对应Status集合
	 * 
	 * @param group
	 * @return
	 */
	public Collection<TerminalStatus> group(String group);

	/**
	 * Group集合(含客户端)
	 * 
	 * @param group
	 * @return
	 */
	public Collection<String> groups();

	/**
	 * SID对应Status
	 * 
	 * @param sid
	 * @return
	 */
	public TerminalStatus sid(String sid);
}
