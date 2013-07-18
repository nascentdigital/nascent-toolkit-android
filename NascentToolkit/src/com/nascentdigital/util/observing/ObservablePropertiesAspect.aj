package com.nascentdigital.util.observing;


import java.lang.reflect.Field;
import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.aspectj.lang.reflect.FieldSignature;
import com.nascentdigital.util.Logger;
import com.nascentdigital.util.observing.Observable;
import com.nascentdigital.util.observing.ObservableField;
import com.nascentdigital.util.observing.Unobserved;


public privileged aspect ObservablePropertiesAspect
{

	// [region] pointcuts

	pointcut fieldSet(Observable observable, Object value) :
		set(@ObservableField public !static * *.*)
		&& !withincode(Observable+.new(..))
		&& !withincode(* Observable+.onObservableChanged(..))
		&& target(observable)		
		&& args(value);

	pointcut collectionSet(Object object) :
		target(object)
		&& target(java.util.List);

	pointcut executingUnobservedAction() :
		within(@Unobserved *)
		|| withincode(@Unobserved * *.*(..));

	// [endregion]


	// [region] advice
	
	@SuppressAjWarnings
	void around(Observable observable, Object newValue) 
		: fieldSet(observable, newValue)
			&& !executingUnobservedAction()
	{
		// skip if old value can't be captured
		FieldSignature signature = (FieldSignature)thisJoinPoint.getSignature();
		Field field = signature.getField();
		Object oldValue;
		try
		{
			oldValue = field.get(observable);
		}
		catch (Exception e)
		{
			Logger.e("ObservablePropertiesApect",
				"Unable to capture old value", e);
			return;
		}

		// skip if value is unchanged
		if (newValue == oldValue
			|| (newValue != null && newValue.equals(oldValue) == true))
		{
			return;
		}

		// proceed with field setter
		proceed(observable, newValue);

		// determine field id
		ObservableField annotation = field.getAnnotation(ObservableField.class);

		// raise event
		String fieldName = field.getName();
		observable.raiseObservableChanged(annotation, fieldName, oldValue,
			newValue);
	}

	// [endregion]

} // aspect ObservableAspect
