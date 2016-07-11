package com.nascentdigital.communication;

public interface ServiceClientCompletion <T>
{
	void onCompletion (ServiceResultStatus serviceResultStatus, int responseCode, T resultValue);

}
