package com.nascentdigital.observing;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;


/**
 * Generic implementation of the observable pattern, based on the java.util.observer class
 */
public class ObservableField<T extends Serializable> extends BaseObservable<FieldObserver<T>> implements Serializable
{
	// [region] instance variables


    /* Value being observed */
	private T value;
	// [endregion]


	// [region] constructors

	/**
	 * Constructs a new observable object.
	 */
	public ObservableField(T value)
	{
		this();
        this.value = value;

	}

	public ObservableField()
	{
        super();
	}

	// [endregion]


	// [region] public methods

	public T getValue()
	{
		return this.value;
	}

	public void setValue(T value)
	{
		this.value = value;
		super.setChanged();
		notifyObservers(value);
	}

	// [endregion]

    // [region] observer methods



    /**
     * If the Observable has actually changed then tell all Observers about it,
     * then reset state to unchanged. Note that though the order of
     * notification is unspecified in subclasses, in Observable it is in the
     * order of registration.
     *
     * @param oldValue
     * @see Observer#update(Observable, Object)
     */

    protected void notifyObservers(T oldValue)
    {
        if (! hasChanged())
            return;
        // Create clone inside monitor, as that is relatively fast and still
        // important to keep threadsafe, but update observers outside of the
        // lock since update() can call arbitrary code.
        ArrayList<FieldObserver<T>> s;
        synchronized (this)
        {
            s =  observers.toArrayList();
        }
        for (FieldObserver<T> observer : s)
        {
            observer.update(this, oldValue);
        }
        clearChanged();
    }



    // [endregion]







} // class Observable
