package com.nascentdigital.caching.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Defines an asynchonous method that is cacheable. A CacheableCompletion must be
 * supplied as an argument in the method to enable proper caching.
 *
 * eg:
 * @Cacheable( expiresInMins = 30)
 * public static void doAsyncTask( final CacheableCompletion<String> completion )
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Cacheable {
    
    // [region] properties

    /**
     * Number of minutes before the result of this method is retained in the cache.
     * @return
     */
    int expiresInMins();

    // [endregion]

}// Cacheable
