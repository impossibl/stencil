package com.impossibl.stencil.api;

import java.util.Map;

/**
 * An object that can be executed via prepare syntax (aka block passing).
 * 
 * @author kdubb
 *
 */
public interface Preparable {
  
  public static final String ALL_BLOCK_NAME = "*";
  public static final String[] ALL_BLOCKS = new String[] {ALL_BLOCK_NAME};
  
  public static final String UNNAMED_BLOCK_NAME = "+";

  /**
   * Gets the block names this preparable will accept.
   * 
   * Returning a single parameter name of @see ALL_BLOCK_NAME will result in
   * the @see prepare method being invoked with all blocks passed.
   * 
   * @return Array of parameter names in order
   */
	String[] getBlockNames();
	
	
  /**
   * Execute the preparable with the given parameters.
   * 
   * @param params Map of blocks passed in
   * @return The result of the execution
   * @throws Throwable Author may throw any exception
   */
	Object prepare(Map<String,Block> params) throws Throwable;
	
}
