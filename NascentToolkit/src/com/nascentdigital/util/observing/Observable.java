package com.nascentdigital.util.observing;


import com.nascentdigital.util.Logger;
import com.nascentdigital.util.WeakList;

import android.os.Handler;
import android.os.Looper;


/**
 * Classes deriving from the <code>Observable</code> class will receive
 * notifications for any fields annotated with the {@link ObservableField}
 * annotation.
 * 
 * @see ObservableField
 * @see ObservableListener
 */
public class Observable
{
	// [region] class variables

	private static final Handler _handler;

	// [endregion]


	// [region] instance variables

	private WeakList<ObservableListener> _listeners;

	// [endregion]


	// [region] constructors

	static
	{
		// initialize class variables
		_handler = new Handler(Looper.getMainLooper());
	}

	/**
	 * Constructs a new observable object.
	 */
	public Observable()
	{
	}

	// [endregion]


	// [region] public methods

	/**
	 * Adds a new listener to the list of observers.
	 * 
	 * @param listener
	 *            the listener to be notified of changes.
	 */
	public final void addObservableListener(ObservableListener listener)
	{
		// lazy-load list
		if (_listeners == null)
		{
			_listeners = new WeakList<ObservableListener>(16);
		}

		// add listener
		_listeners.add(listener);
	}

	/**
	 * Removes a new listener from the list of observers.
	 * 
	 * @param listener
	 *            the listener to stop notifying of changes.
	 */
	public final void removeObservableListener(ObservableListener listener)
	{
		// skip if no listeners
		if (_listeners == null)
		{
			return;
		}

		// remove listener
		_listeners.remove(listener);
	}

	// [endregion]


	// [region] helper methods

	public final void raiseObservableChanged(final int fieldId,
		final String fieldName, final Object oldValue, final Object newValue)
	{
		// post on main thread if on background thread
		if (Looper.myLooper() != _handler.getLooper())
		{
			// post call
			_handler.post(new Runnable()
			{
				public void run()
				{
					raiseObservableChanged(fieldId, fieldName, oldValue,
						newValue);
				}
			});

			// stop processing
			return;
		}

		Logger.v("Observable", "updated " + fieldName + ": " + newValue);

		// notify self
		onObservableChanged(fieldId, fieldName, oldValue, newValue);

		// skip if no listeners
		final WeakList<ObservableListener> listeners = _listeners;
		if (listeners == null)
		{
			return;
		}

		// notify listeners
		for (ObservableListener listener : listeners)
		{
			listener.onObservableChanged(this, fieldId, fieldName, oldValue,
				newValue);
		}
	}

	protected void onObservableChanged(int fieldId, String fieldName,
		Object oldValue, Object newValue)
	{
	}

	// [endregion]

} // class Observable
