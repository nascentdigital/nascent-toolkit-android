package com.nascentdigital.util.observing;


import java.util.List;


/**
 * An <code>ObservableListListener</code> is notified of any changes in the
 * target <code>Collection</code> to which it is registered.
 */
public interface ObservableListListener<T>
{
	// [region] methods

	/**
	 * Indicates a change in the targeted sender collection. The change type is
	 * determined by the <code>change</code> parameter.
	 * 
	 * @param list
	 *            the collection that has been changed.
	 * @param change
	 *            a value defining the type of change (update, insertion, or
	 *            removal).
	 * @param offset
	 *            the index of the change if applicable, or -1 otherwise.
	 * @param length
	 *            the number of elements added/removed/updated if applicable, or
	 *            0 otherwise.
	 */
	void onObservableCollectionChanged(List<T> list, CollectionChange change,
		int offset, int length);

	// [endregion]

} // interface ObservableListListener
