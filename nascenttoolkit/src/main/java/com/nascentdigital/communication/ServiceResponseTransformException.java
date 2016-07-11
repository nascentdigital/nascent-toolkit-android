package com.nascentdigital.communication;

public class ServiceResponseTransformException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6571960605185642322L;

	public ServiceResponseTransformException()
	{

	}

	public ServiceResponseTransformException(String detailMessage)
	{
		super(detailMessage);

	}

	public ServiceResponseTransformException(Throwable throwable)
	{
		super(throwable);

	}

	public ServiceResponseTransformException(String detailMessage,
		Throwable throwable)
	{
		super(detailMessage, throwable);

	}

}
