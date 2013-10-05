package com.impossibl.stencil.api;

import java.io.IOException;
import java.io.Writer;

/**
 * Block of output commands.  Allows Callable and Preparable
 * objects to retrieve the text of blocks programmatically
 * 
 * @author kdubb
 * @see Callable
 * @see Preparable
 */
public interface Block {
  
  /**
   * Processes the block and writes the resulting text to the given character
   * stream
   * 
   * @param out Character stream to write to
   * @throws IOException
   */
  void write(Writer out) throws IOException;

}
