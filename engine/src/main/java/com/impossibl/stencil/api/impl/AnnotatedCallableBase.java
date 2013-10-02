package com.impossibl.stencil.api.impl;

import java.lang.reflect.Method;
import java.util.Map;

import com.impossibl.stencil.api.Callable;

public abstract class AnnotatedCallableBase implements Callable {
  
  Method method;
  
  public String[] getParameterNames() {
    return AnnotatedExtensions.getParameterNames(getMethod());
  }
  
  public Object call(Map<String, ?> params) throws Throwable {
    return AnnotatedExtensions.exec(getMethod(), getParameterNames(), params, this);
  }

  public Method getMethod() {
    
    if(method == null) {
      method = AnnotatedExtensions.findMethod("doCall", getClass());
    }
    
    return method;
  }

}
