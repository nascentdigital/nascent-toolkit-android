package com.nascentdigital.communication;

public interface ServiceClientCompletion <T>
{
	void onCompletion (ServiceResultContainer<T> serviceResultContainer);

}
