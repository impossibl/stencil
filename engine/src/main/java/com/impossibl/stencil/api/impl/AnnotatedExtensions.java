package com.impossibl.stencil.api.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import com.impossibl.stencil.api.Named;
import com.impossibl.stencil.engine.ExecutionException;
import com.impossibl.stencil.engine.InvocationException;
import com.thoughtworks.paranamer.AnnotationParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;

public class AnnotatedExtensions {

  private static Paranamer paranamer = new CachingParanamer(new AnnotationParanamer() {

    @Override
    protected String getNamedValue(Annotation ann) {
      return ((Named)ann).value();
    }

    @Override
    protected boolean isNamed(Annotation ann) {
      return Named.class.isInstance(ann);
    }
    
  });

  public static String[] getParameterNames(Method method) {
    return paranamer.lookupParameterNames(method);
  }
  
  public static Method findMethod(String name, Class<?> cls) {
    
    for(Method method : cls.getDeclaredMethods()) {
      if(method.getName().equals(name)) {
        return method;
      }
    }
    
    throw new ExecutionException("invalid auto-function: no '" + name + "' method declared");
  }

  public static Object exec(Method method, String[] paramNames, Map<String,?> params, Object instance) throws Throwable {
    
    Object[] paramValues = new Object[paramNames.length];

    for(int c=0; c < paramNames.length; ++c) {
      paramValues[c] = params.get(paramNames[c]);
    }
    
    try {
      return method.invoke(instance, paramValues);
    }
    catch (IllegalAccessException e) {
      //Shouldn't happen
      throw new RuntimeException(e);
    }
    catch (IllegalArgumentException e) {
      throw new InvocationException("invalid arguments", e);
    }
    catch (InvocationTargetException e) {
      throw e.getTargetException();
    }
    
  }
  
}
