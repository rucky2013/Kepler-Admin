package com.kepler.admin.traces;

/**
 * @author KimShen
 *
 */
public interface Trace {

	public String getTrace();

	public String getCause();

	public String getService();

	public String getMethod();

	public String getVersionAndCatalog();
}
