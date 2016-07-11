package com.nascentdigital.communication;

public class InvalidResponseCodeException extends Exception
{
	private static final long serialVersionUID = 1L;
	public final int responseCode;
	public final String responseMessage;
	public final String errorMessage;
	
	public InvalidResponseCodeException(int responseCode, String responseMessage, String errorMessage)
	{
		super(responseCode + " : " + responseMessage + " : " + errorMessage);
		this.responseCode = responseCode;
		this.responseMessage = responseMessage;
		this.errorMessage = errorMessage;
	}

}
