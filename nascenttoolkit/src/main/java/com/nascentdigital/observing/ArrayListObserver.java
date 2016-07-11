package com.nascentdigital.observing;

/**
 * Created by ahinton on 16-06-30.
 */
public interface ArrayListObserver<T> {
    void update(ObservableArrayList<T> observableArrayList, CollectionChange collectionChange);
}
