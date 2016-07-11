package com.nascentdigital.caching.aspects;

/*
 * CachingWithAspectJ
 * Copyright (C) 2008 Christian Schenk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */


import com.nascentdigital.caching.Cache;
import com.nascentdigital.caching.CacheableCompletion;

import org.aspectj.lang.JoinPoint;

/**
 * Some helper methods for AspectJ.
 *
 * @author Christian Schenk
 */
class CacheableMethodAspectHelper {

    /**
     * Convenience method that returns the class- and method-name for a given join point.
     */
    public static String getJoinPointName(final JoinPoint joinPoint) {
        return joinPoint.getThis().getClass().getSimpleName() + "build/intermediates/exploded-aar/com.instabug.library/instabugsupport/1.7.4/res" + joinPoint.getSignature().getName();
    }

    /**
     * Returns the arguments of the current join point as a string.
     * @param joinPoint
     * @return string representing the arguments of this join point
     */
    public static String getJoinPointArgs(final JoinPoint joinPoint) {
        final StringBuilder buf = new StringBuilder();
        for (final Object arg : joinPoint.getArgs()) {
            if ( arg != null && !(arg instanceof CacheableCompletion) ) {
                buf.append(arg.getClass().getSimpleName()).append("-").append(arg).append("+");
            }
        }
        return buf.toString().replaceAll("\\+$", "");
    }

    /***
     *
     * @param joinPoint
     * @param keyName cache key name
     * @param expiresInMins minutes value will be valid in the cache for
     * @return True if there was an object in the cache
     */
    public static boolean getAndCallJoinPointManagerCompletionArg(final JoinPoint joinPoint, String keyName, int expiresInMins)
    {
        for (final Object arg : joinPoint.getArgs()) {
            if ( arg != null && (arg instanceof CacheableCompletion))
            {
                CacheableCompletion<?> completion = (CacheableCompletion<?>)arg;
                //noinspection deprecation
                if (Cache.getInstance().getCachedResultAndCallCompletion(keyName, completion) == null)
                {
                    completion.setCacheable(keyName, expiresInMins);
                    return false; //no item in the cache
                }
                else {
                    return true; //item in the cache
                }
            }
        }
        return false; //no completion and thus no item in the cache
    }

}
