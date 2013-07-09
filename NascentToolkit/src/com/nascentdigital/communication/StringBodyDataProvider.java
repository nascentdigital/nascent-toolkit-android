package com.nascentdigital.communication;

import java.io.UnsupportedEncodingException;
import com.nascentdigital.util.Logger;

/**
 * Default string body data provider that converts string to bytes using UTF-8
 *
 */
public class StringBodyDataProvider implements BodyDataProvider
{
	// [region] instance variables
	private String body;
	// [endregion]
	
	// [region] constructors
	public StringBodyDataProvider (String body)
	{
		this.body = body;
	}
	// [endregion]
	
	// [region] public methods
	@Override
	public byte[] getBodyData() {
		if (body == null)
		{
			return null;
		}
		//On android the default charset is UTF-8
		try
		{
			return body.getBytes(ServiceClientConstants.UTF8_ENCODING);
		}
		catch (UnsupportedEncodingException e)
		{
			Logger.e(this.getClass().getName(), "Encoding Exception Transforming Request", e);
			return null;
		}
	}
	// [endregion]
	
	
}
