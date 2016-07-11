package com.nascentdigital.observing;


import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


public class ObservableArrayList<T> extends BaseObservable<ArrayListObserver<T>> implements Serializable, List<T>
{
	// [region] constants

	private static final long serialVersionUID = -7066248475811796640L;

	// [endregion]


	// [region] instance variables

	private final ArrayList<T> list;


	// [endregion]


	// [region] constructors

	public ObservableArrayList()
	{
		list = new ArrayList<>();
	}

	public ObservableArrayList(Collection<? extends T> collection)
	{
		list = new ArrayList<>(collection);
	}

	public ObservableArrayList(int capacity)
	{
		list = new ArrayList<>(capacity);
	}

	// [endregion]


	// [region] public methods



	public final void touch(T object)
	{
		// raise change if in list
		final int index = indexOf(object);
		if (index >= 0)
		{
			setChanged();
			raiseCollectionChanged(CollectionChangeType.TOUCH, index, 1);
		}
	}

	// [endregion]


	// [region] overridden methods

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return list.contains(o);
	}

	@NonNull
	@Override
	public Iterator<T> iterator() {
		return list.iterator();
	}

	@Override
	public Object[] toArray ()
	{
		return list.toArray();
	}

	@NonNull
	@Override
	public <T1> T1[] toArray(T1[] t1s) {
		return list.toArray(t1s);
	}


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
		list.add(index, object);

		// notify listeners
		raiseCollectionChanged(CollectionChangeType.INSERT, index, 1);
	}

	@Override
	public boolean addAll(Collection<? extends T> collection)
	{
		// track insertion offset
		int offset = size();
		int length = collection.size();

		// call base implementation
		list.addAll(collection);

		// notify listeners
		raiseCollectionChanged(CollectionChangeType.INSERT, offset, length);

		return true;
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> collection)
	{
		// track insertion offset
		int length = collection.size();

		// call base implementation
		list.addAll(index, collection);

		// notify listeners
		raiseCollectionChanged(CollectionChangeType.INSERT, index, length);

		return true;
	}

	@Override
	public boolean removeAll(Collection<?> collection) {
		for (Object object : collection)
		{
			this.remove(object);
		}
		return true;
	}

	@Override
	public boolean retainAll(Collection<?> collection) {
		ArrayList<T> toRemove = new ArrayList<>(list);
		for (Object object : collection)
		{
			if (list.contains(object))
			{
				toRemove.remove(object);
			}
		}
		removeAll(toRemove);
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

		// clear list
		list.clear();
		
		// notify listeners
		raiseCollectionChanged(CollectionChangeType.REMOVE, 0, length);
	}

	@Override
	public T get(int i) {
		return list.get(i);
	}

	@Override
	public T remove(int index)
	{

		// call base implementation
		T object = list.remove(index);
		
		// notify listeners
		raiseCollectionChanged(CollectionChangeType.REMOVE, index, 1);

		// return value
		return object;
	}

	@Override
	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(0);
	}

	@Override
	public ListIterator<T> listIterator() {
		return list.listIterator();
	}

	@NonNull
	@Override
	public ListIterator<T> listIterator(int index) {
		return list.listIterator(index);
	}

	@NonNull
	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
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
	public boolean containsAll(Collection<?> collection) {
		return list.containsAll(collection);
	}


	@Override
	public T set(int index, T object)
	{
		// call base implementation
		T previousObject = list.set(index, object);

		// notify listeners
		raiseCollectionChanged(CollectionChangeType.REPLACE, index, index + 1);

		// return value
		return previousObject;
	}

	// [endregion]


	// [region] helper methods

	private void raiseCollectionChanged(CollectionChangeType changeType,
										int offset, int length)
	{
		CollectionChange change = new CollectionChange(changeType, offset, length);
		setChanged();
		notifyObservers(change);
	}

    protected void notifyObservers(CollectionChange collectionChange)
    {
        if (! hasChanged())
            return;
        // Create clone inside monitor, as that is relatively fast and still
        // important to keep threadsafe, but update observers outside of the
        // lock since update() can call arbitrary code.
        ArrayList<ArrayListObserver<T>> s;
        synchronized (this)
        {
            s =  observers.toArrayList();
        }
        for (ArrayListObserver<T> observer : s)
        {
            observer.update(this, collectionChange);
        }
        clearChanged();
    }

	// [endregion]

} // class ObservableArrayList<T>
