package com.nascentdigital.communication;

public interface IResponseTransform <T>
{
	/**
	 * 
	 * @param responseData will be one of byte[], string, map, json, or xml (based on service format)
	 * @return
	 */
	T transformResponseData (Object responseData);
}
