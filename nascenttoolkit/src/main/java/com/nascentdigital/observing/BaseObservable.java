package com.nascentdigital.observing;

import com.nascentdigital.collections.WeakList;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by ahinton on 16-06-30.
 */
abstract class BaseObservable<OBSERVER_TYPE>{

    /** Tracks whether this object has changed. */
    private transient boolean changed;

    /* List of the Observers registered as interested in this Observable. */
    protected final transient WeakList<OBSERVER_TYPE> observers;

    protected BaseObservable()
    {
        observers = new WeakList<>();
    }

    /**
     * Adds an Observer. If the observer was already added this method does
     * nothing.
     *
     * @param observer Observer to add
     * @throws NullPointerException if observer is null
     */

    public synchronized void addObserver(OBSERVER_TYPE observer)
    {
        if (observer == null)
            throw new NullPointerException("can't add null observer");
        observers.add(observer);
    }

    /**
     * Reset this Observable's state to unchanged. This is called automatically
     * by <code>notifyObservers</code> once all observers have been notified.
     *
     * @see #notifyObservers()
     */

    protected synchronized void clearChanged()
    {
        changed = false;
    }



    /**
     * Deletes an Observer of this Observable.
     *
     * @param victim Observer to delete
     */

    public synchronized void deleteObserver(OBSERVER_TYPE victim)
    {
        observers.remove(victim);
    }

    /**
     * Deletes all Observers of this Observable.
     */

    public synchronized void deleteObservers()
    {
        observers.clear();
    }

    /**
     * True if <code>setChanged</code> has been called more recently than
     * <code>clearChanged</code>.
     *
     * @return whether or not this Observable has changed
     */

    public synchronized boolean hasChanged()
    {
        return changed;
    }

    /**
     * If the Observable has actually changed then tell all Observers about it,
     * then reset state to unchanged.
     *
     * @see #notifyObservers(Object)
     * @see Observer#update(Observable, Object)
     */

    /**
     * Marks this Observable as having changed.
     */

    protected synchronized void setChanged()
    {
        changed = true;
    }


}
