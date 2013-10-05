package com.impossibl.stencil.api;

import java.io.IOException;
import java.io.Writer;

/**
 * Block of output commands.  Allows @see Callable and @see Preparable
 * objects to retrieve the text of blocks programmatically
 * 
 * @author kdubb
 *
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
