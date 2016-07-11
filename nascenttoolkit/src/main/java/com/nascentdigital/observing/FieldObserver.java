package com.nascentdigital.observing;


import java.io.Serializable;

public interface FieldObserver<T extends Serializable>{
    void update(ObservableField<T> observableField, T oldValue);

}
