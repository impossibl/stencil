package com.impossibl.stencil.api.impl;

import java.lang.reflect.Method;
import java.util.Map;

import com.impossibl.stencil.api.Callable;
import com.impossibl.stencil.api.Preparable;

/**
 * Base implementation of Callable and Preparable that automatically
 * maps parameters to annotated parameters on a method named "doCall" and
 * automatically maps blocks to annotated blocks on a method named "doPrepare".
 * 
 * @author kdubb
 *
 */
public abstract class AnnotatedCallablePreparableBase implements Callable, Preparable {
  
  Method callMethod;
  Method prepareMethod;
  
  /**
   * Examines a method named "doCall" for parameters annotated with the
   * Named annotation.
   * @see com.impossibl.stencil.api.Named
   */
  public String[] getParameterNames() {
    return AnnotatedExtensions.getParameterNames(getCallMethod());
  }
  
  /**
   * Examines a method named "doPrepare" for blocks annotated with the
   * Named annotation.
   * @see com.impossibl.stencil.api.Named
   */
  public String[] getBlockNames() {
    return AnnotatedExtensions.getParameterNames(getPrepareMethod());
  }

  /**
   * Executes a method named "doCall" by mapping parameters by name to
   * parameters annotated with the Named annotation
   * @see com.impossibl.stencil.api.Named
   */
  public Object call(Map<String, ?> params) throws Throwable {
    return AnnotatedExtensions.exec(getCallMethod(), getParameterNames(), params, this);
  }

  /**
   * Executes a method named "doPrepare" by mapping blocks by name to blocks
   * annotated with the Named annotation
   * @see com.impossibl.stencil.api.Named
   */
  public Object prepare(Map<String, ?> params) throws Throwable {
    return AnnotatedExtensions.exec(getPrepareMethod(), getParameterNames(), params, this);
  }

  /**
   * Lookup a cached method named "doCall".
   * 
   * @return Cached "doCall" method
   */
  public Method getCallMethod() {
    
    if(callMethod == null) {
      callMethod = AnnotatedExtensions.findMethod("doCall", getClass());
    }
    
    return callMethod;
  }

  /**
   * Lookup a cached method named "doPrepare".
   * 
   * @return Cached "doPrepare" method
   */
  public Method getPrepareMethod() {
    
    if(prepareMethod == null) {
      prepareMethod = AnnotatedExtensions.findMethod("doPrepare", getClass());
    }
    
    return prepareMethod;
  }

}
