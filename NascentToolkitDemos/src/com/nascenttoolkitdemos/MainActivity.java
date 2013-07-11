package com.nascenttoolkitdemos;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import com.nascentdigital.communication.ServiceClientCompletion;
import com.nascentdigital.communication.ServiceResultStatus;
import com.nascentdigital.util.LogLevel;
import com.nascentdigital.util.Logger;
import com.nascenttoolkitdemos.services.GoogleMapsServiceClient;


public class MainActivity extends Activity
{
	// [region] static instance variables
	private static final Map<String, Location> _cityToLocation;
	// [endregion]
	
	// [region] instance variables
	private Spinner _fromSpinner, _toSpinner;
	private Button _submitButton;
	private GoogleMapsServiceClient _serviceClient;
	private TextView _resultsTextView;
	// [endregion]
	
	// [region] cctor
	static
	{
		_cityToLocation = populateCityToLocationMap();
	}
	// [region]


	// [region] protected methods
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Init Google maps client
		_serviceClient = new GoogleMapsServiceClient();
		
		Logger.level = LogLevel.VERBOSE;

		
		_resultsTextView = (TextView) findViewById(R.id.resultsTextView);
		_fromSpinner = (Spinner) findViewById(R.id.fromSpinner);
		_toSpinner = (Spinner) findViewById(R.id.toSpinner);
		
		final String[] cities = _cityToLocation.keySet().toArray( new String[_cityToLocation.keySet().size()] );
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, cities);
		dataAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
		
		_fromSpinner.setAdapter(dataAdapter);
		_toSpinner.setAdapter(dataAdapter);
		
		_submitButton = (Button) findViewById(R.id.submitButton);
		setUpSubmitOnClickListener();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	// [endregion]
	
	// [region] private methods
	private void setUpSubmitOnClickListener()
	{
		_submitButton.setOnClickListener(new OnClickListener () {

		@Override
		public void onClick(View arg0)
		{
			final String fromCity = String.valueOf(_fromSpinner.getSelectedItem());
			final String toCity = String.valueOf(_toSpinner.getSelectedItem());
			 
			Location from = _cityToLocation.get(fromCity);
			Location to = _cityToLocation.get(toCity);
			
			//Make network call
			_serviceClient.getTimeToArrivalFromCoordinates(from, to, new ServiceClientCompletion<Long>() {

				@Override
				public void onCompletion(
					final ServiceResultStatus serviceResultStatus,
					final int responseCode, 
					final Long distanceInSeconds)
				{
					//Ensure updates to UI elements happen on the UI thread.
					MainActivity.this.runOnUiThread(new Runnable()
					{
					    public void run()
					    {
					    	if (distanceInSeconds != null && serviceResultStatus == ServiceResultStatus.SUCCESS)
							{
								String formattedTime = formatIntoHoursAndMinutes(distanceInSeconds);
								
								_resultsTextView.setText("It takes " + formattedTime + " to get from " + fromCity + " to " + toCity + ".");
							}
							else
							{
								_resultsTextView.setText("Could not calculate distance due to error. (" + responseCode + ")");
							}
					    }
					});//end runOnUiThread
					
					
				}});//end onCompletion
			
		}});//end onClick
	}
	
	private static String formatIntoHoursAndMinutes(long secondsIn)
	{

		long hours = secondsIn / 3600,
		remainder = secondsIn % 3600,
		minutes = remainder / 60;

	
		return ( (hours > 0 ? hours + " hours and " : "") + 
		 + minutes + " minutes");

	}

	
	private static Map<String, Location> populateCityToLocationMap()
	{
		Map<String, Location> cityToLocationMap = new HashMap<String, Location>();
		
		Location sanFransiso = new Location("CUSTOM");
		sanFransiso.setLatitude(37.78);
		sanFransiso.setLongitude(-122.41);
		cityToLocationMap.put("San Fransisco", sanFransiso);
		
		Location miami = new Location("CUSTOM");
		miami.setLatitude(25.787778);
		miami.setLongitude( -80.224167);
		cityToLocationMap.put("Miami", miami);
		
		Location chicago = new Location("CUSTOM");
		chicago.setLatitude(41.881944);
		chicago.setLongitude(  -87.627778);
		cityToLocationMap.put("Chicago", chicago);
		
		Location mexicoCity = new Location("CUSTOM");
		mexicoCity.setLatitude(19.26);
		mexicoCity.setLongitude(-99.13);
		cityToLocationMap.put("Mexico City", mexicoCity);
		
		Location montreal = new Location("CUSTOM");
		montreal.setLatitude(45.3);
		montreal.setLongitude(-73.34);
		cityToLocationMap.put("Montreal", montreal);
		
		Location newYorkCity = new Location("CUSTOM");
		newYorkCity.setLatitude(40.67);
		newYorkCity.setLongitude(-73.94);
		cityToLocationMap.put("New York City", newYorkCity);
		
		Location toronto = new Location("CUSTOM");
		toronto.setLatitude(43.7);
		toronto.setLongitude(-79.4);
		cityToLocationMap.put("Toronto", toronto);
		
		
		return cityToLocationMap;
	}
	// [endregion]

}
