package com.nascenttoolkitdemos.services;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import android.location.Location;
import com.nascentdigital.communication.ServiceClient;
import com.nascentdigital.communication.ServiceClientCompletion;
import com.nascentdigital.communication.ServiceMethod;
import com.nascentdigital.communication.ServiceResponseFormat;
import com.nascentdigital.communication.ServiceResponseTransform;
import com.nascentdigital.util.Logger;

public class GoogleMapsServiceClient extends ServiceClient
{
	
	// [region] constants
	
	private static final String DISTANCE_MATRIX_SERVICE_URL = "http://maps.googleapis.com/maps/api/distancematrix/json";
	
	// [endregion]

	
	// [region] public methods
	
	public void getTimeToArrivalFromCoordinates (Location fromCoordinates, Location toCoordinates, ServiceClientCompletion<Long> completion)
	{
		//Headers
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("Accept", "application/json");
		
		//Params
		String origin = fromCoordinates.getLatitude() + "," + fromCoordinates.getLongitude();
		String destination = toCoordinates.getLatitude() + "," + toCoordinates.getLongitude();
		
		Map<String,String> params = new HashMap<String,String>();
		params.put("origins", origin);
		params.put("destinations", destination);
		params.put("sensor", "false");
	
		
		//transform
		ServiceResponseTransform<JSONObject,Long> responseTransform = new ServiceResponseTransform<JSONObject,Long> () 
			{
				@Override
				public Long transformResponseData (JSONObject json)
				{
					Long result = null;
					try
					{					
						result = json.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("duration").getLong("value");					    
					}
					catch (JSONException e)
					{
						Logger.e("GoogleMapsServiceClient", "Error parsing JSon in getTimeToArrivalFromCoordinates transform", e);
						
					}
					return result;
				}
			};
			
		super.beginRequest(DISTANCE_MATRIX_SERVICE_URL,
			ServiceMethod.GET,
			headers,
			params,
			(String)null,
			ServiceResponseFormat.JSON,
			responseTransform,
			completion);
			
	}
	
	// [endregion]

}
