package com.nascentdigital.communication;



public final class ServiceOperation<TResponse, TResult> implements Runnable 
{
	// [region] instance variables
	
	public final ServiceOperationPriority priority;
	public final long timestamp;

	// [endregion]

	// [region] constructors
	
	public ServiceOperation()
	{
		this(ServiceOperationPriority.NORMAL);
	}

	public ServiceOperation(ServiceOperationPriority priority)
	{
		this.priority = priority;
		this.timestamp = System.currentTimeMillis();
	}

	// [endregion]

	// [region] public methods

	@Override
	public void run()
	{
		//TODO: Implement request operation
	}


	// [endregion]

}