package com.nascentdigital.caching.exceptions;


import com.nascentdigital.caching.enums.CachePersistanceMode;

public class CachePersistanceModeException extends Exception {

    public CachePersistanceMode cachePersistanceMode;

    public CachePersistanceModeException(String message, CachePersistanceMode persistanceMode)
    {
        super(message);
        cachePersistanceMode = persistanceMode;
    }
}
