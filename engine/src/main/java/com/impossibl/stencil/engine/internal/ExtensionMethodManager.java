package com.impossibl.stencil.engine.internal;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import com.google.common.base.Objects;
import com.impossibl.stencil.api.ExtensionMethods;

public class ExtensionMethodManager {
  
  private static class MethodId {

    public Class<?> extendedClass;
    public String extensionMethodName;

    public MethodId(Class<?> extendedClass, String extensionMethodName) {
      this.extendedClass = extendedClass;
      this.extensionMethodName = extensionMethodName;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(extendedClass, extensionMethodName);
    }

    @Override
    public boolean equals(Object obj) {
      MethodId other = (MethodId) obj;
      return
          Objects.equal(extendedClass, other.extendedClass) && 
          Objects.equal(extensionMethodName, other.extensionMethodName);
    }
    
  }
  
  private static Map<MethodId,Method> extensionMap = createExtensionFunctionMap();
  
  static Map<MethodId, Method> createExtensionFunctionMap() {
    
    Map<MethodId, Method> exts = new HashMap<>();
    
    ServiceLoader<ExtensionMethods> extensionLoader = ServiceLoader.load(ExtensionMethods.class);
    
    for(ExtensionMethods ce : extensionLoader) {
      
      Class<?> ceClass = ce.getClass();
      
      for(Method method : ceClass.getMethods()) {
        
        if(Modifier.isStatic(method.getModifiers())) {
          
          Class<?>[] paramTypes = method.getParameterTypes();
          if(paramTypes.length > 0) {
            
            MethodId methodId = new MethodId(paramTypes[0], method.getName());
            
            exts.put(methodId, method);
            
          }
          
        }
        
      }
      
    }
    
    return exts;
  }
  
  public static Method getExtensionMethod(Class<?> type, String method) {
    
    Method res = extensionMap.get(new MethodId(type, method));
    if(res != null) return res;

    for(Class<?> cur=type.getSuperclass(); cur != null; cur = cur.getSuperclass()) {
      res = extensionMap.get(new MethodId(cur, method));
      if(res != null)
        return cache(type, res);
    }
    
    for(Class<?> iface : type.getInterfaces()) {
      res = extensionMap.get(new MethodId(iface, method));
      if(res != null)
        return cache(type, res);
    }
    
    return null;
  }
  
  public static Method cache(Class<?> type, Method method) {
    extensionMap.put(new MethodId(type, method.getName()), method);
    return method;
  }

}
