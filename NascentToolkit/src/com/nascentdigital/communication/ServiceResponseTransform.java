package com.nascentdigital.communication;

public interface ServiceResponseTransform <TResponse, TResult>
{
	/**
	 * 
	 * @param responseData will be one of byte[], string, map, json, or document (based on service format)
	 * @return
	 */
	TResult transformResponseData (TResponse responseData);
}
