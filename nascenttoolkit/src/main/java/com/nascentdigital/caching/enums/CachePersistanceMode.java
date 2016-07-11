package com.nascentdigital.caching.enums;

/**
 * Defines the persistance mode used by the cache
 */
public enum CachePersistanceMode {
    /**
     * Default value.
     * Cache only exists in memory.
     * Data will be deleted when application
     * is closed or tombstoned.
     */
    IN_MEMORY_ONLY,
    /**
     * Cache is persisted on internal storage
     * as well as in memory. The cache will
     * be loaded from disk when the application
     * starts up.
     */
    INTERNAL_STORAGE
}
