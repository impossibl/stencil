package com.impossibl.stencil.api;


public interface GlobalScope {
  public Object NOT_FOUND = new Object();

  Object get(String name);
  
}
