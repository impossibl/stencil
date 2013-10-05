package com.impossibl.stencil.api;


/**
 * Top level variable scope.
 * 
 * @author kdubb
 *
 */
public interface GlobalScope {
  
  /**
   * Value used to signify the variable was not found in the scope
   */
  public Object NOT_FOUND = new Object();

  /**
   * Lookup a variable by name.
   * 
   * @param name Name of variable
   * @return Value of variable or @see NOT_FOUND if the variable doesn't exist
   */
  Object get(String name);
  
}
