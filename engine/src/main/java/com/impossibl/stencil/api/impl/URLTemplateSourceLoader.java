package com.impossibl.stencil.api.impl;

import java.io.IOException;
import java.net.URL;

import com.impossibl.stencil.api.TemplateSource;
import com.impossibl.stencil.api.TemplateSourceLoader;

/**
 * URL template source loader that simply maps the path to a file URL.
 * 
 * @author kdubb
 *
 */
public class URLTemplateSourceLoader implements TemplateSourceLoader {

  /**
   * Maps the given path to a file URL and builds a @see URLTemplateSource for
   * it.
   * @return URLTemplateSource for the path
   */
  @Override
  public TemplateSource find(String path) throws IOException {
    return new URLTemplateSource(new URL("file", null, path));
  }

}
