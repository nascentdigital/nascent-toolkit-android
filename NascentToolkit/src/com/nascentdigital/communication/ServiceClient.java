package com.nascentdigital.communication;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;


import com.nascentdigital.common.ILog;
import com.nascentdigital.common.NullLogger;
import com.nascentdigital.threading.PriorityThreadPoolExecutor;


public class ServiceClient{
	
	// [region] class variables
	
	private static final Comparator<ServiceOperation> _operationTaskComparator;
	
	// [endregion]
	
	// [region] instance variables
	private PriorityThreadPoolExecutor<ServiceOperation> requestQueue;
	private long requestTimeoutInMilliseconds;
	private ILog logger;
	// [endregion]
	
	// [region] getter/setter methods
	public PriorityThreadPoolExecutor<ServiceOperation> getRequestQueue() {
		return requestQueue;
	}

	public long getRequestTimeoutInMilliseconds() {
		return requestTimeoutInMilliseconds;
	}

	public void setRequestTimeoutInMilliseconds(long requestTimeoutInMilliseconds) {
		this.requestTimeoutInMilliseconds = requestTimeoutInMilliseconds;
	}
	
	public ILog getLogger() {
		return logger;
	}

	public void setLogger(ILog logger) {
		if (logger == null)
		{
			logger = new NullLogger();
		}
		this.logger = logger;
	}
	// [endregion]
	
	// [region] constructors
	
	static 
	{
		_operationTaskComparator = new Comparator<ServiceOperation>()
			{
			@Override
			public int compare(ServiceOperation lhs,
				ServiceOperation rhs)
			{
				int result = rhs.priority.getIntValue() - lhs.priority.getIntValue();
				if (result == 0)
				{
					if (lhs.timestamp < rhs.timestamp)
					{
						return -1;
					}
					else if (lhs.timestamp > rhs.timestamp)
					{
						return 1;
					}
					return 0;
				}
				return result;
			}
		};
	}
	
	
	public ServiceClient ()
	{
		this (ServiceClientConstants.MAX_ACTIVE_REQUESTS, ServiceClientConstants.DEFAULT_REQUEST_TIMEOUT, new NullLogger());
	}
	
	public ServiceClient (int maxConcurrentCount, 
		long requestTimeoutInMilliseconds)
	{
		this(maxConcurrentCount, ServiceClientConstants.MAX_POOL_SIZE, ServiceClientConstants.POOL_KEEP_ALIVE_SECONDS, requestTimeoutInMilliseconds, new NullLogger());
	}
	
	public ServiceClient (int maxConcurrentCount, 
		long requestTimeoutInMilliseconds, 
		ILog logger)
	{
		this(maxConcurrentCount, ServiceClientConstants.MAX_POOL_SIZE, ServiceClientConstants.POOL_KEEP_ALIVE_SECONDS, requestTimeoutInMilliseconds, logger);
	}
	
	public ServiceClient 
		(int maxConcurrentCount,
		int maxPoolSize, 
		long poolKeepAliveSeconds,
		long requestTimeoutInMilliseconds,
		ILog logger)
	{
		// FIXME: pull out magic number as constant
		this.requestQueue = new PriorityThreadPoolExecutor<ServiceOperation>(maxConcurrentCount, maxPoolSize, poolKeepAliveSeconds, TimeUnit.SECONDS, 
			new PriorityBlockingQueue<ServiceOperation>(16, _operationTaskComparator));
		
		setLogger( logger);
		this.requestTimeoutInMilliseconds = requestTimeoutInMilliseconds;

	}
	
	// [endregion]
	
	// [region] public methods
	
	
	public <T> ServiceOperation beginRequest (String uri,
			ServiceMethod method,
			Map<String, String> headers,
			Map<String, String> queryParameters,
			String body,
			ServiceFormat responseFormat,
			IResponseTransform<T> responseTransform,
			IServiceClientCompletion<T> completion)
	{
		return this.beginRequest(uri, 
				method, 
				headers, 
				queryParameters, 
				body, 
				responseFormat, 
				responseTransform, 
				completion, 
				ServiceOperationPriority.NORMAL, 
				false);
	}
	
	public <T> ServiceOperation beginRequest (String uri,
			ServiceMethod method,
			Map<String, String> headers,
			Map<String, String> queryParameters,
			String body,
			ServiceFormat responseFormat,
			IResponseTransform<T> responseTransform,
			IServiceClientCompletion<T> completion,
			ServiceOperationPriority priority,
			boolean useCaches)
	
	{
		StringBodyDataProvider bodyDataProvider = null;
		if (body != null)
		{
			bodyDataProvider = new StringBodyDataProvider (body);
		}
		
		return this.beginRequest(uri, 
				method, 
				headers, 
				queryParameters, 
				bodyDataProvider, 
				responseFormat, 
				responseTransform, 
				completion, 
				priority, 
				useCaches);
	}
	
	public <T> ServiceOperation beginRequest (String uri,
			ServiceMethod method,
			Map<String, String> headers,
			Map<String, String> queryParameters,
			byte[] bodyData,
			ServiceFormat responseFormat,
			IResponseTransform<T> responseTransform,
			IServiceClientCompletion<T> completion)
	{
		return this.beginRequest(uri, 
				method, 
				headers, 
				queryParameters, 
				bodyData, 
				responseFormat, 
				responseTransform, 
				completion, 
				ServiceOperationPriority.NORMAL, 
				false);
	}
	
	public <T> ServiceOperation beginRequest (String uri,
			ServiceMethod method,
			Map<String, String> headers,
			Map<String, String> queryParameters,
			byte[] bodyData,
			ServiceFormat responseFormat,
			IResponseTransform<T> responseTransform,
			IServiceClientCompletion<T> completion,
			ServiceOperationPriority priority,
			boolean useCaches)
	
	{
		ByteBodyDataProvider bodyDataProvider = null;
		if (bodyData != null)
		{
			bodyDataProvider = new ByteBodyDataProvider (bodyData);
		}
		
		return this.beginRequest(uri, 
				method, 
				headers, 
				queryParameters, 
				bodyDataProvider, 
				responseFormat, 
				responseTransform, 
				completion, 
				priority, 
				useCaches);
	}
	
	
	public <T> ServiceOperation beginRequest (String uri,
			ServiceMethod method,
			Map<String, String> headers,
			Map<String, String> queryParameters,
			IBodyDataProvider bodyDataProvider,
			ServiceFormat responseFormat,
			IResponseTransform<T> responseTransform,
			IServiceClientCompletion<T> completion,
			ServiceOperationPriority priority,
			boolean useCaches)
	{
		
		//TODO: Implementation
		
		return null;
	}
	
	
	
	public Object transformDataIntoIntermediateFormat (ServiceOperation serviceOperation,
			byte[] data,
			ServiceFormat format)
	{
		return null;
	}
				
	// [endregion]
	
	// [region] protected methods
	
	protected void serviceOperationDidBegin (ServiceOperation serviceOperation)
	{
		
	}
	
	protected void serviceOperationDidEnd (ServiceOperation serviceOperation)
	{
		
	}
	
	protected boolean serviceOperationShouldRetry (ServiceOperation serviceOperation, 
			ServiceResult result, 
			byte[] data, 
			int retryCount)
	{
		return false;
	}
	
	protected void serviceOperationFailed (ServiceOperation serviceOperation)
	{
		
	}
	// [endregion]

}


