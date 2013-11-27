package com.impossibl.stencil.api.impl;

import java.util.Map;

import com.impossibl.stencil.api.GlobalScope;

/**
 * GlobalScope backed by Map
 * 
 * @author kdubb
 *
 */
public class MapGlobalScope implements GlobalScope {
  
  
  Map<String, Object> source;
  

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
