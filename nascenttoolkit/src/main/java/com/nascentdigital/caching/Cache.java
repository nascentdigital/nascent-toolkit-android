package com.nascentdigital.caching;

import android.content.Context;
import android.util.Log;

import com.nascentdigital.caching.enums.CachePersistanceMode;
import com.nascentdigital.caching.enums.CachedResultStatus;
import com.nascentdigital.caching.exceptions.CachePersistanceModeException;
import com.nascentdigital.util.StorageHelper;
import com.nascentdigital.util.StringHelper;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Class that implements caching. Note that using the annotations (@Cacheable and @NonCacheable)
 * should be used instead of the CacheResult and
 */
public class Cache implements Serializable
{
    //region static fields
    private transient static Cache instance;
    //endregion

    //region fields
    private HashMap<String, CacheableResult<?>> cache = new HashMap<>();

    private transient final String stateKey = this.getClass().getName();
    private transient CachePersistanceMode persistanceMode = CachePersistanceMode.IN_MEMORY_ONLY;
    private transient Context context = null;
    //endregion

    //region static methods

    public static synchronized Cache getInstance()
    {
        if (instance == null)
        {
            instance = new Cache();
        }
        return instance;
    }

    //endregion

    //region public methods



    /**
     * Default state.
     * Cache only exists in memory.
     * Data will be deleted when application
     * is closed or tombstoned.
     */
    public void setPersistanceModeToInMemoryOnly()
    {
        persistanceMode = CachePersistanceMode.IN_MEMORY_ONLY;
    }

    /**
     * Cache is persisted on internal storage
     * as well as in memory. The cache will
     * be loaded from disk when the application
     * starts up.
     */
    public void setPersistanceModeToInternalStorage(Context applicationContext)
    {
        persistanceMode = CachePersistanceMode.INTERNAL_STORAGE;
        context = applicationContext;
    }

    /**
     *
     * @return the persistance mode used by the cache
     */
    public CachePersistanceMode getPersistanceMode()
    {
        return persistanceMode;
    }

    /**
     * Saves the cache to disk if persistance mode set to INTERNAL_STORAGE
     */
    public void saveState()
    {
        if (persistanceMode == CachePersistanceMode.IN_MEMORY_ONLY)
        {
            return;
        }
        synchronized (this) {
            StorageHelper.saveToInternalStorage(stateKey, this, context);
        }
    }

    /**
     * Loads the cache from disk if persistance mode is set to INTERNAL_STORAGE
     * @throws CachePersistanceModeException if persistance mode is set to IN_MEMORY_ONLY.
     */
    public void loadState() throws CachePersistanceModeException
    {
        if (persistanceMode == CachePersistanceMode.IN_MEMORY_ONLY)
        {
            throw new CachePersistanceModeException
                    ("Error: Cache.loadState() called while persistance mode set to IN_MEMORY_ONLY. " +
                            "Set the persistance mode to INTERNAL_STORAGE before loading state by calling " +
                            "setPersistanceModeToInternalStorage(Context applicationContext).", persistanceMode);
        }
        synchronized (this) {
            Cache savedState = StorageHelper.loadFromInternalStorage(stateKey, context);
            if (savedState != null) {
                this.cache = savedState.cache;
            }
        }
    }

    /**
     * Caches the result
     *
     * @Deprecated use the @Cacheable method annotation instead of this method
     * @param key string key to store and retreive value
     * @param result cached value to store
     * @param expiresInMins amount of time before result expires in memory
     * @param <T> type of cacheable result, must be serializable
     */
    @Deprecated
    public <T extends Serializable> void cacheResult (String key, CacheableResult<T> result, int expiresInMins)
    {
        //Don't cache ERROR responses
        if (result.status == CachedResultStatus.ERROR)
        {
            Log.e("baseManager", "Error, attempt to cache ERROR response: "
                    + StringHelper.isNull(result.errorMessage, "<No Error Message>"));
            return;
        }

        //first make a copy of the result object to put in the cache
        CacheableResult<T> copy = new CacheableResult<>(CachedResultStatus.CACHED);
        copy.createdDate = result.createdDate;

        copy.expiryDate = copy.createdDate.plusMinutes(expiresInMins);

        copy.errorMessage = result.errorMessage;
        copy.result = result.result;

        cache.put(key, copy);
        saveState();
    }

    /**
     * retreives the cached result, and if it exists calls the completion supplied.
     *
     * @Deprecated use the @Cacheable method annotation instead of this method
     * @param key string key that the cached result is stored under
     * @param completion completion to be called if result is in cache
     * @param <T> type
     * @return cached result, or null if item is not in cache
     */
    @Deprecated
    public <T extends Serializable> CacheableResult<T> getCachedResultAndCallCompletion(String key, final CacheableCompletion<T> completion)
    {
        //Get previous result from cache
        CacheableResult<T> cachedResult = getCachedResult(key);

        if (cachedResult != null)
        {
            completion.onCompletion(cachedResult);
        }

        return cachedResult;
    }

    /**
     * removes the assocated cached value from the cache
     * @param key
     */
    public void removeCachedResult(String key)
    {
        if (cache.containsKey(key)) {
            cache.remove(key);
        }
        saveState();
    }

    /**
     * clears out the cache completely
     */
    public void clearCache()
    {

        cache.clear();
        saveState();
    }

    /**
     * returns true if the key is in the cache.
     * @param key
     * @return
     */
    public boolean contains(String key) {
        return cache.containsKey(key);
    }

    //region helper methods
    private <T extends Serializable> CacheableResult<T> getCachedResult(String key)
    {
        CacheableResult<T> result = null;
        if (cache.containsKey(key))
        {
            result = (CacheableResult<T>) cache.get(key);

            //If the CACHED entry has expired, return null and clear the entry
            DateTime now = DateTime.now();
            if (result.expiryDate != null && now.isAfter(result.expiryDate))
            {
                cache.remove(key);
                return null;
            }

        }
        return result;
    }
    //endregion

}
