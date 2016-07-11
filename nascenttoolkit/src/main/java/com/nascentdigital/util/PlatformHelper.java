package com.nascentdigital.util;


import android.content.Context;
import android.content.res.Configuration;


public final class PlatformHelper
{
	// [region] constants

	// [endregion]


	// [region] instance variables

	// [endregion]


	// [region] constructors

	private PlatformHelper()
	{
	}

	// [endregion]


	// [region] properties

	// [endregion]


	// [region] public methods

	/**
	 * returns true if the application is running in a tablet experience.
	 * @param context application context
	 * @return true if the application is running in a tablet experience.
     */
	public static boolean isTablet(Context context)
	{
		int screenSize =
			context.getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK;
		return screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE
			|| screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}
	// [endregion]

}
