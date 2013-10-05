package com.impossibl.stencil.api;

import java.util.Map;

/**
 * An object that can be executed via call syntax
 *  
 * @author kdubb
 *
 */
public interface Callable {
  
  public static final String ALL_PARAM_NAME = "*";
  public static final String[] ALL_PARAMS = new String[] {ALL_PARAM_NAME};

  /**
   * Gets the parameter names this callable will accept.
   * 
   * Returning a single parameter name of @see ALL_PARAM_NAME will result in
   * the @see call method being invoked with all parameters passed.
   * 
   * @return Array of parameter names in order
   */
	String[] getParameterNames();
	
	/**
	 * Execute the callable with the given parameters.
	 * 
	 * Note: Positional parameters are passed with names equivalent to their
	 * position in the call (e.g. 0=..., 1=..., n=...)
	 *  
	 * @param params Map of parameters passed in
	 * @return The result of the execution
	 * @throws Throwable Author may throw any exception
	 */
	Object call(Map<String,?> params) throws Throwable;
	
}
