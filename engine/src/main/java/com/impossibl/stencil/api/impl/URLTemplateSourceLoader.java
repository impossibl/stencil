package com.impossibl.stencil.api.impl;

import java.io.IOException;
import java.net.URL;

import com.impossibl.stencil.api.TemplateSource;
import com.impossibl.stencil.api.TemplateSourceLoader;

public class URLTemplateSourceLoader implements TemplateSourceLoader {

  @Override
  public TemplateSource find(String path) throws IOException {
    return new URLTemplateSource(new URL("file", null, path));
  }

}
