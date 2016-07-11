package com.nascentdigital.caching;

import com.nascentdigital.caching.enums.CachedResultStatus;
import com.nascentdigital.util.StringHelper;

import java.io.Serializable;


/**
 * This completion class allows for the completion to be called more than once, with the intention
 * that it would first be called with CACHED data, then later called again with LIVE data.
 * This allows us to quickly load pages with CACHED data, then update them again with LIVE data later.
 */
public abstract class CacheableCompletion<T extends Serializable>
{
    //region private fields
    private int cacheEntryExpiresInMins = 0;
    private String cacheKey = "";

    //endregion



    //region public methods
    public void setCacheable(String key, int expiresInMins)
    {
        this.cacheEntryExpiresInMins = expiresInMins;
        this.cacheKey = key;
    }

    public synchronized void onCompletion (CacheableResult<T> result)
    {
        if (result.status == CachedResultStatus.LIVE)
        {
            if (cacheEntryExpiresInMins > 0 && !StringHelper.isNullOrWhitespace(cacheKey))
            {
                //noinspection deprecation
                Cache.getInstance().cacheResult(cacheKey, result, cacheEntryExpiresInMins);
            }
        }


        executeCompletion(result);
    }
    //endregion

    //region protected methods
    protected abstract void executeCompletion(CacheableResult<T> result);
    //endregion
}
