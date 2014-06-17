package com.nascentdigital.util;


import android.util.Log;


public final class Logger
{
	// [region] class variables

	public static volatile LogLevel level = LogLevel.INFO;

	// [endregion]


	// [region] constructors

	private Logger()
	{
	}

	// [endregion]


	// [region] public methods

	public static final void v(String context, String message)
	{
		if (level != null
			&& level.allows(LogLevel.VERBOSE))
		{
			Log.v(context, message);
		}
	}

	public static final void d(String context, String message)
	{
		if (level != null
			&& level.allows(LogLevel.DEBUG))
		{
			Log.d(context, message);
		}
	}

	public static final void i(String context, String message)
	{
		if (level != null
			&& level.allows(LogLevel.INFO))
		{
			Log.i(context, message);
		}
	}

	public static final void w(String context, String message)
	{
		if (level != null
			&& level.allows(LogLevel.WARN))
		{
			Log.w(context, message);
		}
	}

	public static final void e(String context, String message)
	{
		if (level != null
			&& level.allows(LogLevel.ERROR))
		{
			Log.e(context, message);
		}
	}

	public static final void e(String context, String message, Exception e)
	{
		if (level != null
			&& level.allows(LogLevel.ERROR))
		{
			Log.e(context, message, e);
		}
	}

	// [endregion]

} // class Logger
