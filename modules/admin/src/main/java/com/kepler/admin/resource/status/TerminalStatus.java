package com.kepler.admin.resource.status;

import com.kepler.host.HostStatus;

/**
 * 指定SID的Host静态状态
 * 
 * @author kim 2016年1月4日
 */
public interface TerminalStatus extends HostStatus {

	public String getPath();
}
