package com.nascentdigital.util.observing;


/**
 * An <code>ObservableListener</code> is notified of any changes of
 * {@link ObservableField} annotated fields on the target to which it is
 * registered.
 * 
 * @see Observable
 * @see ObservableField
 */
public interface ObservableListener
{
	// [region] methods

	/**
	 * Raised whenever an {@link Observable} object's field changes.
	 * 
	 * The implementation should use the <code>fieldId</code> to efficiently
	 * branch handling of notification callbacks.
	 * 
	 * @param sender
	 *            the observable instance that changed.
	 * @param fieldId
	 *            the identifier for the field that changed.
	 * @param fieldName
	 *            the field name.
	 * @param oldValue
	 *            the value prior to the field change (primitives are boxed).
	 * @param newValue
	 *            the value after the field change (primitives are boxed).
	 */
	void onObservableChanged(Object sender, int fieldId, String fieldName,
		Object oldValue, Object newValue);

	// [endregion]

} // interface ObservableListener
