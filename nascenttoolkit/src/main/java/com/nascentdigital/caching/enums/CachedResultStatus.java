package com.nascentdigital.caching.enums;

/**
 * The status of a result made from a cachable method
 */
public enum CachedResultStatus
{
    /**
     * Live data, the result was not in the cache
     */
    LIVE,
    /**
     * Cached data, the result was in the cache
     */
    CACHED,
    /**
     * An error was encountered trying to make the method call
     */
    ERROR
}
