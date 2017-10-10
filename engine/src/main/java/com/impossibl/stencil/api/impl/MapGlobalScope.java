package com.impossibl.stencil.api.impl;

import com.impossibl.stencil.api.GlobalScope;

import java.util.Map;

/**
 * GlobalScope backed by Map
 * 
 * @author kdubb
 *
 */
public class MapGlobalScope implements GlobalScope {
  
  
  private Map<String, Object> source;
  

  public MapGlobalScope(Map<String, Object> source) {
    this.source = source;
  }

  @Override
  public Object get(String name) {
    if (!source.containsKey(name))
      return GlobalScope.NOT_FOUND;
    return source.get(name);
  }

}
