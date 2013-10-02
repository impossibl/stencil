package com.impossibl.stencil.api.impl;

import java.lang.reflect.Method;
import java.util.Map;

import com.impossibl.stencil.api.Preparable;

public abstract class AnnotatedPreparableBase implements Preparable {
  
  Method method;
  
  public String[] getBlockNames() {
    return AnnotatedExtensions.getParameterNames(getMethod());
  }
  
  public Object prepare(Map<String, ?> params) throws Throwable {
    return AnnotatedExtensions.exec(getMethod(), getBlockNames(), params, this);
  }

  public Method getMethod() {
    
    if(method == null) {
      method = AnnotatedExtensions.findMethod("doPrepare", getClass());
    }
    
    return method;
  }
  
}
