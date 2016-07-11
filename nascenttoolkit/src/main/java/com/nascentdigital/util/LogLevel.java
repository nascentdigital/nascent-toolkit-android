package com.nascentdigital.util;


public enum LogLevel
{
	NONE(0),

	VERBOSE(1),

	DEBUG(2),

	INFO(3),

	WARN(4),

	ERROR(5);


	// [region] instance variables

	public final int value;

	// [endregion]


	// [region] constructors

	private LogLevel(int value)
	{
		this.value = value;
	}

	// [endregion]


	// [region] public methods

	/**
	 * Return <code>true</code> if the specified <code>LogLevel</code> is
	 * allowed when the current instance value is active.
	 * 
	 * @param other
	 *            the <code>LogLevel</code> being checked
	 */
	public boolean allows(LogLevel other)
	{
		return value <= other.value;
	}

	// [endregion]

} // enum LogLevel
