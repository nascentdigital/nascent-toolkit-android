package com.nascentdigital.communication;

/**
 * Created by andyrohan on 2016-07-12.
 */
public class ServiceResultContainer <TResult> {
    public final ServiceResponseHeaders serviceResponseHeaders;
    public final int serviceResponseCode;
    public final TResult serviceResult;
    public final ServiceResultStatus serviceResultStatus;

    public ServiceResultContainer(ServiceResponseHeaders serviceResponseHeaders,
                                  int serviceResponseCode,
                                  TResult result,
                                  ServiceResultStatus serviceResultStatus) {
        this.serviceResponseHeaders = serviceResponseHeaders;
        this.serviceResponseCode = serviceResponseCode;
        this.serviceResult = result;
        this.serviceResultStatus = serviceResultStatus;
    }
}
