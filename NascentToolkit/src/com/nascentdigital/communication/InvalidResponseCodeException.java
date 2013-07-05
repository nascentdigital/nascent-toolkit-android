package com.nascentdigital.communication;

public class InvalidResponseCodeException extends Exception
{
	private static final long serialVersionUID = 1L;
	public final int responseCode;
	public final String responseMessage;
	
	public InvalidResponseCodeException(int responseCode, String responseMessage)
	{
		super(responseCode + ":" + responseMessage);
		this.responseCode = responseCode;
		this.responseMessage = responseMessage;
	}

}
