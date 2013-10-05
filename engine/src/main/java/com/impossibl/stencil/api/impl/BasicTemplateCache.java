package com.impossibl.stencil.api.impl;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.impossibl.stencil.api.TemplateCache;

/**
 * Basic template cache that allows entries to expire after a given # of
 * seconds.
 * 
 * @author kdubb
 *
 */
public class BasicTemplateCache implements TemplateCache {
  
  Cache<URI,CachedTemplate> cache;

  /**
   * Construct template cache that expires after a certain number of seconds.
   * 
   * @param cacheDurationSeconds # of second to elapse before entries expire
   */
  public BasicTemplateCache(int cacheDurationSeconds) {
    this(cacheDurationSeconds, TimeUnit.SECONDS);
  }

  /**
   * Construct template cache that expires after a certain amount of time.
   * 
   * @param cacheDuration # of cacheDurationUnits to elapse before entries 
   *        expire
   * @param cacheDurationUnit Unit of time specified in cacheDuration
   *        parameter
   */
  public BasicTemplateCache(int cacheDuration, TimeUnit cacheDurationUnit) {
    cache = CacheBuilder.newBuilder().expireAfterWrite(cacheDuration, cacheDurationUnit).build();    
  }

  @Override
  public CachedTemplate get(URI uri) {
    return cache.getIfPresent(uri);
  }

  @Override
  public CachedTemplate update(URI uri, CachedTemplate cachedTemplate) {
    cache.put(uri, cachedTemplate);
    return cachedTemplate;
  }

  @Override
  public void remove(URI uri) {
    cache.invalidate(uri);
  }

}
