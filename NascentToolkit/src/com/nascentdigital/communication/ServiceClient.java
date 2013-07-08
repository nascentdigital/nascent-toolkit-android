package com.nascentdigital.communication;


import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import com.nascentdigital.threading.PriorityThreadPoolExecutor;


public class ServiceClient
{

	// [region] class variables

	private static final Comparator<ServiceOperation<?, ?>> _operationTaskComparator;

	// [endregion]

	// [region] instance variables

	private PriorityThreadPoolExecutor<ServiceOperation<?, ?>> _requestPool;
	private int _requestTimeoutInMilliseconds;

	// [endregion]

	// [region] getter/setter methods

	public PriorityThreadPoolExecutor<ServiceOperation<?, ?>> getRequestPool()
	{
		return _requestPool;
	}

	public long getRequestTimeoutInMilliseconds()
	{
		return _requestTimeoutInMilliseconds;
	}

	public void setRequestTimeoutInMilliseconds(int requestTimeoutInMilliseconds)
	{
		this._requestTimeoutInMilliseconds = requestTimeoutInMilliseconds;
	}

	// [endregion]

	// [region] constructors

	static
	{
		_operationTaskComparator = new Comparator<ServiceOperation<?, ?>>()
		{
			@Override
			public int compare(ServiceOperation<?, ?> lhs,
				ServiceOperation<?, ?> rhs)
			{
				int result =
					rhs.priority.getIntValue() - lhs.priority.getIntValue();
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


	public ServiceClient()
	{
		this(ServiceClientConstants.MAX_ACTIVE_REQUESTS,
			ServiceClientConstants.DEFAULT_REQUEST_TIMEOUT);
	}

	public ServiceClient(int maxConcurrentCount,
		int requestTimeoutInMilliseconds)
	{
		this(maxConcurrentCount, ServiceClientConstants.MAX_POOL_SIZE,
			ServiceClientConstants.POOL_KEEP_ALIVE_SECONDS,
			requestTimeoutInMilliseconds);
	}

	public ServiceClient(int maxConcurrentCount, int maxPoolSize,
		long poolKeepAliveSeconds, int requestTimeoutInMilliseconds)
	{
		_requestPool =
			new PriorityThreadPoolExecutor<ServiceOperation<?, ?>>(
				maxConcurrentCount, maxPoolSize, poolKeepAliveSeconds,
				TimeUnit.SECONDS,
				new PriorityBlockingQueue<ServiceOperation<?, ?>>(
					ServiceClientConstants.DEFAULT_QUEUE_SIZE,
					_operationTaskComparator));
		_requestTimeoutInMilliseconds = requestTimeoutInMilliseconds;
	}

	// [endregion]

	// [region] public methods

	public <TResponse, TResult> ServiceOperation<TResponse, TResult> beginRequest(
		String uri, ServiceMethod method, Map<String, String> headers,
		Map<String, String> queryParameters, String body,
		ServiceResponseFormat<TResponse> responseFormat,
		IResponseTransform<TResponse, TResult> responseTransform,
		IServiceClientCompletion<TResult> completion)
	{
		return this.beginRequest(uri, method, headers, queryParameters, body,
			responseFormat, responseTransform, completion,
			ServiceOperationPriority.NORMAL, false);
	}

	public <TResponse, TResult> ServiceOperation<TResponse, TResult> beginRequest(
		String uri, ServiceMethod method, Map<String, String> headers,
		Map<String, String> queryParameters, String body,
		ServiceResponseFormat<TResponse> responseFormat,
		IResponseTransform<TResponse, TResult> responseTransform,
		IServiceClientCompletion<TResult> completion,
		ServiceOperationPriority priority, boolean useCaches)

	{
		StringBodyDataProvider bodyDataProvider = null;
		if (body != null)
		{
			bodyDataProvider = new StringBodyDataProvider(body);
		}

		return this.beginRequest(uri, method, headers, queryParameters,
			bodyDataProvider, responseFormat, responseTransform, completion,
			priority, useCaches);
	}

	public <TResponse, TResult> ServiceOperation<TResponse, TResult> beginRequest(
		String uri, ServiceMethod method, Map<String, String> headers,
		Map<String, String> queryParameters, byte[] bodyData,
		ServiceResponseFormat<TResponse> responseFormat,
		IResponseTransform<TResponse, TResult> responseTransform,
		IServiceClientCompletion<TResult> completion)
	{
		return this.beginRequest(uri, method, headers, queryParameters,
			bodyData, responseFormat, responseTransform, completion,
			ServiceOperationPriority.NORMAL, false);
	}

	public <TResponse, TResult> ServiceOperation<TResponse, TResult> beginRequest(
		String uri, ServiceMethod method, Map<String, String> headers,
		Map<String, String> queryParameters, byte[] bodyData,
		ServiceResponseFormat<TResponse> responseFormat,
		IResponseTransform<TResponse, TResult> responseTransform,
		IServiceClientCompletion<TResult> completion,
		ServiceOperationPriority priority, boolean useCaches)

	{
		ByteBodyDataProvider bodyDataProvider = null;
		if (bodyData != null)
		{
			bodyDataProvider = new ByteBodyDataProvider(bodyData);
		}

		return this.beginRequest(uri, method, headers, queryParameters,
			bodyDataProvider, responseFormat, responseTransform, completion,
			priority, useCaches);
	}


	public <TResponse, TResult> ServiceOperation<TResponse, TResult> beginRequest(
		String uri, ServiceMethod method, Map<String, String> headers,
		Map<String, String> queryParameters,
		IBodyDataProvider bodyDataProvider,
		ServiceResponseFormat<TResponse> responseFormat,
		IResponseTransform<TResponse, TResult> responseTransform,
		IServiceClientCompletion<TResult> completion,
		ServiceOperationPriority priority, boolean useCaches)
	{

		ServiceOperation<TResponse, TResult> serviceOperation =
			new ServiceOperation<TResponse, TResult>(uri, method, headers,
				queryParameters, bodyDataProvider, responseFormat,
				responseTransform, completion, priority, useCaches,
				this._requestTimeoutInMilliseconds, this);
		this._requestPool.execute(serviceOperation);

		return serviceOperation;
	}

	
	
	
	public <TResponse> TResponse transformDataIntoResponseFormat (ServiceOperation<TResponse, ?> serviceOperation,
			byte[] responseData,
			ServiceResponseFormat<TResponse> format)
	{
		// deserialize response
			Object data;
			try
			{
				switch (format.type)
				{
					case RAW:
						data = responseData;
						break;
	
					case STRING:
						
						data = new String(responseData, "UTF-8");
						break;
	
					case FORM_ENCODED:
						data = deserializeQueryString(responseData);
						
						break;
	
					case JSON:
						data = deserializeJson(responseData);
						break;
	
					case GSON:
						data = deserializeGson(responseData);
						break;
	
					default:
						throw new UnsupportedOperationException(
							"Unexpected response format: " + format);
				}
			}
			catch (UnsupportedEncodingException e)
			{
				//this._logger.e(this.getClass().getName(), "Invalid encoding used.", e);
				return null;
			}
		

		return (TResponse) data;
	}
	
	private Object deserializeQueryString(byte[] responseData) throws UnsupportedEncodingException
	{
		String queryString = new String(responseData, "UTF-8");
		

	    Map<String, String> mappedQueryString = new HashMap<String, String>();
	    if (queryString == null || queryString.length() == 0) {
	        return mappedQueryString;
	    }
	    List<NameValuePair> list = URLEncodedUtils.parse(URI.create("http://localhost/?" + queryString), "UTF-8");
	    for (NameValuePair pair : list) {
	    	mappedQueryString.put(pair.getName(), pair.getValue());
	    }

	    return mappedQueryString;
		
	}
				
	private Object deserializeGson(byte[] responseData)
	{
		// TODO Auto-generated method stub
		return null;
	}

	private Object deserializeJson(byte[] responseData)
	{
		// TODO Auto-generated method stub
		return null;
	}


	// [endregion]

	// [region] protected methods

	protected void serviceOperationDidBegin(
		ServiceOperation<?, ?> serviceOperation)
	{

	}

	protected void serviceOperationDidEnd(
		ServiceOperation<?, ?> serviceOperation)
	{

	}

	protected boolean serviceOperationShouldRetry(
		ServiceOperation<?, ?> serviceOperation, int responseCode,
		int retryCount)
	{
		return false;
	}

	protected void serviceOperationFailed(
		ServiceOperation<?, ?> serviceOperation, Exception ex)
	{

	}
	// [endregion]

}
