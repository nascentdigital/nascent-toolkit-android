package com.nascentdigital.util;


import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


public final class WeakList<T> implements Collection<T>
{
	// [region] constants

	private static final long MIN_PRUNE_TIMEOUT = 10000;

	// [endregion]


	// [region] class variables

	@SuppressWarnings("rawtypes")
	private static final Iterator _emptyIterator;

	// [endregion]


	// [region] instance variables

	private final ArrayList<WeakReference<T>> _list;
	private long _lastPruneTime;

	// [endregion]


	// [region] constructors

	static
	{
		// initialize class variables
		_emptyIterator = new EmptyIterator();
	}

	public WeakList()
	{
		_list = new ArrayList<WeakReference<T>>();
		_lastPruneTime = new Date().getTime();
	}

	public WeakList(int capacity)
	{
		_list = new ArrayList<WeakReference<T>>(capacity);
		_lastPruneTime = new Date().getTime();
	}

	// [endregion]


	// [region] public methods

	public boolean add(T object)
	{
		// prune (if required)
		final long time = new Date().getTime();
		final long ellapsedTime = time - _lastPruneTime;
		if (ellapsedTime > MIN_PRUNE_TIMEOUT)
		{
			_lastPruneTime = time;
			prune();
		}

		// add item
		return _list.add(new WeakReference<T>(object));
	}

	public boolean addAll(Collection<? extends T> objects)
	{
		// skip if nothing to add
		final int objectCount = objects.size();
		if (objectCount == 0)
		{
			return false;
		}

		// prune (if required)
		final long ellapsedTime = new Date().getTime() - _lastPruneTime;
		if (ellapsedTime > MIN_PRUNE_TIMEOUT)
		{
			prune();
		}

		// add items
		int objectIndex = 0;
		for (T object : objects)
		{
			_list.add(objectIndex++, new WeakReference<T>(object));
		}

		return true;
	}

	public void clear()
	{
		_list.clear();
	}

	public boolean contains(Object object)
	{
		for (WeakReference<T> objectReference : _list)
		{
			if (objectReference.get() == object)
			{
				return true;
			}
		}

		return false;
	}

	@Deprecated
	public boolean containsAll(Collection<?> objects)
	{
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public boolean isEmpty()
	{
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	public Iterator<T> iterator()
	{
		// skip list is empty
		final int referenceCount = _list.size();
		if (referenceCount == 0)
		{
			return _emptyIterator;
		}

		// copy/prune list
		final List<T> objects = new ArrayList<T>(referenceCount);
		final List<WeakReference<T>> removedReferences =
			new ArrayList<WeakReference<T>>(referenceCount);
		for (int i = 0; i < referenceCount; ++i)
		{
			WeakReference<T> objectReference = _list.get(i);
			T object = objectReference.get();
			if (object == null)
			{
				removedReferences.add(objectReference);
			}
			else
			{
				objects.add(object);
			}
		}

		// remove pruned references (if any)
		final int removedReferenceCount = removedReferences.size();
		if (removedReferenceCount > 0)
		{
			_list.removeAll(removedReferences);
		}
		_lastPruneTime = new Date().getTime();

		// return iterator for strong references
		return objects.iterator();
	}

	public void prune()
	{
		// update prune time
		_lastPruneTime = new Date().getTime();

		// skip list is empty
		final int referenceCount = _list.size();
		if (referenceCount == 0)
		{
			return;
		}

		// aggregate references to prune
		final List<WeakReference<T>> removedReferences =
			new ArrayList<WeakReference<T>>(referenceCount);
		for (int i = 0; i < referenceCount; ++i)
		{
			WeakReference<T> objectReference = _list.get(i);
			T object = objectReference.get();
			if (object == null)
			{
				removedReferences.add(objectReference);
			}
		}

		// remove pruned references (if any)
		final int removedReferenceCount = removedReferences.size();
		if (removedReferenceCount > 0)
		{
			_list.removeAll(removedReferences);
		}
	}

	public boolean remove(Object target)
	{
		// skip list is empty
		final int referenceCount = _list.size();
		if (referenceCount == 0)
		{
			return false;
		}

		// aggregate references to prune
		boolean targetFound = false;
		final List<WeakReference<T>> removedReferences =
			new ArrayList<WeakReference<T>>(referenceCount);
		for (int i = 0; i < referenceCount; ++i)
		{
			WeakReference<T> objectReference = _list.get(i);
			T object = objectReference.get();
			if (object == null)
			{
				removedReferences.add(objectReference);
			}
			else if (object == target)
			{
				removedReferences.add(objectReference);
				targetFound = true;
			}
		}

		// remove pruned references (if any)
		final int removedReferenceCount = removedReferences.size();
		if (removedReferenceCount > 0)
		{
			_list.removeAll(removedReferences);
		}
		_lastPruneTime = new Date().getTime();

		// return success
		return targetFound;
	}

	@Deprecated
	public boolean removeAll(Collection<?> arg0)
	{
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public boolean retainAll(Collection<?> arg0)
	{
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public int size()
	{
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public Object[] toArray()
	{
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public <E> E[] toArray(E[] array)
	{
		throw new UnsupportedOperationException();
	}

	// [endregion]


	// [region] internal data structures

	@SuppressWarnings("rawtypes")
	private static final class EmptyIterator implements Iterator
	{
		// [region] public methods

		public boolean hasNext()
		{
			return false;
		}

		public Object next()
		{
			return null;
		}

		public void remove()
		{
		}

		// [endregion]

	} // class EmptyIterator

	// [endregion]

} // endregion
