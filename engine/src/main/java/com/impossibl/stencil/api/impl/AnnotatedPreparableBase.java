package com.impossibl.stencil.api.impl;

import java.lang.reflect.Method;
import java.util.Map;

import com.impossibl.stencil.api.Block;
import com.impossibl.stencil.api.Preparable;

/**
 * Base implementation of Preparable that automatically maps blocks to
 * annotated blocks on a method named "doPrepare".
 * 
 * @author kdubb
 *
 */
public abstract class AnnotatedPreparableBase implements Preparable {
  
  Method method;
  
  /**
   * Examines a method named "doPrepare" for blocks annotated with the
   * Named annotation.
   * @see com.impossibl.stencil.api.Named
   */
  public String[] getBlockNames() {
    return AnnotatedExtensions.getParameterNames(getMethod());
  }
    
  /**
   * Executes a method named "doPrepare" by mapping blocks by name to blocks
   * annotated with the Named annotation
   * @see com.impossibl.stencil.api.Named
   */
  public Object prepare(Map<String, Block> params) throws Throwable {
    return AnnotatedExtensions.exec(getMethod(), getBlockNames(), params, this);
  }

  /**
   * Lookup a cached method named "doPrepare".
   * 
   * @return Cached "doPrepare" method
   */
  public Method getMethod() {
    
    if(method == null) {
      method = AnnotatedExtensions.findMethod("doPrepare", getClass());
    }
    
    return method;
  }
  
}
