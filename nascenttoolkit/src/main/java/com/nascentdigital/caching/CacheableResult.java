package com.nascentdigital.caching;


import com.nascentdigital.caching.enums.CachedResultStatus;

import org.joda.time.DateTime;

import java.io.Serializable;

public class CacheableResult<T extends Serializable> implements Serializable
{
    private final int defaultStaleTimeInSeconds = 60;

    public DateTime createdDate;
    public DateTime expiryDate;


    public T result;
    public CachedResultStatus status;
    public String errorMessage;

    public CacheableResult(CachedResultStatus status)
    {
        createdDate = DateTime.now();
        expiryDate = DateTime.now();

        this.status = status;
    }


    public boolean isExpired()
    {
        DateTime now = DateTime.now();
        return (this.expiryDate != null && now.isAfter(this.expiryDate));
    }



}
