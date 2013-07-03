package com.nascentdigital.communication;

public interface IServiceClientCompletion <T>
{
	void onCompletion (ServiceResult serviceResult, T resultValue);

}
