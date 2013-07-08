package com.nascentdigital.util.observing;


import java.util.ArrayList;
import java.util.Collection;
import com.nascentdigital.util.WeakList;


public class ObservableArrayList<T> extends ArrayList<T>
{
	// [region] constants

	private static final long serialVersionUID = -7066248475811796640L;

	// [endregion]


	// [region] instance variables

	private WeakList<ObservableListListener<T>> _listeners;

	// [endregion]


	// [region] constructors

	public ObservableArrayList()
	{
		super();
	}

	public ObservableArrayList(Collection<? extends T> collection)
	{
		super(collection);
	}

	public ObservableArrayList(int capacity)
	{
		super(capacity);
	}

	// [endregion]


	// [region] public methods

	/**
	 * Adds a new listener to the list of observers.
	 * 
	 * @param listener
	 *            the listener to be notified of changes.
	 */
	public final void addObservableCollectionListener(
		ObservableListListener<T> listener)
	{
		// lazy-load list
		if (_listeners == null)
		{
			_listeners = new WeakList<ObservableListListener<T>>(16);
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
	public final void removeObservableCollectionListener(
		ObservableListListener<T> listener)
	{
		// skip if no list
		if (_listeners == null)
		{
			return;
		}

		// remove listener
		_listeners.remove(listener);
	}

	public final void touch(T object)
	{
		// raise change if in list
		final int index = indexOf(object);
		if (index >= 0)
		{
			raiseCollectionChanged(CollectionChange.TOUCH, index, 1);
		}
	}

	// [endregion]


	// [region] overridden methods

	@Override
	public boolean add(T object)
	{
		add(size(), object);
		return true;
	}

	@Override
	public void add(int index, T object)
	{
		// call base implementation
		super.add(index, object);

		// notify listeners
		raiseCollectionChanged(CollectionChange.INSERT, index, 1);
	}

	@Override
	public boolean addAll(Collection<? extends T> collection)
	{
		// track insertion offset
		int offset = size();
		int length = collection.size();

		// call base implementation
		super.addAll(collection);

		// notify listeners
		raiseCollectionChanged(CollectionChange.INSERT, offset, length);

		return true;
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> collection)
	{
		// track insertion offset
		int offset = index;
		int length = collection.size();

		// call base implementation
		super.addAll(index, collection);

		// notify listeners
		raiseCollectionChanged(CollectionChange.INSERT, offset, length);

		return true;
	}

	@Override
	public void clear()
	{
		// skip if no items to remove
		int length = size();
		if (length == 0)
		{
			return;
		}

		// notify listeners
		raiseCollectionChanged(CollectionChange.REMOVE, 0, length);

		// clear list
		super.clear();
	}

	@Override
	public T remove(int index)
	{
		// notify listeners
		raiseCollectionChanged(CollectionChange.REMOVE, index, 1);

		// call base implementation
		T object = super.remove(index);

		// return value
		return object;
	}

	@Override
	public boolean remove(Object object)
	{
		// skip if object is not in list
		final int index = indexOf(object);
		if (index < 0)
		{
			return false;
		}

		// remove object
		remove(index);
		return true;
	}

	@Override
	protected void removeRange(int start, int end)
	{
		// notify listeners
		raiseCollectionChanged(CollectionChange.REMOVE, start, end - start);

		// call base implementation
		super.removeRange(start, end);
	}

	@Override
	public T set(int index, T object)
	{
		// call base implementation
		T previousObject = super.set(index, object);

		// notify listeners
		raiseCollectionChanged(CollectionChange.REPLACE, index, index + 1);

		// return value
		return previousObject;
	}

	// [endregion]


	// [region] helper methods

	private final void raiseCollectionChanged(CollectionChange change,
		int offset, int length)
	{
		// skip if no listeners
		final WeakList<ObservableListListener<T>> listeners = _listeners;
		if (_listeners == null)
		{
			return;
		}

		// notify listeners
		for (ObservableListListener<T> listener : listeners)
		{
			listener
				.onObservableCollectionChanged(this, change, offset, length);
		}
	}

	// [endregion]

} // class ObservableArrayList<T>
