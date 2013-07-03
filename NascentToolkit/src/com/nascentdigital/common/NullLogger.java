package com.nascentdigital.common;

/**
 * Empty logging class to be used when no ILog implementation is passed
 * to the ServiceClient. 
 *
 */
public final class NullLogger implements ILog {

	@Override
	public void d(String context, String message) {
		
	}

	@Override
	public void v(String context, String message) {
		
	}

	@Override
	public void i(String context, String message) {
		
	}

	@Override
	public void w(String context, String message) {
		
	}

	@Override
	public void e(String context, String message) {
		
	}

	@Override
	public void e(String context, String message, Exception e) {
		
	}

}
