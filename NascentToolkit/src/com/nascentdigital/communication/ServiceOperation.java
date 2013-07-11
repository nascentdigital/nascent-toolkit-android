package com.nascentdigital.communication;


import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import org.apache.http.util.ByteArrayBuffer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import com.nascentdigital.util.Logger;


public final class ServiceOperation<TResponse, TResult> implements Runnable
{
	// [region] constants
	private static final int HTTP_OK_STATUS_CODE = 200;
	private static final int RESPONSE_BUFFER_SIZE = 512;
	// [endregion]

	// [region] instance variables

	public final ServiceOperationPriority priority;
	public final long timestamp;
	public final String uri;
	public final ServiceMethod method;
	public final Map<String, String> headers;
	public final Map<String, String> queryParameters;

	private final BodyDataProvider _bodyDataProvider;
	private final ServiceResponseFormat<TResponse> _responseFormat;
	private final ServiceResponseTransform<TResponse, TResult> _responseTransform;
	private final ServiceClientCompletion<TResult> _completion;
	private final boolean _useCaches;
	private final ServiceClient _serviceClient;
	private final int _requestTimeoutInMilliseconds;
	private Thread _currentThread;

	// [endregion]

	// [region] constructors

	public ServiceOperation(String uri, ServiceMethod method,
		Map<String, String> headers, Map<String, String> queryParameters,
		BodyDataProvider bodyDataProvider,
		ServiceResponseFormat<TResponse> responseFormat,
		ServiceResponseTransform<TResponse, TResult> responseTransform,
		ServiceClientCompletion<TResult> completion,
		ServiceOperationPriority priority, boolean useCaches,
		int requestTimeoutInMilliseconds, ServiceClient serviceClient)
	{
		this.priority = priority;
		this.timestamp = SystemClock.elapsedRealtimeNanos();
		this.uri = uri;
		this.method = method;
		this.headers = headers;
		this.queryParameters = queryParameters;

		_bodyDataProvider = bodyDataProvider;
		_responseFormat = responseFormat;
		_responseTransform = responseTransform;
		_completion = completion;
		_useCaches = useCaches;
		_serviceClient = serviceClient;
		_requestTimeoutInMilliseconds = requestTimeoutInMilliseconds;
	}

	// [endregion]

	// [region] public methods


	@Override
	public void run()
	{
		_currentThread = Thread.currentThread();
		boolean requestInProgress = true;
		int retryCount = 0;
		do
		{
			boolean isConnected = false;
			HttpURLConnection connection = null;
			int responseCode = -1;
			try
			{
				// Check for cancellation
				throwIfInterrupted();

				// Add query string params to uri
				String uriWithQueryParams =
					addQueryStringParametersToUri(this.uri,
						this.queryParameters);

				URL url = new URL(uriWithQueryParams);

				// Create and open request/connection
				_serviceClient.serviceOperationDidBegin(this);

				connection = (HttpURLConnection)url.openConnection();
				connection.setRequestMethod(method.name());

				// Set headers
				for (String field : this.headers.keySet())
				{
					connection.setRequestProperty(field,
						this.headers.get(field));
				}

				connection.setReadTimeout(_requestTimeoutInMilliseconds);
				connection.setUseCaches(_useCaches);
				connection.setDoInput(true);

				// create and send body data to request
				byte[] bodyData =
					_bodyDataProvider == null ? null : _bodyDataProvider
						.getBodyData();

				// check for cancellation
				throwIfInterrupted();

				if (bodyData != null)
				{
					connection.setRequestProperty("Content-Length", ""
						+ bodyData.length);
					connection.setDoOutput(true);

					DataOutputStream wr =
						new DataOutputStream(connection.getOutputStream());
					wr.write(bodyData);
					wr.flush();
					wr.close();
				}
				else
				{
					connection.setDoOutput(false);
				}

				// check for cancellation
				throwIfInterrupted();

				// Get response
				InputStream in = connection.getInputStream();

				// Verify the responseCode before reading from the stream
				responseCode = connection.getResponseCode();
				if (responseCode != HTTP_OK_STATUS_CODE)
				{
					String statusMessage = connection.getResponseMessage();
					throw new InvalidResponseCodeException(responseCode,
						statusMessage);
				}

				// Check for cancellation
				throwIfInterrupted();

				BufferedInputStream bis = new BufferedInputStream(in);
				ByteArrayBuffer baf = new ByteArrayBuffer(RESPONSE_BUFFER_SIZE);
				int read = 0;
				byte[] buffer = new byte[RESPONSE_BUFFER_SIZE];
				while (true)
				{
					throwIfInterrupted();
					read = bis.read(buffer);
					if (read == -1)
					{
						break;
					}
					baf.append(buffer, 0, read);
				}
				buffer = null;

				byte[] responseBody = baf.toByteArray();

				// Disconnect to release resources
				bis.close();
				baf.clear();
				connection.disconnect();
				connection = null;
				_serviceClient.serviceOperationDidEnd(this);

				// Check for cancellation
				throwIfInterrupted();

				// process response
				TResponse data =
					_serviceClient.transformDataIntoResponseFormat(this,
						responseBody, _responseFormat);

				TResult result = null;
				if (_responseTransform != null)
				{
					result = _responseTransform.transformResponseData(data);
				}
				else
				// since there is no transform, set data to be the result
				{
					@SuppressWarnings("unchecked")
					TResult resultTemp = (TResult)data;
					result = resultTemp;
				}

				// Check for cancellation
				throwIfInterrupted();

				raiseCompletion(ServiceResultStatus.SUCCESS, responseCode, result);

				requestInProgress = false;
			}
			catch (InterruptedException ie)
			{
				Logger.e(getClass().getName(),
					"Service Operation Task Cancelled.", ie);
				raiseCompletion(ServiceResultStatus.CANCELLED, responseCode, null);
				
				requestInProgress = false;
			}
			catch (Exception ex)
			{
				Logger.e(getClass().getName(),
					"Error: Service Request Failed.", ex);

				_serviceClient.serviceOperationFailed(this, ex);
				boolean retryRequired =
					_serviceClient.serviceOperationShouldRetry(this,
						responseCode, retryCount);
				if (retryRequired)
				{
					++retryCount;
				}
				else
				{
					raiseCompletion(ServiceResultStatus.FAILED, responseCode, null);
					
					requestInProgress = false;
				}
			}
			finally
			{
				// close the connection if it hasn't been closed already
				if (connection != null && isConnected)
				{
					connection.disconnect();
				}
			}

		} while (requestInProgress);
	}
	
	// [endregion]
	
	// [region] protected methods
	
	protected void cancel ()
	{
		if (_currentThread != null)
		{
			_currentThread.interrupt();
		}
	}
	
	// [endregion]
	
	// [region] private methods

	private void raiseCompletion(final ServiceResultStatus resultStatus, final int responseCode, final TResult result)
	{
		// raise completion
		if (_completion != null)
		{
			Handler handler = new Handler(Looper.getMainLooper());
			handler.post(new Runnable()
			{
			    public void run()
			    {
					_completion.onCompletion(resultStatus, responseCode, result);
			    }
			});//end runOnUiThread

		}
	}

	private void throwIfInterrupted() throws InterruptedException
	{
		if (_currentThread.isInterrupted())
		{
			throw new InterruptedException();
		}
	}

	private static String addQueryStringParametersToUri(String uri,
		Map<String, String> queryParameters)
		throws UnsupportedEncodingException
	{
		String uriWithQueryParams = uri;
		if (queryParameters != null && !queryParameters.isEmpty())
		{
			uriWithQueryParams += "?";
			boolean addAmperstand = false;
			for (String field : queryParameters.keySet())
			{
				if (addAmperstand)
				{
					uriWithQueryParams += "&";
				}
				uriWithQueryParams +=
					field
						+ "="
						+ URLEncoder.encode(queryParameters.get(field),
							ServiceClientConstants.UTF8_ENCODING);
				addAmperstand = true;
			}
		}
		return uriWithQueryParams;
	}


	// [endregion]
	


}