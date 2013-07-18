package com.nascentdigital.util.observing;


import com.nascentdigital.util.LogLevel;
import com.nascentdigital.util.Logger;
import com.nascentdigital.util.WeakList;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;


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

	private SparseArray<WeakList<ObservableListener>> _listenerGroups;

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
	 * Adds the specified listener to receive event callbacks whenever the
	 * targeted fields are modified.
	 * 
	 * @param listener
	 *            the listener to register for callbacks
	 * @param fieldIds
	 *            the ids of the fields being observed by the specified
	 *            listener/observer
	 */
	public final void addObservableListener(ObservableListener listener,
		int... fieldIds)
	{
		if (fieldIds == null || fieldIds.length < 1)
		{
			throw new IllegalArgumentException(
				"At least one field identifier must be specified.");
		}

		// lazy-load listener groups
		if (_listenerGroups == null)
		{
			_listenerGroups = new SparseArray<WeakList<ObservableListener>>(8);
		}

		// lazy-load listeners
		for (int fieldId : fieldIds)
		{
			// create list if required
			WeakList<ObservableListener> listeners =
				_listenerGroups.get(fieldId);
			if (listeners == null)
			{
				listeners = new WeakList<ObservableListener>(4);
				_listenerGroups.put(fieldId, listeners);
			}

			// add listener
			listeners.add(listener);
		}
	}

	/**
	 * Removes a new listener from the list of observers.
	 * 
	 * @param listener
	 *            the listener to stop notifying of changes.
	 * @param fieldIds
	 *            a set of field ids to be unregistered for, or no parameters if
	 *            all fields should be unregistered for the specified listener
	 */
	public final void removeObservableListener(ObservableListener listener,
		int... fieldIds)
	{
		// skip if no listener groups exist
		if (_listenerGroups == null)
		{
			return;
		}

		// remove from all fields
		if (fieldIds == null)
		{
			int listenerGroupCount = _listenerGroups.size();
			for (int i = 0; i < listenerGroupCount; ++i)
			{
				WeakList<ObservableListener> listeners =
					_listenerGroups.valueAt(i);
				listeners.remove(listener);
			}
		}

		// or remove from specific fields specified
		else
		{
			for (int fieldId : fieldIds)
			{
				WeakList<ObservableListener> listeners =
					_listenerGroups.get(fieldId);
				if (listeners != null)
				{
					listeners.remove(listener);
				}
			}
		}
	}

	// [endregion]


	// [region] helper methods

	public final void raiseObservableChanged(final ObservableField field,
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
					raiseObservableChanged(field, fieldName, oldValue, newValue);
				}
			});

			// stop processing
			return;
		}

		if (Logger.level.allows(LogLevel.VERBOSE))
		{
			Logger.v(
				"Observable",
				getClass().getSimpleName() + "@"
					+ System.identityHashCode(this) + " updated " + fieldName
					+ ": " + newValue);
		}

		// notify self
		onObservableChanged(field, fieldName, oldValue, newValue);

		// skip if no groups are defined
		if (_listenerGroups == null)
		{
			return;
		}

		// skip if no listeners
		final int fieldId = field.value();
		final WeakList<ObservableListener> listeners =
			_listenerGroups.get(fieldId);
		if (listeners == null)
		{
			return;
		}

		// notify listeners
		for (ObservableListener listener : listeners)
		{
			listener.onObservableChanged(this, field, fieldName, oldValue,
				newValue);
		}
	}

	protected void onObservableChanged(ObservableField field, String fieldName,
		Object oldValue, Object newValue)
	{
	}

	// [endregion]

} // class Observable
