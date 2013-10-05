package com.impossibl.stencil.api.impl;

import java.lang.reflect.Method;
import java.util.Map;

import com.impossibl.stencil.api.Callable;

/**
 * Base implementation of @see Callable that automatically maps parameters to
 * annotated parameters on a method named "doCall".
 * 
 * @author kdubb
 *
 */
public abstract class AnnotatedCallableBase implements Callable {
  
  Method method;

  /**
   * Examines a method named "doCall" for parameters annotated with the @see
   * Named annotation.
   */
  public String[] getParameterNames() {
    return AnnotatedExtensions.getParameterNames(getMethod());
  }
  
  /**
   * Executes a method named "doCall" by mapping parameters by name to
   * parameters annotated with the @see Named annotation
   */
  public Object call(Map<String, ?> params) throws Throwable {
    return AnnotatedExtensions.exec(getMethod(), getParameterNames(), params, this);
  }

  /**
   * Lookup a cached method named "doCall".
   * 
   * @return Cached "doCall" method
   */
  public Method getMethod() {
    
    if(method == null) {
      method = AnnotatedExtensions.findMethod("doCall", getClass());
    }
    
    return method;
  }

}
