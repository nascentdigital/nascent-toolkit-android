package com.nascentdigital.communication;

public interface IServiceClientCompletion <T>
{
	void onCompletion (ServiceResultStatus serviceResultStatus, int responseCode, T resultValue);

}
