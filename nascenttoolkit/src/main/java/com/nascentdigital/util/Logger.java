package com.nascentdigital.util;


import android.util.Log;


public final class Logger
{
	// [region] class variables

	public static final LogLevel level = LogLevel.VERBOSE;

	// [endregion]


	// [region] constructors

	private Logger()
	{
	}

	// [endregion]


	// [region] public methods

	public static void v(String context, String message)
	{
		if (level != null
			&& level.allows(LogLevel.VERBOSE))
		{
			Log.v(context, message);
		}
	}

	public static void d(String context, String message)
	{
		if (level != null
			&& level.allows(LogLevel.DEBUG))
		{
			Log.d(context, message);
		}
	}

	public static void i(String context, String message)
	{
		if (level != null
			&& level.allows(LogLevel.INFO))
		{
			Log.i(context, message);
		}
	}

	public static void w(String context, String message)
	{
		if (level != null
			&& level.allows(LogLevel.WARN))
		{
			Log.w(context, message);
		}
	}

	public static void e(String context, String message)
	{
		if (level != null
			&& level.allows(LogLevel.ERROR))
		{
			Log.e(context, message);
		}
	}

	public static void e(String context, String message, Exception e)
	{
		if (level != null
			&& level.allows(LogLevel.ERROR))
		{
			Log.e(context, message, e);
		}
	}

	// [endregion]

} // class Logger
