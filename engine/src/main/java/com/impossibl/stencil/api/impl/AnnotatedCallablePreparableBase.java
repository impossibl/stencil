package com.impossibl.stencil.api.impl;

import java.lang.reflect.Method;
import java.util.Map;

import com.impossibl.stencil.api.Block;
import com.impossibl.stencil.api.Callable;
import com.impossibl.stencil.api.Preparable;

public abstract class AnnotatedCallablePreparableBase implements Callable, Preparable {
  
  Method callMethod;
  Method prepareMethod;
  
  public String[] getParameterNames() {
    return AnnotatedExtensions.getParameterNames(getCallMethod());
  }
  
  public String[] getBlockNames() {
    return AnnotatedExtensions.getParameterNames(getPrepareMethod());
  }

  public Object call(Map<String, ?> params) throws Throwable {
    return AnnotatedExtensions.exec(getCallMethod(), getParameterNames(), params, this);
  }

  public Object prepare(Map<String, Block> params) throws Throwable {
    return AnnotatedExtensions.exec(getPrepareMethod(), getParameterNames(), params, this);
  }

  public Method getCallMethod() {
    
    if(callMethod == null) {
      callMethod = AnnotatedExtensions.findMethod("doCall", getClass());
    }
    
    return callMethod;
  }

  public Method getPrepareMethod() {
    
    if(prepareMethod == null) {
      prepareMethod = AnnotatedExtensions.findMethod("doPrepare", getClass());
    }
    
    return prepareMethod;
  }

}
