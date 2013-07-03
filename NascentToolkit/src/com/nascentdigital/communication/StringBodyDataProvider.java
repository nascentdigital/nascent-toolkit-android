package com.nascentdigital.communication;

import java.nio.charset.Charset;

/**
 * Default string body data provider that converts string to bytes using UTF-8
 *
 */
public class StringBodyDataProvider implements IBodyDataProvider
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
		return body.getBytes(Charset.defaultCharset());
	}
	// [endregion]
	
	
}
