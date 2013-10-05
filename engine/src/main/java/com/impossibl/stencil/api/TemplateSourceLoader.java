package com.impossibl.stencil.api;

import java.io.IOException;

/**
 * Locates and loads loads template source.
 * 
 * @author kdubb
 *
 */
public interface TemplateSourceLoader {
  
  /**
   * Find and load template source for the given path.
   * 
   * @param path Path to retrieve template source for
   * @return Template source information
   * @throws IOException
   */
  TemplateSource find(String path) throws IOException;

}
