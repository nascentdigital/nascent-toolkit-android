package com.nascentdigital.communication;

public class ServiceResult
{
	//[region] instance variables
	private ServiceResultStatus serviceResultStatus;
	private int responseCode;
	private String responseMessage; 
	//[endregion]
	
	//[region] cctor
	public ServiceResult(ServiceResultStatus serviceResultStatus,
			int responseCode, String responseMessage) {
		this.serviceResultStatus = serviceResultStatus;
		this.responseCode = responseCode;
		this.responseMessage = responseMessage;
	}
	
	//[endregion]
	
	//[region] public methods
	public ServiceResultStatus getServiceResultStatus() {
		return serviceResultStatus;
	}

	public void setServiceResultStatus(ServiceResultStatus serviceResultStatus) {
		this.serviceResultStatus = serviceResultStatus;
	}
	public int getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
	public String getResponseMessage() {
		return responseMessage;
	}
	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}
	//[endregion]
}


