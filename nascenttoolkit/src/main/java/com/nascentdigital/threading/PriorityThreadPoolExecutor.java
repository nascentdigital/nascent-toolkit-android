package com.nascentdigital.threading;



import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;



public class PriorityThreadPoolExecutor<TRunnable extends Runnable>
{
	private final ThreadPoolExecutor _executor;

	// [region] constructors

	@SuppressWarnings("unchecked")
	public PriorityThreadPoolExecutor(int corePoolSize, 
		int maximumPoolSize,
		long keepAliveTime, 
		TimeUnit unit,
		PriorityBlockingQueue<TRunnable> workQueue)
	{
		//Object workQueueHack = workQueue;
		_executor =
			new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit,
				(PriorityBlockingQueue<Runnable>)workQueue);
	}

	// [endregion]

	// [region] public methods

	public void execute(TRunnable runnable)
	{
		_executor.execute(runnable);
	}

	// [endregion]

}
