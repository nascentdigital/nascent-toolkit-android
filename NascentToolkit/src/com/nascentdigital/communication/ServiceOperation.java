package com.nascentdigital.communication;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.util.ByteArrayBuffer;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.nascentdigital.util.Logger;

public final class ServiceOperation<TResponse, TResult> implements Runnable {
	// [region] constants
	private static final int HTTP_OK_STATUS_CODE = 200;
	private static final int HTTP_MULTIPLE_CHOICES_CODE = 300;
	private static final int RESPONSE_BUFFER_SIZE = 512;

	public static final String twoHyphens = "--";
	public static final String boundary =  "__com.nascentdigital.communication__";
	public static final String lineEnd = "\r\n";

	private static final String multiPartDivider = twoHyphens + boundary + lineEnd;
	private static final String multiPartDataDivider = twoHyphens + boundary + lineEnd;
	private static final String multiPartFinalDivider = twoHyphens + boundary + twoHyphens + lineEnd;
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

	private boolean _multiPartRequest;
	private MultiPartEntity [] _preDataEntities;
	private MultiPartEntity [] _postDataEntities;
	private MultiPartEntity _dataEntity;

	// [endregion]

	// [region] constructors

	public ServiceOperation(String uri, ServiceMethod method,
		Map<String, String> headers, Map<String, String> queryParameters,
		BodyDataProvider bodyDataProvider,
		ServiceResponseFormat<TResponse> responseFormat,
		ServiceResponseTransform<TResponse, TResult> responseTransform,
		ServiceClientCompletion<TResult> completion,
		ServiceOperationPriority priority, boolean useCaches,
		int requestTimeoutInMilliseconds, ServiceClient serviceClient) {
		this.priority = priority;
		this.timestamp = (new Date().getTime());
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

	//Constructor for multiPart POST requests
	public ServiceOperation(String uri, ServiceMethod method,
		Map<String, String> headers, Map<String, String> queryParameters,
		MultiPartEntity [] preDataEntities,
		MultiPartEntity dataEntity,
		MultiPartEntity [] postDataEntities,
		ServiceResponseFormat<TResponse> responseFormat,
		ServiceResponseTransform<TResponse, TResult> responseTransform,
		ServiceClientCompletion<TResult> completion,
		ServiceOperationPriority priority, boolean useCaches,
		int requestTimeoutInMilliseconds, ServiceClient serviceClient, boolean multiPart) {
		this.priority = priority;
		this.timestamp = (new Date().getTime());
		this.uri = uri;
		this.method = method;
		this.headers = headers;
		this.queryParameters = queryParameters;

		_bodyDataProvider = null;
		_preDataEntities = preDataEntities;
		_dataEntity = dataEntity;
		_postDataEntities = postDataEntities;
		_responseFormat = responseFormat;
		_responseTransform = responseTransform;
		_completion = completion;
		_useCaches = useCaches;
		_serviceClient = serviceClient;
		_requestTimeoutInMilliseconds = requestTimeoutInMilliseconds;
		_multiPartRequest = multiPart;
	}

	// [endregion]

	// [region] public methods

	@Override
	public void run() {
		_currentThread = Thread.currentThread();
		boolean requestInProgress = true;
		int retryCount = 0;
		do {
			boolean isConnected = false;
			HttpURLConnection connection = null;
			int responseCode = -1;
			try {
				// Check for cancellation
				throwIfInterrupted();

				// Add query string params to uri
				String uriWithQueryParams = addQueryStringParametersToUri(
					this.uri, this.queryParameters);

				URL url = new URL(uriWithQueryParams);

				// Create and open request/connection
				_serviceClient.serviceOperationDidBegin(this);

				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod(method.name());

				// Set headers
				if (this.headers != null) {
					for (String field : this.headers.keySet()) {
						connection.setRequestProperty(field,
							this.headers.get(field));
					}
				}

				connection.setReadTimeout(_requestTimeoutInMilliseconds);
				connection.setConnectTimeout(_requestTimeoutInMilliseconds);
				connection.setUseCaches(_useCaches);
				connection.setDoInput(true);

				// check for cancellation
				throwIfInterrupted();

				//For MultiPart requests push data directly onto the 
				//connection's output stream to avoid OutOfMemory errors
				if(_multiPartRequest)
				{
					Bitmap image = null;
					byte[] imageStream = null;
					FileInputStream fileInputStream = null;
					String dataDispositionAndType = "";
					File _video = null;

					int multiPartDividerLength = multiPartDivider.getBytes().length;
					int contentLength = multiPartDividerLength;
					
					int preDataEntitiesSize = _preDataEntities.length;
					int postDataEntitiesSize = _postDataEntities == null ? _dataEntity == null ? 0 : 1 : _postDataEntities.length;

					String [] preDataEntityInstances = new String[preDataEntitiesSize];
					String [] postDataEntityInstances = new String[postDataEntitiesSize];

					//Must first calculate the content-length for the entire request
					for (int i = 0; i < preDataEntitiesSize; i++)
					{
						//When no file to upload close request body after the last item in the preDataEntity array
						if(_dataEntity == null)
						{
							if(i == _preDataEntities.length - 1)
							{
								preDataEntityInstances[i] = addMultiPartStringWithDivider(_preDataEntities[i].content, _preDataEntities[i].name, _preDataEntities[i].contentType, true);
							}
							else
							{
								preDataEntityInstances[i] = addMultiPartStringWithDivider(_preDataEntities[i].content, _preDataEntities[i].name, _preDataEntities[i].contentType, false);
							}
						}
						else
						{
							preDataEntityInstances[i] = addMultiPartStringWithDivider(_preDataEntities[i].content, _preDataEntities[i].name, _preDataEntities[i].contentType, false);
						}
						contentLength += preDataEntityInstances[i].getBytes().length;
						//Log.w("ANDY", ""+beforeSegmentContents[i].getBytes().length);
					}

					for (int i = 0; i < postDataEntitiesSize; i++)
					{
						//If no postDataEntity we close request body after the data entity
						if(_postDataEntities == null) {
							postDataEntityInstances[i] = multiPartFinalDivider;
						}
						//The last multiPart entity must be closed off correctly
						else if(i == _postDataEntities.length - 1) {
							postDataEntityInstances[i] = addMultiPartStringWithDivider(_postDataEntities[i].content, _postDataEntities[i].name, _postDataEntities[i].contentType, true);
						}
						else {
							postDataEntityInstances[i] = addMultiPartStringWithDivider(_postDataEntities[i].content, _postDataEntities[i].name, _postDataEntities[i].contentType, false);
						}
						contentLength += postDataEntityInstances[i].getBytes().length;
						//Log.w("ANDY", ""+postDataEntityInstances[i].getBytes().length);
					}

					if(_dataEntity != null)
					{
						dataDispositionAndType = addMultiPartDataDispositionAndType(_dataEntity.name, _dataEntity.filename, _dataEntity.contentType);

						contentLength += dataDispositionAndType.getBytes().length;
						//Log.w("ANDY", ""+dataDispositionAndType.getBytes().length);

						switch (_dataEntity.dataType)
						{
							case IMAGE:
								image = (Bitmap) _dataEntity.fileContent;

								//Need to compress image to calculate the length for content-length header
								ByteArrayOutputStream baos = new ByteArrayOutputStream();
								image.compress(Bitmap.CompressFormat.JPEG, 100, baos); 
								imageStream = baos.toByteArray();
								baos.close();

								//Log.w("ANDY", "Image bytes: "+imageSizeStream.length);
								contentLength += imageStream.length;
								break;
							case VIDEO:
								String vid = (String) _dataEntity.fileContent;
								_video = new File(vid);
								fileInputStream = new FileInputStream(_video);
								Log.w("ANDY", "Video bytes: "+_video.length());
								contentLength += _video.length();

								break;
							default:
								raiseCompletion(ServiceResultStatus.FAILED, ServiceClientConstants.SERVICE_RESPONSE_STATUS_CODE_BAD_PARAMETERS, null);
								requestInProgress = false;
								break;
						}

						if(_postDataEntities != null)
						{
							contentLength += multiPartDataDivider.getBytes().length;
							Log.w("ANDY", ""+multiPartDataDivider.getBytes().length);
						}
					}

					//Set headers for MultiPart requests
					connection.setRequestProperty("Content-Length", String.valueOf(contentLength));
					connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
					connection.setRequestProperty("Accept", "*/*");
					connection.setRequestProperty("Accept-Language", "en-us");
					connection.setRequestProperty("Connection", "keep-alive");
					connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

					connection.setDoOutput(true);

					OutputStream wr = connection.getOutputStream();

					//Write MultiPart entities inc. file to upload connection's output stream
					wr.write(multiPartDivider.getBytes());
					//Log.w("ANDY", multiPartDivider);

					for(int i = 0; i < preDataEntitiesSize; i++)
					{
						wr.write(preDataEntityInstances[i].getBytes());
						//Log.w("ANDY", new String(preDataEntityInstances[i].getBytes()));
					}

					if(_dataEntity != null)
					{
						wr.write(dataDispositionAndType.getBytes());
						//Log.w("ANDY", dataDispositionAndType);

						switch (_dataEntity.dataType)
						{
							case IMAGE:
								wr.write(imageStream);
								//image.compress(Bitmap.CompressFormat.JPEG, 100, wr);

								break;
							case VIDEO:
								fileInputStream = new FileInputStream(_video);
								byte[] buffer;
								int maxBufferSize = 16*1024;

								try
								{
									buffer = new byte[maxBufferSize];

									int read = 0;
									while ((read = fileInputStream.read(buffer)) != -1) {
										wr.write(buffer, 0, read);
									}
									fileInputStream.close();
									break;
								}
								catch (IOException e)
								{
									raiseCompletion(ServiceResultStatus.FAILED, ServiceClientConstants.SERVICE_RESPONSE_STATUS_CODE_SERVER_ERROR, null);
									requestInProgress = false;
								}
								break;
							default:
								raiseCompletion(ServiceResultStatus.FAILED, ServiceClientConstants.SERVICE_RESPONSE_STATUS_CODE_BAD_PARAMETERS, null);
								requestInProgress = false;
								break;
						}
					}

					if(_postDataEntities != null)
					{
						wr.write(multiPartDataDivider.getBytes());

						//Log.w("ANDY", multiPartDataDivider);
					}
					for(int i = 0; i < postDataEntitiesSize; i++)
					{
						wr.write(postDataEntityInstances[i].getBytes());
						//Log.w("ANDY", new String(postDataEntityInstances[i].getBytes()));
					}

					wr.flush();
					wr.close();

					// Verify the responseCode after sending output
					responseCode = verifyResponseCode(connection);
				}
				else
				{
					// create and send body data to request
					byte[] bodyData = _bodyDataProvider == null ? null
						: _bodyDataProvider.getBodyData();

					if (bodyData != null) {
						connection.setRequestProperty("Content-Length", ""
							+ bodyData.length);

						connection.setDoOutput(true);

						DataOutputStream wr = new DataOutputStream(
							connection.getOutputStream());
						wr.write(bodyData);
						wr.flush();
						wr.close();

						// Verify the responseCode after sending output
						responseCode = verifyResponseCode(connection);
					} else {
						connection.setDoOutput(false);
					}
				}



				// check for cancellation
				throwIfInterrupted();

				// Get response
				InputStream in = connection.getInputStream();

				// Verify the responseCode before reading from the stream (only
				// if not read after output)
				if (responseCode == -1) {
					responseCode = verifyResponseCode(connection);
				}

				// Check for cancellation
				throwIfInterrupted();

				byte[] responseBody = readFromStream(in);
				connection.disconnect();
				connection = null;
				_serviceClient.serviceOperationDidEnd(this);

				// Check for cancellation
				throwIfInterrupted();

				// process response
				TResponse data = _serviceClient
					.transformDataIntoResponseFormat(this, responseBody,
						_responseFormat);

				TResult result = null;
				if (_responseTransform != null) {
					result = _responseTransform.transformResponseData(data);
				} else
					// since there is no transform, set data to be the result
				{
					@SuppressWarnings("unchecked")
					TResult resultTemp = (TResult) data;
					result = resultTemp;
				}

				// Check for cancellation
				throwIfInterrupted();

				raiseCompletion(ServiceResultStatus.SUCCESS, responseCode,
					result);

				requestInProgress = false;
			} catch (InterruptedException ie) {
				Logger.e(getClass().getName(),
					"Service Operation Task Cancelled.", ie);
				raiseCompletion(ServiceResultStatus.CANCELLED, responseCode,
					null);

				requestInProgress = false;
			} catch (FileNotFoundException fnfe) {
				Logger.e(getClass().getName(), "File not found.", fnfe);
				raiseCompletion(ServiceResultStatus.FAILED, ServiceClientConstants.SERVICE_RESPONSE_STATUS_CODE_NOT_FOUND, null);
				requestInProgress = false;
			} catch (ServiceResponseTransformException te) {
				Logger.e(getClass().getName(),
					"Error transforming response data.", te);
				raiseCompletion(ServiceResultStatus.FAILED, responseCode, null);

				requestInProgress = false;
			} catch (InvalidResponseCodeException ire) {
				responseCode = ire.responseCode;
				Logger.e(getClass().getName(),
					"Error: Service Request Failed.", ire);

				_serviceClient.serviceOperationFailed(this, ire);
				boolean retryRequired = _serviceClient
					.serviceOperationShouldRetry(this, responseCode,
						retryCount);
				if (retryRequired) {
					++retryCount;
				} else {
					raiseCompletion(ServiceResultStatus.FAILED, responseCode,
						null);

					requestInProgress = false;
				}
			}
			catch (Exception ex) {
				Logger.e(getClass().getName(),
					"Error: Service Request Failed.", ex);

				_serviceClient.serviceOperationFailed(this, ex);
				boolean retryRequired = _serviceClient
					.serviceOperationShouldRetry(this, responseCode,
						retryCount);
				if (retryRequired) {
					++retryCount;
				} else {
					raiseCompletion(ServiceResultStatus.FAILED, responseCode,
						null);

					requestInProgress = false;
				}
			} finally {
				// close the connection if it hasn't been closed already
				if (connection != null && isConnected) {
					connection.disconnect();
				}
			}

		} while (requestInProgress);
	}

	private byte[] readFromStream(InputStream in) throws InterruptedException,
	IOException {
		BufferedInputStream bis = new BufferedInputStream(in);
		ByteArrayBuffer baf = new ByteArrayBuffer(RESPONSE_BUFFER_SIZE);
		int read = 0;
		byte[] buffer = new byte[RESPONSE_BUFFER_SIZE];
		while (true) {
			throwIfInterrupted();
			read = bis.read(buffer);
			if (read == -1) {
				break;
			}
			baf.append(buffer, 0, read);
		}
		buffer = null;

		byte[] responseBody = baf.toByteArray();

		// Disconnect to release resources
		bis.close();
		baf.clear();
		return responseBody;
	}

	private int verifyResponseCode(HttpURLConnection connection)
		throws IOException, InvalidResponseCodeException {
		int responseCode;
		try {
			responseCode = connection.getResponseCode();
		} catch (IOException e) {
			// Older Android versions have trouble handling 401 errors.
			if (e.getMessage().contains("authentication challenge")) {
				return HttpsURLConnection.HTTP_UNAUTHORIZED;
			} else {
				throw e;
			}
		}
		if (responseCode < HTTP_OK_STATUS_CODE
			|| responseCode >= HTTP_MULTIPLE_CHOICES_CODE) {
			String statusMessage = connection.getResponseMessage();
			String errorMessage = "";
			try {
				// try to read the error stream.
				InputStream in = connection.getErrorStream();
				byte[] errorBody = readFromStream(in);
				errorMessage = new String(errorBody,
					ServiceClientConstants.UTF8_ENCODING);
			} catch (Exception ex) {
				Logger.e(this.getClass().getName(),
					"No Error Body in Response", ex);
			}

			throw new InvalidResponseCodeException(responseCode, statusMessage,
				errorMessage);
		}
		return responseCode;
	}

	// [endregion]

	// [region] protected methods

	protected void cancel() {
		if (_currentThread != null) {
			_currentThread.interrupt();
		}
	}

	// [endregion]

	// [region] private methods

	private void raiseCompletion(final ServiceResultStatus resultStatus,
		final int responseCode, final TResult result) {
		// raise completion
		if (_completion != null) {
			Handler handler = new Handler(Looper.getMainLooper());
			handler.post(new Runnable() {
				@Override
				public void run() {
					_completion
					.onCompletion(resultStatus, responseCode, result);
				}
			});// end runOnUiThread

		}
	}

	private void throwIfInterrupted() throws InterruptedException {
		if (_currentThread.isInterrupted()) {
			throw new InterruptedException();
		}
	}

	private static String addQueryStringParametersToUri(String uri,
		Map<String, String> queryParameters)
			throws UnsupportedEncodingException {
		String uriWithQueryParams = uri;
		if (queryParameters != null && !queryParameters.isEmpty()) {
			uriWithQueryParams += "?";
			boolean addAmperstand = false;
			for (String field : queryParameters.keySet()) {
				if (addAmperstand) {
					uriWithQueryParams += "&";
				}
				uriWithQueryParams += field
					+ "="
					+ URLEncoder.encode(queryParameters.get(field),
						ServiceClientConstants.UTF8_ENCODING);
				addAmperstand = true;
			}
		}
		return uriWithQueryParams;
	}

	private String addMultiPartStringWithDivider(String content, String name, String contentType, boolean finalPart)
	{
		StringBuilder partData = new StringBuilder();
		partData.append("Content-Disposition: form-data; name=\"" + name + "\"" + lineEnd);
		partData.append("Content-Type: " + contentType + lineEnd + lineEnd);
		partData.append(content);
		partData.append(lineEnd);
		if(finalPart)
		{
			partData.append(multiPartFinalDivider);
		}
		else
		{
			partData.append(multiPartDivider);
		}

		return partData.toString();
	}

	private String addMultiPartDataDispositionAndType(String name, String filename, String contentType)
	{
		String disp = "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + filename + "\"" + lineEnd;
		String cont = "Content-Type: " + contentType + lineEnd + lineEnd;

		StringBuilder partData = new StringBuilder();
		partData.append(disp);
		partData.append(cont);

		return partData.toString();
	}

	// [endregion]

}