package com.nascentdigital.communication;

public interface IResponseTransform <TResponse, TResult>
{
	/**
	 * 
	 * @param responseData will be one of byte[], string, map, json, or xml (based on service format)
	 * @return
	 */
	TResult transformResponseData (TResponse responseData);
}
