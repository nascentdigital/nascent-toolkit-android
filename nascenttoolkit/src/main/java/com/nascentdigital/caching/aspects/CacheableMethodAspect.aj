package com.nascentdigital.caching.aspects;


import com.nascentdigital.caching.annotations.Cacheable;
import com.nascentdigital.caching.aspects.CacheableMethodAspectHelper;
import java.lang.reflect.Method;
import org.aspectj.lang.reflect.MethodSignature;
import android.util.Log;



public aspect CacheableMethodAspect {

    void around() : execution(@Cacheable * *.*(..) ) {

        //Create Key
        final String thisJoinPointName = CacheableMethodAspectHelper.getJoinPointName(thisJoinPoint);
        final String thisJoinPointArgs = CacheableMethodAspectHelper.getJoinPointArgs(thisJoinPoint);
        final String keyName = thisJoinPointName + "-" + thisJoinPointArgs;

        //Get annotation properties (expire time)
        MethodSignature signature = (MethodSignature)thisJoinPoint.getSignature();
        Method method = signature.getMethod();
        Cacheable cacheableAnnotation = method.getAnnotation(Cacheable.class);

        final boolean cachedResultFound =
            CacheableMethodAspectHelper.getAndCallJoinPointManagerCompletionArg
            (thisJoinPoint, keyName, cacheableAnnotation.expiresInMins());

        if (!cachedResultFound)
        {
             Log.v("CacheHelper", "No cached result found for @Cacheable method: " + keyName);
             proceed();
        }
        else {
            Log.v("CacheHelper", "Cached result found for @Cacheable method: " + keyName);
        }


    }

}