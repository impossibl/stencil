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
   * @return Array of parameter names in order
   * @see Preparable#ALL_BLOCK_NAME
   * @see Preparable#ALL_BLOCKS
   * @see Preparable#UNNAMED_BLOCK_NAME
   * @see Preparable#prepare(Map)
   */
	String[] getBlockNames();
	
	
  /**
   * Execute the preparable with the given parameters.
   * 
   * @param params Map of blocks passed in; each value will be a Block or a map of blocks in the "all blocks" case
   * @return The result of the execution
   * @throws Throwable Author may throw any exception
   */
	Object prepare(Map<String, ?> params) throws Throwable;
	
}
