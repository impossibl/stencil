package com.impossibl.stencil.api;

import java.util.Map;

public interface Preparable {
  
  public static final String ALL_BLOCK_NAME = "*";
  public static final String[] ALL_BLOCKS = new String[] {ALL_BLOCK_NAME};
  
  public static final String UNNAMED_BLOCK_NAME = "+";

	String[] getBlockNames();
	
	
	Object prepare(Map<String,Block> params) throws Throwable;
	
}
