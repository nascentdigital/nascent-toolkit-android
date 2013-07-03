package com.nascentdigital.common;

/**
 * Logging interface for a logging class that can be passed to the ServiceClient
 * library for logging. Based on the standard android Log class.
 */
public interface ILog {
	
	void d(String context, String message);

	void v(String context, String message);

	void i(String context, String message);

	void w(String context, String message);

	void e(String context, String message);

	void e(String context, String message, Exception e);

}
