package com.nascentdigital.util.observing;


import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Defines a field within an {@link Observable} class as generating change
 * notifications.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ObservableField
{
	// [region] properties

	public int id();

	// [endregion]

} // @interface ObservableField
