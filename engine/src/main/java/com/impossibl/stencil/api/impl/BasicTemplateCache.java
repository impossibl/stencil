package com.impossibl.stencil.api.impl;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.impossibl.stencil.api.TemplateCache;

public class BasicTemplateCache implements TemplateCache {
  
  Cache<URI,CachedTemplate> cache;

  public BasicTemplateCache(int cacheDurationSeconds) {
    this(cacheDurationSeconds, TimeUnit.SECONDS);
  }

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
