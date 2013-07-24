package com.nascentdigital.communication;

public class ServiceClientConstants {
	// [region] constants
	public static final int DEFAULT_REQUEST_TIMEOUT = 30 * 1000;
	public static final int MAX_ACTIVE_REQUESTS = 1;
	public static final int MAX_POOL_SIZE = MAX_ACTIVE_REQUESTS;
	public static final int POOL_KEEP_ALIVE_SECONDS = 30;
	public static final int DEFAULT_QUEUE_SIZE = 16;
	public static final String UTF8_ENCODING = "UTF-8";
	
	public static final int SERVICE_RESPONSE_STATUS_CODE_ALERT_MESSAGE = -400;
	public static final int SERVICE_RESPONSE_STATUS_CODE_CANCELLED = -300;
	public static final int SERVICE_RESPONSE_STATUS_CODE_BAD_DATA = -200;
	public static final int SERVICE_RESPONSE_STATUS_CODE_ARCHIVED = -100;
	public static final int SERVICE_RESPONSE_STATUS_CODE_UNKNOWN = -1;
	public static final int SSERVICE_RESPONSE_STATUS_CODE_NONE = 0;
	public static final int SERVICE_RESPONSE_STATUS_CODE_SUCCESS = 200;
	public static final int SSERVICE_RESPONSE_STATUS_CODE_BAD_PARAMETERS = 400;
	public static final int SERVICE_RESPONSE_STATUS_CODE_UNAUTHORIZED = 401;
	public static final int SERVICE_RESPONSE_STATUS_CODE_INTERNAL_SERVER_ERROR = 500;
	public static final int SERVICE_RESPONSE_STATUS_CODE_SERVER_ERROR = 501;
	// [endregion]
}
