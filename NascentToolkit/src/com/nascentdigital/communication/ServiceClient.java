package com.nascentdigital.communication;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import android.graphics.Bitmap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.nascentdigital.threading.PriorityThreadPoolExecutor;
import com.nascentdigital.util.Logger;


public class ServiceClient
{

	// [region] class variables

	private static final Comparator<ServiceOperation<?, ?>> _operationTaskComparator;

	public static enum MultiPartDataType
	{
		VIDEO,
		IMAGE
	}

	// [endregion]

	// [region] instance variables

	private final PriorityThreadPoolExecutor<ServiceOperation<?, ?>> _requestPool;
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
		this(maxConcurrentCount, maxConcurrentCount,
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
			new ServiceOperation<TResponse, TResult>(uri, method, headers,
				queryParameters, bodyDataProvider, responseFormat,
				responseTransform, completion, priority, useCaches,
				this._requestTimeoutInMilliseconds, this);
		this._requestPool.execute(serviceOperation);

		return serviceOperation;
		}


	//To handle MultiPart POST requests for files
	public <TResponse, TResult> ServiceOperation<TResponse, TResult> beginRequest(
		String uri,
		ServiceMethod method,
		Map<String, String> headers,
		Map<String, String> queryParameters,
		MultiPartEntity [] beforeSegments,
		MultiPartEntity dataSegment,
		MultiPartEntity [] afterSegments,
		ServiceResponseFormat<TResponse> responseFormat,
		ServiceResponseTransform<TResponse, TResult> serviceResponseTransform,
		ServiceClientCompletion<TResult> completion,
		ServiceOperationPriority priority,
		boolean useCaches, boolean multiPart)
		{
		ServiceOperation<TResponse, TResult> serviceOperation =
			new ServiceOperation<TResponse, TResult>(uri, method, headers,
				queryParameters, beforeSegments, dataSegment, afterSegments, responseFormat,
				serviceResponseTransform, completion, priority, useCaches,
				this._requestTimeoutInMilliseconds, this, multiPart);
		this._requestPool.execute(serviceOperation);

		return serviceOperation;
		}

	//To handle MultiPart POST requests for bitmaps
	public <TResponse, TResult> ServiceOperation<TResponse, TResult> beginRequest(
		String uri,
		ServiceMethod method,
		Map<String, String> headers,
		Map<String, String> queryParameters,
		byte[] bodyData,
		ServiceResponseFormat<TResponse> responseFormat,
		ServiceResponseTransform<TResponse, TResult> serviceResponseTransform,
		ServiceClientCompletion<TResult> completion,
		ServiceOperationPriority priority,
		boolean useCaches, boolean multiPart)
		{

		ByteBodyDataProvider bodyDataProvider = null;
		if (bodyData != null)
		{
			bodyDataProvider = new ByteBodyDataProvider(bodyData);
		}

		ServiceOperation<TResponse, TResult> serviceOperation =
			new ServiceOperation<TResponse, TResult>(uri, method, headers,
				queryParameters, bodyDataProvider, responseFormat,
				serviceResponseTransform, completion, priority, useCaches,
				this._requestTimeoutInMilliseconds, this, multiPart);
		this._requestPool.execute(serviceOperation);

		return serviceOperation;
		}

	public String addMultiPartString(String content, String name, String contentType)
	{
		StringBuilder partData = new StringBuilder();
		partData.append("Content-Disposition: form-data; name=\"" + name + "\"" + ServiceOperation.lineEnd);
		partData.append("Content-Type: " + contentType + ServiceOperation.lineEnd + ServiceOperation.lineEnd);
		partData.append(content);
		partData.append(ServiceOperation.lineEnd);

		return partData.toString();
	}

	public byte[] addMultiPartData(MultiPartDataType dataType, Object content, String name, String filename, String contentType)
	{
		String disp = "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + filename + "\"" + ServiceOperation.lineEnd;
		String cont = "Content-Type: " + contentType + ServiceOperation.lineEnd + ServiceOperation.lineEnd;

		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		Bitmap _image = (Bitmap) content;
		try
		{
			stream.write(disp.getBytes());
			stream.write(cont.getBytes());
			_image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		}
		catch (IOException e1)
		{
			return null;
		}

		byte[] byteArray = stream.toByteArray();
		return byteArray;
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

	private static final Document deserializeXml(byte[] responseData) throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = documentBuilder.parse(new ByteArrayInputStream(responseData));

		return document;
	}

	private static final Map<String, String> deserializeQueryString(byte[] responseData) throws UnsupportedEncodingException
	{
		String queryString = new String(responseData, ServiceClientConstants.UTF8_ENCODING);


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

	private static final JSONObject deserializeJson(byte[] responseData)
		throws JSONException, UnsupportedEncodingException
		{
		String json = new String(responseData, ServiceClientConstants.UTF8_ENCODING);
		JSONTokener jsonParser = new JSONTokener(json);
		return (JSONObject)jsonParser.nextValue();
		}

	private static final JsonElement deserializeGson(byte[] responseData)
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

	}
	// [endregion]

}
