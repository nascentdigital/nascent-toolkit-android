package com.nascentdigital.util.observing;


/**
 * Implementations of this interface will execute without triggering instances
 * of the {@link Observable} to raise change events on {@link ObservableField}
 * annotated fields.
 */
public interface UnobservedAction
{
	// [region] methods

	/**
	 * Executes the specified unobserved action.
	 */
	public void execute();

	// [endregion]

} // interface UnobservedAction
