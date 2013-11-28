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

/**
 * Utility class for handling annotated Callable's and Preparable's
 * 
 * @author kdubb
 * @see com.impossibl.stencil.api.Callable
 * @see com.impossibl.stencil.api.Preparable
 *
 */
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

  /**
   * Retrieves the Named annotated parameter names from the given method
   * 
   * @param method Method to retrieve annotated parameter names for
   * @return Names of parameters annotated with Named
   * @see com.impossibl.stencil.api.Named
   */
  public static String[] getParameterNames(Method method) {
    return paranamer.lookupParameterNames(method);
  }
  
  /**
   * Finds a named declared method on the given class.
   * 
   * @param name Name of declared method to retrieve
   * @param cls Class to retrieve method from
   * @return Named method on class
   */
  public static Method findMethod(String name, Class<?> cls) {
    
    for(Method method : cls.getDeclaredMethods()) {
      if(method.getName().equals(name)) {
        return method;
      }
    }
    
    throw new ExecutionException("invalid auto-function: no '" + name + "' method declared", null);
  }

  /**
   * Invokes the given method mapping named parameters to positions based on a
   * given list of parameter names.
   * 
   * @param method Method to invoke
   * @param paramNames Positioned list of parameter names to map direct mapping
   * @param params Named parameters to use
   * @param instance Instance to invoke method on
   * @return Result of method invocation
   * @throws Throwable
   */
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
      throw new InvocationException("invalid arguments", e, null);
    }
    catch (InvocationTargetException e) {
      throw e.getTargetException();
    }
    
  }
  
}
