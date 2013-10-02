package com.impossibl.stencil.api.impl;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletContext;

import com.impossibl.stencil.api.TemplateSource;
import com.impossibl.stencil.api.TemplateSourceLoader;

public class ServletContextTemplateSourceLoader implements TemplateSourceLoader {

  ServletContext servletContext;
  String prefix;
  String suffix;
  
  public ServletContextTemplateSourceLoader(ServletContext servletContext, String prefix, String suffix) {
    this.servletContext = servletContext;
    this.prefix = prefix;
    this.suffix = suffix;
  }

  public TemplateSource find(String path) throws IOException {
    URL url = servletContext.getResource(prefix + path + suffix);
    return new URLTemplateSource(url);
  }

}
