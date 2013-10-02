package com.impossibl.stencil.ext.std;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.impossibl.stencil.api.GlobalScope;
import com.impossibl.stencil.api.Named;
import com.impossibl.stencil.engine.StencilEngine;

public class StandardGlobals implements GlobalScope {
  
  private static final Logger logger = LogManager.getLogger(StandardGlobals.class);

  private static Map<String, ?> globals = getGlobals();
  
  public StandardGlobals() {
    logger.info("Initializing Standard Globals");
  }

  @Override
  public Object get(String name) {
    Object val = globals.get(name);
    if(val == null) {
      val = getType(name);
    }
    return val;
  }

  private static Map<String,?> getGlobals() {
    
    ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
    
    add(builder, new JoinFunction());
    add(builder, new RawFunction());
    add(builder, new AttrsFunction());
    
    return builder.build();
  }

  private static void add(ImmutableMap.Builder<String, Object> builder, Object func) {
    Named named = func.getClass().getAnnotation(Named.class);
    if(named != null) {
      logger.info("Adding Extension Function: {} ({})", named.value(), func.getClass().getName());
      builder.put(named.value(), func);
    }
    else {
      logger.warn("Extension Function '{}' is missing @Named annotation", func.getClass().getName());
    }
  }

  private static Class<?> getType(String typeName) {
    
    try {
      return StencilEngine.class.getClassLoader().loadClass(typeName);
    }
    catch (ClassNotFoundException e) {
      return null;
    }
    
  }
  
}
