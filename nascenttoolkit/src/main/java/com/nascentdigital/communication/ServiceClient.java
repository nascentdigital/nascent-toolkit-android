
package com.nascentdigital.communication;


import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.nascentdigital.threading.PriorityThreadPoolExecutor;
import com.nascentdigital.util.Logger;


public class ServiceClient
{

	// [region] class variables
	
	private static final Comparator<ServiceOperation<?, ?>> _operationTaskComparator;

	// [endregion]

	// [region] instance variables

	private final PriorityThreadPoolExecutor<ServiceOperation<?, ?>> _requestPool;
	private int _requestTimeoutInMilliseconds;
	protected SSLContextFactory _sslContextFactory;

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
		_operationTaskComparator = (lhs, rhs) -> {
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
		this(maxConcurrentCount, maxConcurrentCount,
			ServiceClientConstants.POOL_KEEP_ALIVE_SECONDS,
			requestTimeoutInMilliseconds);
	}

	public ServiceClient(int maxConcurrentCount, int maxPoolSize,
		long poolKeepAliveSeconds, int requestTimeoutInMilliseconds)
	{
		_requestPool =
				new PriorityThreadPoolExecutor<>(
						maxConcurrentCount, maxPoolSize, poolKeepAliveSeconds,
						TimeUnit.SECONDS,
						new PriorityBlockingQueue<>(
								ServiceClientConstants.DEFAULT_QUEUE_SIZE,
								_operationTaskComparator));
		_requestTimeoutInMilliseconds = requestTimeoutInMilliseconds;
	}

	// [endregion]

	// [region] public methods

	public <TResponse, TResult> ServiceOperation<TResponse, TResult> beginRequest(
		String uri, 
		ServiceMethod method, 
		Map<String, String> headers,
		Map<String, String> queryParameters, 
		String body,
		ServiceResponseFormat<TResponse> responseFormat,
		ServiceResponseTransform<TResponse, TResult> responseTransform,
		ServiceClientCompletion<TResult> completion)
	{
		return this.beginRequest(uri, method, headers, queryParameters, body,
			responseFormat, responseTransform, completion,
			ServiceOperationPriority.NORMAL, false);
	}

	public <TResponse, TResult> ServiceOperation<TResponse, TResult> beginRequest(
		String uri, 
		ServiceMethod method, Map<String, String> headers,
		Map<String, String> queryParameters,
		String body,
		ServiceResponseFormat<TResponse> responseFormat,
		ServiceResponseTransform<TResponse, TResult> responseTransform,
		ServiceClientCompletion<TResult> completion,
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
		String uri, 
		ServiceMethod method, 
		Map<String, String> headers,
		Map<String, String> queryParameters, 
		byte[] bodyData,
		ServiceResponseFormat<TResponse> responseFormat,
		ServiceResponseTransform<TResponse, TResult> responseTransform,
		ServiceClientCompletion<TResult> completion)
	{
		return this.beginRequest(uri, method, headers, queryParameters,
			bodyData, responseFormat, responseTransform, completion,
			ServiceOperationPriority.NORMAL, false);
	}

	public <TResponse, TResult> ServiceOperation<TResponse, TResult> beginRequest(
		String uri, 
		ServiceMethod method, Map<String, String> headers,
		Map<String, String> queryParameters, 
		byte[] bodyData,
		ServiceResponseFormat<TResponse> responseFormat,
		ServiceResponseTransform<TResponse, TResult> responseTransform,
		ServiceClientCompletion<TResult> completion,
		ServiceOperationPriority priority, 
		boolean useCaches)

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
		String uri, 
		ServiceMethod method, 
		Map<String, String> headers,
		Map<String, String> queryParameters,
		BodyDataProvider bodyDataProvider,
		ServiceResponseFormat<TResponse> responseFormat,
		ServiceResponseTransform<TResponse, TResult> responseTransform,
		ServiceClientCompletion<TResult> completion,
		ServiceOperationPriority priority, 
		boolean useCaches)
	{

		ServiceOperation<TResponse, TResult> serviceOperation =
				new ServiceOperation<>(uri, method, headers,
						queryParameters, bodyDataProvider, responseFormat,
						responseTransform, completion, priority, useCaches,
						this._requestTimeoutInMilliseconds, this, _sslContextFactory);
		this._requestPool.execute(serviceOperation);

		return serviceOperation;
	}

	
	
	
	public <TResponse> TResponse transformDataIntoResponseFormat (ServiceOperation<TResponse, ?> serviceOperation,
			byte[] responseData,
			ServiceResponseFormat<TResponse> format)
	{
			// deserialize response
			Object data = null;
			try
			{
				switch (format.type)
				{
					case RAW:
						data = responseData;
						break;
	
					case STRING:					
						data = new String(responseData, ServiceClientConstants.UTF8_ENCODING);
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
						
					case XML:
						data = deserializeXml(responseData);
						break;
	
					default:
						throw new UnsupportedOperationException(
							"Unexpected response format: " + format);
				}
			}
			catch (UnsupportedEncodingException e)
			{
				Logger.e(this.getClass().getName(), "Invalid encoding used.", e);
				return null;
			}
			catch (JSONException e)
			{
				Logger.e(this.getClass().getName(), "Error Parsing JSON", e);
				return null;
			}
			catch (ParserConfigurationException e)
			{
				Logger.e(this.getClass().getName(), "Error Parsing XML", e);
				return null;
			}
			catch (SAXException e)
			{
				Logger.e(this.getClass().getName(), "Error Parsing XML", e);
				return null;
			}
			catch (IOException e)
			{
				Logger.e(this.getClass().getName(), "Error Parsing XML", e);
				return null;
			}

		@SuppressWarnings("unchecked")
		TResponse result = (TResponse) data;
		return result;
	}
	
	private static Document deserializeXml(byte[] responseData) throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			// This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all XML entity attacks are prevented
			// Xerces 2 only - http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl
			dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

			// If you can't completely disable DTDs, then at least do the following:
			// Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
			// Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
			dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);

			// Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
			// Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
			dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);


			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

			// and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks" (see reference below)
			dbf.setXIncludeAware(false);
			dbf.setExpandEntityReferences(false);
			dbf.setValidating(false);

			// And, per Timothy Morgan: "If for some reason support for inline DOCTYPEs are a requirement, then
			// ensure the entity settings are disabled (as shown above) and beware that SSRF attacks
			// (http://cwe.mitre.org/data/definitions/918.html) and denial
			// of service attacks (such as billion laughs or decompression bombs via "jar:") are a risk."

			// remaining parser logic
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();

			return documentBuilder.parse(new ByteArrayInputStream(responseData));
		}
        catch (Exception e) {
            // This should catch a failed setFeature feature
            Log.e("ServiceClient", "error setting up parser: ", e);
            throw e;
        }


	}

	private static Map<String, String> deserializeQueryString(byte[] responseData) throws UnsupportedEncodingException
	{
		String queryString = new String(responseData, ServiceClientConstants.UTF8_ENCODING);
		

	    Map<String, String> mappedQueryString = new HashMap<>();
	    if (queryString == null || queryString.length() == 0) {
	        return mappedQueryString;
	    }
	    List<NameValuePair> list = URLEncodedUtils.parse(URI.create("http://localhost/?" + queryString), "UTF-8");
	    for (NameValuePair pair : list) {
	    	mappedQueryString.put(pair.getName(), pair.getValue());
	    }

	    return mappedQueryString;		
	}
	
	private static JSONObject deserializeJson(byte[] responseData)
		throws JSONException, UnsupportedEncodingException
	{
		String json = new String(responseData, ServiceClientConstants.UTF8_ENCODING);
		JSONTokener jsonParser = new JSONTokener(json);
		return (JSONObject)jsonParser.nextValue();
	}

	private static JsonElement deserializeGson(byte[] responseData)
		throws UnsupportedEncodingException
	{
		String json = new String(responseData, ServiceClientConstants.UTF8_ENCODING);
		JsonParser jsonParser = new JsonParser();
		return jsonParser.parse(json);
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
		Log.e("ServiceClient", "error Service Operation Failed: ", ex);
	}
	// [endregion]

}
