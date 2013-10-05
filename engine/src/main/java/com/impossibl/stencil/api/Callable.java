package com.impossibl.stencil.api;

import java.util.Map;

public interface Callable {
  
  public static final String ALL_PARAM_NAME = "*";
  public static final String[] ALL_PARAMS = new String[] {ALL_PARAM_NAME};

	String[] getParameterNames();
	Object call(Map<String,?> params) throws Throwable;
	
}
